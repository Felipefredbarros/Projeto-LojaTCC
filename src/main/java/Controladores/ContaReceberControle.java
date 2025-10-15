/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Conta;
import Entidades.ContaReceber;
import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoConta;
import Entidades.Enums.TipoLancamento;
import Entidades.LancamentoFinanceiro;
import Entidades.Venda;
import Facade.ContaFacade;
import Facade.ContaReceberFacade;
import Facade.LancamentoFinanceiroFacade;
import Utilitario.FinanceDesc;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class ContaReceberControle implements Serializable {

    private ContaReceber contaReceber = new ContaReceber();
    @EJB
    private ContaReceberFacade contaReceberFacade;
    @EJB
    private LancamentoFinanceiroFacade lancamentoFinanceiroFacade;
    @EJB
    private ContaFacade contaFacade;
    private List<ContaReceber> listaContaRecebers;
    private ContaReceber contaSelecionada;
    private List<ContaReceber> listaContas = new ArrayList<>();
    private MetodoPagamento metodoSelecionado;
    private Conta contaParaReceber;
    private Date dataInicio;
    private Date dataFim;
    private Date dataInicioRecebidas;
    private Date dataFimRecebidas;

    private String obsRecebimento;

    public void salvar() {
        contaReceber.setStatus("ABERTA");
        contaReceber.setCliente(contaReceber.getCliente());
        contaReceber.setDataCriação(new Date());
        contaReceberFacade.salvar(contaReceber);
        contaReceber = new ContaReceber();
    }

    public double getTotalRecebidas() {
        listaContas = contaReceberFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "RECEBIDA".equals(c.getStatus()))
                .mapToDouble(c -> c.getValor())
                .sum();
    }

    public double getTotalAReceber() {
        listaContas = contaReceberFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "ABERTA".equals(c.getStatus()))
                .mapToDouble(c -> c.getValor())
                .sum();
    }

    public double getTotalGerado() {
        listaContas = contaReceberFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "RECEBIDA".equals(c.getStatus()) || "ABERTA".equals(c.getStatus()))
                .mapToDouble(ContaReceber::getValor)
                .sum();
    }

    public void contaReceberItem(ContaReceber c) {
        contaReceberFacade.receberConta(c);
    }

    public void cancelarConta() {
        ContaReceber cr = this.contaSelecionada;

        if ("RECEBIDA".equals(cr.getStatus())) {
            LancamentoFinanceiro original = lancamentoFinanceiroFacade.buscarOriginalRecebimento(cr);
            if (original == null) {
                cr.setStatus("ESTORNADA");
                contaReceberFacade.salvar(cr);
                return;
            }
            original.setStatus(StatusLancamento.ESTORNADO);
            lancamentoFinanceiroFacade.salvar(original);

            LancamentoFinanceiro reverso = new LancamentoFinanceiro();
            reverso.setConta(original.getConta());
            reverso.setTipo(TipoLancamento.SAIDA);
            reverso.setValor(original.getValor());
            reverso.setDataHora(new Date());
            reverso.setMetodo(original.getMetodo());
            reverso.setContaReceber(cr);
            reverso.setStatus(StatusLancamento.NORMAL);

            reverso.setDescricao(FinanceDesc.estornoRecebimentoCR(cr, null));

            lancamentoFinanceiroFacade.salvar(reverso);
            cr.setStatus("ESTORNADA");
            contaReceberFacade.salvar(cr);
            recomputarSaldo(original.getConta());

            cr.setStatus("ESTORNADA");
        } else {
            cr.setStatus("CANCELADA");
        }
        contaReceberFacade.salvar(cr);
    }

    public void prepararCancelamento(ContaReceber c) {
        this.contaSelecionada = c;
    }

    public void prepararRecebimento(ContaReceber c) {
        this.contaSelecionada = c;
        this.metodoSelecionado = null;
        this.contaParaReceber = null;
        this.obsRecebimento = null;

    }

    public void confirmarRecebimento() {
        if (contaSelecionada.getMetodoPagamento() == MetodoPagamento.CARTAO_CREDITO) {
            metodoSelecionado = MetodoPagamento.CARTAO_CREDITO;
        }

        if (contaSelecionada == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma conta a pagar selecionada.", null));
            return;
        }
        if (contaParaReceber == null || contaParaReceber.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Selecione a conta (cofre/banco) para pagar.", null));
            return;
        }
        boolean isDinheiro = MetodoPagamento.DINHEIRO.equals(metodoSelecionado);
        if (isDinheiro && contaParaReceber.getTipoConta() != TipoConta.COFRE) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Para pagamento em dinheiro selecione um Cofre.", null));
            return;
        }
        if (!isDinheiro && contaParaReceber.getTipoConta() != TipoConta.BANCO) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Para este método selecione uma Conta Bancária.", null));
            return;
        }

        // Regra de saldo quando for cofre
        if (contaParaReceber.getTipoConta() == TipoConta.COFRE) {
            double saldoAtual = contaParaReceber.getSaldo() != null ? contaParaReceber.getSaldo() : 0d;
            if (contaSelecionada.getValor() > saldoAtual) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Saldo insuficiente no cofre selecionado. Saldo: R$ " + String.format("%.2f", saldoAtual), null));
                return;
            }
        }

        ContaReceber cr = contaReceberFacade.findWithLancamentos(contaSelecionada.getId());

        LancamentoFinanceiro lanc = new LancamentoFinanceiro();
        lanc.setConta(contaParaReceber);
        lanc.setTipo(TipoLancamento.ENTRADA);
        lanc.setValor(cr.getValor());
        lanc.setDataHora(new Date());
        lanc.setStatus(StatusLancamento.NORMAL);
        lanc.setMetodo(metodoSelecionado);
        lanc.setContaReceber(cr);

        String desc = FinanceDesc.recebimentoContaReceber(cr, obsRecebimento);

        lanc.setDescricao(desc);

        cr.addLancamento(lanc);
        cr.setMetodoPagamento(metodoSelecionado);
        cr.setStatus("RECEBIDA");
        cr.setDataRecebimento(new Date());

        contaReceberFacade.salvar(cr);
        recomputarSaldo(contaParaReceber);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Conta paga via " + metodoSelecionado.getDescricao(), null));

        metodoSelecionado = null;
        contaParaReceber = null;
        obsRecebimento = null;

    }

    private void recomputarSaldo(Conta conta) {
        Double inicial = conta.getValorInicial() != null ? conta.getValorInicial() : 0.0;
        Double entradas = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, Entidades.Enums.TipoLancamento.ENTRADA, null, null);
        Double saidas = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, Entidades.Enums.TipoLancamento.SAIDA, null, null);
        if (entradas == null) {
            entradas = 0d;
        }
        if (saidas == null) {
            saidas = 0d;
        }
        conta.setSaldo(inicial + entradas - saidas);
        contaFacade.salvar(conta);
    }

    public void limparFiltros() {
        dataInicio = null;
        dataFim = null;
        dataInicioRecebidas = null;
        dataFimRecebidas = null;
        listaContas = contaReceberFacade.listaTodos();
    }

    public void exportarPDFFiltradoRecebidas() throws DocumentException, IOException {
        List<ContaReceber> contasParaExportar = contaReceberFacade.buscarPorFiltrosRecebidas(dataInicioRecebidas, dataFimRecebidas);
        exportarPDF(contasParaExportar, false);
    }

    public void exportarPDFFiltradoReceber() throws DocumentException, IOException {
        List<ContaReceber> contasParaExportar = contaReceberFacade.buscarPorFiltrosReceber(dataInicio, dataFim);
        exportarPDF(contasParaExportar, true);
    }

    // se for recebida false, senao true
    public void exportarPDF(List<ContaReceber> contasParaExportar, Boolean is) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_contas.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 20, 30);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        // Helpers de formatação
        java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Funções para evitar NPE
        java.util.function.Function<Object, String> s = o -> o == null ? "-" : o.toString();
        java.util.function.Function<java.util.Date, String> sd = d -> d == null ? "-" : df.format(d);
        java.util.function.Function<java.lang.Number, String> sm = n -> n == null ? "-" : moeda.format(n);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);
            // se for a receber true, senao senao

            if (is == true) {
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
                Paragraph subtitle = new Paragraph("Relatório de Contas a Receber", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(20);
                document.add(subtitle);
            } else {
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
                Paragraph subtitle = new Paragraph("Relatório de Contas Recebidas", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(20);
                document.add(subtitle);
            }

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, Color.BLACK);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            if (contasParaExportar == null || contasParaExportar.isEmpty()) {
                document.add(new Paragraph("Nenhum registro encontrado.", contentFont));
            } else {
                int contaCounter = 1;
                for (ContaReceber con : contasParaExportar) {
                    Paragraph contaHeader = new Paragraph("Conta: " + contaCounter, headerFont);
                    contaHeader.setSpacingAfter(10);
                    document.add(contaHeader);

                    PdfPTable contaTable = new PdfPTable(2);
                    contaTable.setWidthPercentage(100);
                    contaTable.setSpacingBefore(5);
                    contaTable.setSpacingAfter(10);

                    contaTable.addCell(new PdfPCell(new Phrase("ID:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(s.apply(con.getId()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Descrição:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(s.apply(con.getDescricao()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Cliente:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(
                            (con.getCliente() != null && con.getCliente().getNome() != null)
                            ? con.getCliente().getNome()
                            : "-",
                            contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Método de Recebimento:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(
                            con.getMetodoPagamento() == null ? "-" : con.getMetodoPagamento().toString(),
                            contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Data de Criação:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(sd.apply(con.getDataCriação()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Data de Recebimento:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(sd.apply(con.getDataRecebimento()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Data de Vencimento:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(sd.apply(con.getDataVencimento()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Status:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(s.apply(con.getStatus()), contentFont)));

                    contaTable.addCell(new PdfPCell(new Phrase("Valor:", headerFont)));
                    contaTable.addCell(new PdfPCell(new Phrase(sm.apply(con.getValor()), contentFont)));

                    document.add(contaTable);

                    Paragraph separator = new Paragraph(" ");
                    separator.setSpacingAfter(20);
                    document.add(separator);

                    contaCounter++;
                }
            }
        } finally {
            document.close();
            writer.flush();
            writer.close();
            facesContext.responseComplete();
        }
    }

    public List<MetodoPagamento> getMetodosPagamento() {
        return MetodoPagamento.getMetodosPagamentoNaoAVista();
    }

    public void onMetodoChange() {
        // sempre que trocar o método, limpamos a conta escolhida
        this.contaParaReceber = null;
    }

    public boolean isDinheiroSelecionado() {
        return this.metodoSelecionado == Entidades.Enums.MetodoPagamento.DINHEIRO;
    }

    public boolean isMetodoEmDefinicao() {
        return contaSelecionada != null
                && (contaSelecionada.getMetodoPagamento() == MetodoPagamento.A_DENIFIR
                || contaSelecionada.getMetodoPagamento() == MetodoPagamento.A_DENIFIR);
    }

    public void novo() {
        contaReceber = new ContaReceber();
    }

    public void excluir(ContaReceber est) {
        contaReceberFacade.remover(est);
    }

    public void editar(ContaReceber est) {
        this.contaReceber = est;
    }

    public ContaReceber getContaReceber() {
        return contaReceber;
    }

    public void setContaReceber(ContaReceber contaReceber) {
        this.contaReceber = contaReceber;
    }

    public ContaReceberFacade getContaReceberFacade() {
        return contaReceberFacade;
    }

    public void setContaReceberFacade(ContaReceberFacade contaReceberFacade) {
        this.contaReceberFacade = contaReceberFacade;
    }

    public List<ContaReceber> getListaContaRecebers() {
        return contaReceberFacade.listaTodos();
    }

    public List<ContaReceber> getListaContaReceberReais() {
        return contaReceberFacade.listaTodosReais();

    }

    public List<ContaReceber> getListaContaReceberCanceladas() {
        return contaReceberFacade.listaTodosCanceladas();
    }

    public ContaReceber getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(ContaReceber contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public MetodoPagamento getMetodoSelecionado() {
        return metodoSelecionado;
    }

    public void setMetodoSelecionado(MetodoPagamento metodoSelecionado) {
        this.metodoSelecionado = metodoSelecionado;
    }

    public Conta getContaParaReceber() {
        return contaParaReceber;
    }

    public void setContaParaReceber(Conta contaParaReceber) {
        this.contaParaReceber = contaParaReceber;
    }

    public String getObsRecebimento() {
        return obsRecebimento;
    }

    public void setObsRecebimento(String obsRecebimento) {
        this.obsRecebimento = obsRecebimento;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Date getDataInicioRecebidas() {
        return dataInicioRecebidas;
    }

    public void setDataInicioRecebidas(Date dataInicioRecebidas) {
        this.dataInicioRecebidas = dataInicioRecebidas;
    }

    public Date getDataFimRecebidas() {
        return dataFimRecebidas;
    }

    public void setDataFimRecebidas(Date dataFimRecebidas) {
        this.dataFimRecebidas = dataFimRecebidas;
    }

}
