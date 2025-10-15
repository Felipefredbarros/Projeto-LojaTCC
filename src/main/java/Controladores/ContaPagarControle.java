/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Conta;
import Entidades.ContaPagar;
import Entidades.ContaReceber;
import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoConta;
import Entidades.Enums.TipoLancamento;
import Entidades.LancamentoFinanceiro;
import Facade.ContaPagarFacade;
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
import javax.faces.context.FacesContext;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@ManagedBean
@ViewScoped
public class ContaPagarControle implements Serializable {

    private ContaPagar contaPagar = new ContaPagar();
    @EJB
    private ContaPagarFacade contaPagarFacade;
    @EJB
    private LancamentoFinanceiroFacade lancamentoFinanceiroFacade;
    @EJB
    private Facade.ContaFacade contaFacade;
    private List<ContaPagar> listaContaPagar;
    private List<ContaPagar> listaContas = new ArrayList<>();
    private ContaPagar contaSelecionada;
    private MetodoPagamento metodoSelecionado;
    private Conta contaParaPagar;
    private String obsPagamento;
    private Date dataInicio;
    private Date dataFim;
    private Date dataInicioPagas;
    private Date dataFimPagas;

    public void salvar() {
        contaPagar.setStatus("ABERTA");
        contaPagar.setDataCriação(new Date());
        contaPagarFacade.salvar(contaPagar);
        contaPagar = new ContaPagar();
    }

    public double getTotalPagas() {
        listaContas = contaPagarFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "PAGA".equals(c.getStatus()))
                .mapToDouble(c -> c.getValor())
                .sum();
    }

    public double getTotalAPagar() {
        listaContas = contaPagarFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "ABERTA".equals(c.getStatus()))
                .mapToDouble(c -> c.getValor())
                .sum();
    }

    public double getTotalGerado() {
        listaContas = contaPagarFacade.listaTodos();

        return listaContas.stream()
                .filter(c -> "PAGA".equals(c.getStatus()) || "ABERTA".equals(c.getStatus()))
                .mapToDouble(ContaPagar::getValor)
                .sum();
    }

    public void prepararPagamento(ContaPagar conta) {
        System.out.println("aquiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
        this.contaSelecionada = conta;
        System.out.println("CONTA: " + contaSelecionada.getDescricao() + "contacuzaopreto:" + contaSelecionada.getStatus());
        this.metodoSelecionado = null;
        this.obsPagamento = null;
    }

    public void prepararCancelamento(ContaPagar c) {
        this.contaSelecionada = c;
    }

    public void confirmarPagamento() {
        // validações básicas
        if (contaSelecionada == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma conta a pagar selecionada.", null));
            return;
        }
        if (metodoSelecionado == null) {
            // required no diálogo já acusa; só não prossegue
            return;
        }
        if (contaParaPagar == null || contaParaPagar.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Selecione a conta (cofre/banco) para pagar.", null));
            return;
        }

        boolean isDinheiro = MetodoPagamento.DINHEIRO.equals(metodoSelecionado);
        if (isDinheiro && contaParaPagar.getTipoConta() != TipoConta.COFRE) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Para pagamento em dinheiro selecione um Cofre.", null));
            return;
        }
        if (!isDinheiro && contaParaPagar.getTipoConta() != TipoConta.BANCO) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Para este método selecione uma Conta Bancária.", null));
            return;
        }

        // Regra de saldo quando for cofre
        if (contaParaPagar.getTipoConta() == TipoConta.COFRE) {
            double saldoAtual = contaParaPagar.getSaldo() != null ? contaParaPagar.getSaldo() : 0d;
            if (contaSelecionada.getValor() > saldoAtual) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Saldo insuficiente no cofre selecionado. Saldo: R$ " + String.format("%.2f", saldoAtual), null));
                return;
            }
        }
        ContaPagar cp = contaPagarFacade.findWithLancamentos(contaSelecionada.getId());

        LancamentoFinanceiro lanc = new LancamentoFinanceiro();
        lanc.setConta(contaParaPagar);
        lanc.setTipo(TipoLancamento.SAIDA);
        lanc.setValor(cp.getValor());
        lanc.setDataHora(new Date());
        lanc.setStatus(StatusLancamento.NORMAL);
        lanc.setMetodo(metodoSelecionado);
        lanc.setContaPagar(cp);

        lanc.setDescricao(FinanceDesc.pagamentoContaPagar(cp, obsPagamento));

        // vincula nas duas pontas
        cp.addLancamento(lanc);

        // atualiza status da conta
        cp.setMetodoPagamento(metodoSelecionado);
        cp.setStatus("PAGA");
        cp.setDataRecebimento(new Date());

        // salva (cascade vai persistir o lançamento)
        contaPagarFacade.salvar(cp);

        recomputarSaldo(contaParaPagar);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Conta paga via " + metodoSelecionado.getDescricao(), null));

        metodoSelecionado = null;
        contaParaPagar = null;
        obsPagamento = null;
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

    public void cancelarConta() {
        ContaPagar cp = this.contaSelecionada;

        if ("PAGA".equals(cp.getStatus())) {
            LancamentoFinanceiro original = lancamentoFinanceiroFacade.buscarOriginalPagamento(cp);

            if (original == null) {
                cp.setStatus("ESTORNADA");
                contaPagarFacade.salvar(cp);
                return;
            }
            original.setStatus(StatusLancamento.ESTORNADO);
            lancamentoFinanceiroFacade.salvar(original);

            LancamentoFinanceiro reverso = new LancamentoFinanceiro();
            reverso.setConta(original.getConta());
            reverso.setTipo(TipoLancamento.ENTRADA);
            reverso.setValor(original.getValor());
            reverso.setDataHora(new Date());
            reverso.setMetodo(original.getMetodo());
            reverso.setContaPagar(cp);
            reverso.setStatus(StatusLancamento.NORMAL);

            reverso.setDescricao(FinanceDesc.estornoPagamentoCP(cp, null));

            lancamentoFinanceiroFacade.salvar(reverso);
            cp.setStatus("ESTORNADA");
            contaPagarFacade.salvar(cp);
            recomputarSaldo(original.getConta());

        } else {
            cp.setStatus("CANCELADA");
            contaPagarFacade.salvar(cp);

        }
    }

    public void limparFiltros() {
        dataInicio = null;
        dataFim = null;
        dataInicioPagas = null; 
        dataFimPagas = null;
        listaContas = contaPagarFacade.listaTodos();
    }

    public void exportarPDFFiltradoPagas() throws DocumentException, IOException {
        List<ContaPagar> contasParaExportar = contaPagarFacade.buscarPorFiltrosPagas(dataInicioPagas, dataFimPagas);
        exportarPDF(contasParaExportar, false);
        limparFiltros();
    }

    public void exportarPDFFiltradoPagar() throws DocumentException, IOException {
        List<ContaPagar> contasParaExportar = contaPagarFacade.buscarPorFiltrosPagar(dataInicio, dataFim);
        exportarPDF(contasParaExportar, true);
        limparFiltros();
    }

    public void exportarPDF(List<ContaPagar> contasParaExportar, Boolean is) throws DocumentException, IOException {
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

            // se for a pagar true, senao true
            if (is == true) {
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
                Paragraph subtitle = new Paragraph("Relatório de Contas a Pagar", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(20);
                document.add(subtitle);
            } else {
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
                Paragraph subtitle = new Paragraph("Relatório de Contas Pagas", subtitleFont);
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
                for (ContaPagar con : contasParaExportar) {
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
        this.contaParaPagar = null;
    }

    public boolean isDinheiroSelecionado() {
        return this.metodoSelecionado == Entidades.Enums.MetodoPagamento.DINHEIRO;
    }

    public void novo() {
        contaPagar = new ContaPagar();
    }

    public void excluir(ContaPagar est) {
        contaPagarFacade.remover(est);
    }

    public void editar(ContaPagar est) {
        this.contaPagar = est;
    }

    public ContaPagar getContaPagar() {
        return contaPagar;
    }

    public void setContaPagar(ContaPagar contaPagar) {
        this.contaPagar = contaPagar;
    }

    public ContaPagarFacade getContaPagarFacade() {
        return contaPagarFacade;
    }

    public void setContaPagarFacade(ContaPagarFacade contaReceberFacade) {
        this.contaPagarFacade = contaReceberFacade;
    }

    public List<ContaPagar> getListaContaPagar() {
        return contaPagarFacade.listaTodos();
    }

    public List<ContaPagar> getListaContaPagarReais() {
        return contaPagarFacade.listaTodosReais();

    }

    public List<ContaPagar> getListaContaPagarCanceladas() {
        return contaPagarFacade.listaTodosCanceladas();
    }

    public ContaPagar getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(ContaPagar contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public MetodoPagamento getMetodoSelecionado() {
        return metodoSelecionado;
    }

    public void setMetodoSelecionado(MetodoPagamento metodoSelecionado) {
        this.metodoSelecionado = metodoSelecionado;
    }

    public Conta getContaParaPagar() {
        return contaParaPagar;
    }

    public void setContaParaPagar(Conta contaParaPagar) {
        this.contaParaPagar = contaParaPagar;
    }

    public String getObsPagamento() {
        return obsPagamento;
    }

    public void setObsPagamento(String obsPagamento) {
        this.obsPagamento = obsPagamento;
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

    public Date getDataInicioPagas() {
        return dataInicioPagas;
    }

    public void setDataInicioPagas(Date dataInicioPagas) {
        this.dataInicioPagas = dataInicioPagas;
    }

    public Date getDataFimPagas() {
        return dataFimPagas;
    }

    public void setDataFimPagas(Date dataFimPagas) {
        this.dataFimPagas = dataFimPagas;
    }

}
