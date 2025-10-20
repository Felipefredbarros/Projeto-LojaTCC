/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Conta;
import Entidades.ContaPagar;
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
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@Named("contaPagarControle")
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
        exportarPDF(contasParaExportar, "Relatório de Contas Pagas", dataInicioPagas, dataFimPagas);
    }

    public void exportarPDFFiltradoPagar() throws DocumentException, IOException {
        List<ContaPagar> contasParaExportar = contaPagarFacade.buscarPorFiltrosPagar(dataInicio, dataFim);
        exportarPDF(contasParaExportar, "Relatório de Contas a Pagar", dataInicio, dataFim);
    }

    public void exportarPDF(List<ContaPagar> contasParaExportar, String tituloRelatorio, Date dataIni, Date dataFim) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_contas_pagar.pdf");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        java.text.DateFormat dfRelatorio = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.text.DateFormat dfCompleto = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        java.util.function.Function<Object, String> s = o -> o == null ? "-" : o.toString();
        java.util.function.Function<java.util.Date, String> sd = d -> d == null ? "-" : dfRelatorio.format(d);
        java.util.function.Function<java.lang.Number, String> sm = n -> n == null ? moeda.format(0) : moeda.format(n);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph(tituloRelatorio, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            String periodoStr = "Período: ";
            if (dataIni == null && dataFim == null) {
                periodoStr += "Todos os registros";
            } else {
                periodoStr += (dataIni != null ? sd.apply(dataIni) : "__/__/____") + " até " + (dataFim != null ? sd.apply(dataFim) : "__/__/____");
            }
            Paragraph periodo = new Paragraph(periodoStr, periodFont);
            periodo.setAlignment(Element.ALIGN_CENTER);
            periodo.setSpacingAfter(20);
            document.add(periodo);

            if (contasParaExportar == null || contasParaExportar.isEmpty()) {
                document.add(new Paragraph("Nenhum registro encontrado para o período.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {

                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{0.8f, 3.2f, 1.5f, 1.5f, 1.5f, 1.0f, 1.5f});

                table.addCell(createHeaderCell("ID"));
                table.addCell(createHeaderCell("Descrição"));
                table.addCell(createHeaderCell("Data Criação"));
                table.addCell(createHeaderCell("Data Vencimento"));
                table.addCell(createHeaderCell("Data Pagamento")); // Nome corrigido
                table.addCell(createHeaderCell("Status"));
                table.addCell(createHeaderCell("Valor"));

                double valorTotal = 0.0;

                for (ContaPagar con : contasParaExportar) {

                    table.addCell(createDataCell(s.apply(con.getId()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(s.apply(con.getDescricao()), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(sd.apply(con.getDataCriação()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(sd.apply(con.getDataVencimento()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(sd.apply(con.getDataRecebimento()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(s.apply(con.getStatus()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(sm.apply(con.getValor()), Element.ALIGN_RIGHT));

                    if (con.getValor() != null) {
                        valorTotal += con.getValor();
                    }
                }

                document.add(table);

                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph totalPar = new Paragraph("Valor Total: " + sm.apply(valorTotal), totalFont);
                totalPar.setAlignment(Element.ALIGN_RIGHT);
                totalPar.setSpacingBefore(15);
                document.add(totalPar);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

        } finally {
            document.close();
            writer.flush();
            writer.close();
            facesContext.responseComplete();
        }
    }

    private PdfPCell createHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(0, 51, 102)); // Azul escuro
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
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
