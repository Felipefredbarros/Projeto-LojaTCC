package Controladores;

import Converters.ConverterGenerico;
import Entidades.Conta;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoConta;
import Entidades.Enums.TipoLancamento;
import Entidades.LancamentoFinanceiro;
import Facade.ContaFacade;
import Facade.LancamentoFinanceiroFacade;
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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

@ManagedBean
@SessionScoped
public class ContaControle implements Serializable {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(PT_BR);

    private Conta conta = new Conta();
    private Conta contaSelecionada;
    private String filtroTipo;
    private Date[] filtroPeriodo;
    private Conta contaDestino;
    private Double valorTransferencia;
    private Date dataTransferencia;
    private String descricaoTransferencia;
    private Double valorRetirada;
    private Date dataRetirada;
    private String motivoRetirada;
    private String tipoMovimentacao;
    private Double valorMovimentacao;
    private Date dataMovimentacao;
    private String descricaoMovimentacao;
    private Long paramContaId;
    private FiltroEstorno filtroEstorno = FiltroEstorno.TODAS;
    private Date dataInicioRelatorio;
    private Date dataFimRelatorio;
    private String filtroTipoExtrato = "TODOS";

    private List<LancamentoFinanceiro> movimentacoesFiltradas;
    private List<LancamentoFinanceiro> movimentacoesDaConta;

    @EJB
    private ContaFacade contaFacade;
    @EJB
    private LancamentoFinanceiroFacade lancamentoFinanceiroFacade;

    private ConverterGenerico contaConverter;

    @PostConstruct
    public void init() {
        carregarMovimentacoes();
        filtroTipo = "";
        filtroPeriodo = null;
        movimentacoesDaConta = new ArrayList<>();
        tipoMovimentacao = "ENTRADA";
        dataMovimentacao = new Date();
        dataTransferencia = new Date();
        dataRetirada = new Date();
    }

    public void carregarMovimentacoes() {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            this.movimentacoesDaConta = Collections.emptyList();
            return;
        }
        this.movimentacoesDaConta = lancamentoFinanceiroFacade
                .buscarPorConta(contaSelecionada);
    }

    public void initDetalhe() {
        if (contaSelecionada == null || (paramContaId != null && !paramContaId.equals(contaSelecionada.getId()))) {
            if (paramContaId != null) {
                contaSelecionada = contaFacade.buscar(paramContaId);
                carregarMovimentacoes();
            }
        }
    }

    public String irParaDetalhe(Conta c) {
        this.contaSelecionada = c;
        if (contaSelecionada.getAtivo() == true) {
            carregarMovimentacoes();
            return "/Contas/contaDetalhe.xhtml?faces-redirect=true&contaId=" + c.getId();
        } else {
            carregarMovimentacoes();
            return "/Contas/contaDetalheInativa.xhtml?faces-redirect=true&contaId=" + c.getId();
        }
    }

    public ConverterGenerico getContaConverter() {
        if (contaConverter == null) {
            contaConverter = new ConverterGenerico(contaFacade);
        }
        return contaConverter;
    }

    public void novo() {
        conta = new Conta();
    }

    public void salvar() {
        conta.setDataCriacao(new Date());
        conta.setStatus("ATIVA");
        if (Boolean.TRUE.equals(conta.getIsCofre())) {
            conta.setTipoConta(TipoConta.COFRE);
        } else {
            conta.setTipoConta(TipoConta.BANCO);
        }

        if (conta.getNome() == null || conta.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da conta é obrigatório!");
        }
        conta.setSaldo(conta.getValorInicial());

        contaFacade.salvar(conta);
        conta = new Conta();
    }

    public void excluirOuInativar(Conta conta) {
        try {
            // Verifica se existem lançamentos financeiros para essa conta
            boolean temLancamentos = !lancamentoFinanceiroFacade.listarPorConta(conta).isEmpty();

            if (temLancamentos) {
                // Só inativa
                conta.setAtivo(false);
                conta.setStatus("INATIVA");
                contaFacade.salvar(conta);
                addMensagem(FacesMessage.SEVERITY_WARN, "Conta inativada pois possui lançamentos relacionados.");
            } else {
                // Pode excluir de vez
                contaFacade.remover(conta);
                addMensagem(FacesMessage.SEVERITY_INFO, "Conta removida com sucesso!");
            }

        } catch (Exception e) {
            addMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao excluir/inativar: " + e.getMessage());
        }
    }

    public void ativar(Conta conta) {
        conta.setAtivo(true);
        conta.setStatus("ATIVA");
        contaFacade.salvar(conta);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso",
                        "Conta ativada com sucesso!"));
    }

    public void transferir() {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Selecione a conta de origem.");
            return;
        }
        if (contaDestino == null || contaDestino.getId() == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Selecione a conta de destino.");
            return;
        }
        if (Objects.equals(contaSelecionada.getId(), contaDestino.getId())) {
            addMsg(FacesMessage.SEVERITY_WARN, "A conta de destino deve ser diferente da origem.");
            return;
        }
        if (valorTransferencia == null || valorTransferencia <= 0d) {
            addMsg(FacesMessage.SEVERITY_WARN, "Informe um valor de transferência válido.");
            return;
        }

        if (contaSelecionada.getTipoConta() == TipoConta.COFRE) {
            double saldoAtual = contaSelecionada.getSaldo() != null ? contaSelecionada.getSaldo() : 0.0;
            if (valorTransferencia > saldoAtual) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Saldo insuficiente no cofre para realizar a transferência. Saldo atual: R$ " + String.format("%.2f", saldoAtual));
                return;
            }
        }

        Date quando = (dataTransferencia != null) ? dataTransferencia : new Date();

        // Saída na origem
        LancamentoFinanceiro saida = new LancamentoFinanceiro();
        saida.setConta(contaSelecionada);
        saida.setTipo(TipoLancamento.SAIDA);
        saida.setValor(valorTransferencia);
        saida.setDataHora(quando);
        saida.setStatus(StatusLancamento.NORMAL);
        saida.setDescricao(!isBlank(descricaoTransferencia)
                ? "Transferência para " + contaDestino.getNome() + " - " + descricaoTransferencia
                : "Transferência para " + contaDestino.getNome());
        lancamentoFinanceiroFacade.salvar(saida);

        // Entrada no destino
        LancamentoFinanceiro entrada = new LancamentoFinanceiro();
        entrada.setConta(contaDestino);
        entrada.setTipo(TipoLancamento.ENTRADA);
        entrada.setValor(valorTransferencia);
        entrada.setDataHora(quando);
        entrada.setStatus(StatusLancamento.NORMAL);
        entrada.setDescricao(!isBlank(descricaoTransferencia)
                ? "Transferência de " + contaSelecionada.getNome() + " - " + descricaoTransferencia
                : "Transferência de " + contaSelecionada.getNome());
        lancamentoFinanceiroFacade.salvar(entrada); // <-- ESSENCIAL

        contaSelecionada.setSaldo((contaSelecionada.getSaldo() != null ? contaSelecionada.getSaldo() : 0.0) - valorTransferencia);
        contaDestino.setSaldo((contaDestino.getSaldo() != null ? contaDestino.getSaldo() : 0.0) + valorTransferencia);

        contaFacade.salvar(contaSelecionada);
        contaFacade.salvar(contaDestino);
        carregarMovimentacoes();

        limparTransferencia();
        addMsg(FacesMessage.SEVERITY_INFO, "Transferência realizada com sucesso.");
    }

    public void retirarDoCofre() {
        if (contaSelecionada == null || contaSelecionada.getTipoConta() != TipoConta.COFRE) {
            addMsg(FacesMessage.SEVERITY_WARN, "Esta ação é permitida apenas para o Cofre.");
            return;
        }
        if (valorRetirada == null || valorRetirada <= 0) {
            addMsg(FacesMessage.SEVERITY_WARN, "Informe um valor válido.");
            return;
        }
        if (contaSelecionada.getTipoConta() == TipoConta.COFRE) {
            double saldoAtual = contaSelecionada.getSaldo() != null ? contaSelecionada.getSaldo() : 0.0;
            if (valorRetirada > saldoAtual) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Saldo insuficiente no cofre para realizar a retirada. Saldo atual: R$ " + String.format("%.2f", saldoAtual));
                return;
            }
        }
        Date quando = (dataRetirada != null) ? dataRetirada : new Date();

        LancamentoFinanceiro saida = new LancamentoFinanceiro();
        saida.setConta(contaSelecionada);
        saida.setTipo(TipoLancamento.SAIDA);
        saida.setValor(valorRetirada);
        saida.setDataHora(quando);
        saida.setStatus(StatusLancamento.NORMAL);
        saida.setDescricao(!isBlank(motivoRetirada)
                ? "Retirada do cofre - " + motivoRetirada
                : "Retirada do cofre");
        lancamentoFinanceiroFacade.salvar(saida);

        atualizarSaldoMaterializado(contaSelecionada);

        carregarMovimentacoes();

        limparRetirada();
        addMsg(FacesMessage.SEVERITY_INFO, "Retirada registrada.");
    }

    public void adicionarNoCofre() {
        if (contaSelecionada == null || contaSelecionada.getTipoConta() != TipoConta.COFRE) {
            addMsg(FacesMessage.SEVERITY_WARN, "Esta ação é permitida apenas para o Cofre.");
            return;
        }
        if (valorMovimentacao == null || valorMovimentacao <= 0) {
            addMsg(FacesMessage.SEVERITY_WARN, "Informe um valor válido.");
            return;
        }

        Date quando = (dataRetirada != null) ? dataRetirada : new Date();

        LancamentoFinanceiro entrada = new LancamentoFinanceiro();
        entrada.setConta(contaSelecionada);
        entrada.setTipo(TipoLancamento.ENTRADA);
        entrada.setValor(valorMovimentacao);
        entrada.setDataHora(quando);
        entrada.setStatus(StatusLancamento.NORMAL);
        entrada.setDescricao(!isBlank(motivoRetirada)
                ? "Entrada no cofre - " + motivoRetirada
                : "Entrada no cofre");
        lancamentoFinanceiroFacade.salvar(entrada);

        atualizarSaldoMaterializado(contaSelecionada);

        carregarMovimentacoes();

        limparRetirada();
        addMsg(FacesMessage.SEVERITY_INFO, "Entrada registrada.");
    }

    public void salvarMovimentacao() {
        if (valorMovimentacao == null || valorMovimentacao <= 0) {
            addMsg(FacesMessage.SEVERITY_WARN, "Informe um valor válido.");
            return;
        }

        if (contaSelecionada.getTipoConta() == TipoConta.COFRE) {
            double saldoAtual = contaSelecionada.getSaldo() != null ? contaSelecionada.getSaldo() : 0.0;
            if (valorMovimentacao > saldoAtual) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Saldo insuficiente no cofre para realizar a movimentação. Saldo atual: R$ " + String.format("%.2f", saldoAtual));
                return;
            }
        }
        TipoLancamento tipo = "SAIDA".equalsIgnoreCase(tipoMovimentacao)
                ? TipoLancamento.SAIDA : TipoLancamento.ENTRADA;

        LancamentoFinanceiro mov = new LancamentoFinanceiro();
        mov.setConta(contaSelecionada);
        mov.setTipo(tipo);
        mov.setValor(valorMovimentacao);
        mov.setDataHora((dataMovimentacao != null) ? dataMovimentacao : new Date());
        mov.setStatus(StatusLancamento.NORMAL);
        mov.setDescricao(!isBlank(descricaoMovimentacao)
                ? descricaoMovimentacao
                : (tipo == TipoLancamento.ENTRADA ? "Entrada manual" : "Saída manual"));

        lancamentoFinanceiroFacade.salvar(mov);
        atualizarSaldoMaterializado(contaSelecionada);

        carregarMovimentacoes();

        limparMovimentacaoGenerica();
        addMsg(FacesMessage.SEVERITY_INFO, "Movimentação salva.");
    }

    public enum FiltroEstorno {
        TODAS,
        SEM_ESTORNOS,
        APENAS_ESTORNOS
    }

    public boolean isEstorno(LancamentoFinanceiro l) {
        if (l == null) {
            return false;
        }
        if (l.getStatus() == Entidades.Enums.StatusLancamento.ESTORNADO) {
            return true;
        }
        String d = l.getDescricao();
        return d != null && d.trim().toLowerCase().startsWith("estorno");
    }

    public List<LancamentoFinanceiro> getMovimentacoesFiltradas() {
        if (contaSelecionada == null || movimentacoesDaConta == null) {
            return Collections.emptyList();
        }
        FiltroEstorno filtroAtual = (filtroEstorno != null) ? filtroEstorno : FiltroEstorno.TODAS;

        switch (filtroAtual) { 
            case SEM_ESTORNOS:
                return movimentacoesDaConta.stream()
                        .filter(l -> !isEstorno(l))
                        .collect(Collectors.toList());
            case APENAS_ESTORNOS:
                return movimentacoesDaConta.stream()
                        .filter(this::isEstorno)
                        .collect(Collectors.toList());
            default: 
                return new ArrayList<>(movimentacoesDaConta); 
        }
    }

    public List<LancamentoFinanceiro> getMovimentacoesDaConta() {
        return movimentacoesDaConta;
    }

    public List<Conta> contasTransferiveis(Conta origem) {
        List<Conta> todas;
        try {
            todas = contaFacade != null ? contaFacade.listaContaAtivo() : Collections.emptyList();
        } catch (Exception e) {
            todas = Collections.emptyList();
        }
        if (todas == null) {
            todas = Collections.emptyList();
        }

        if (origem == null || origem.getId() == null) {
            return new ArrayList<>(todas);
        }

        Long idOrigem = origem.getId();
        List<Conta> filtradas = new ArrayList<>();
        for (Conta c : todas) {
            if (c != null) {
                Long idC = c.getId();
                if (!Objects.equals(idC, idOrigem)) {
                    filtradas.add(c);
                }
            }
        }
        return filtradas;
    }

    public List<Conta> getContasBancoAtivas() {
        List<Conta> todas = getListaContaAtiva();
        List<Conta> out = new ArrayList<>();
        if (todas != null) {
            for (Conta c : todas) {
                if (c != null && c.getTipoConta() == Entidades.Enums.TipoConta.BANCO) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    public List<Conta> getContasCofreAtivas() {
        List<Conta> todas = getListaContaAtiva();
        List<Conta> out = new ArrayList<>();
        if (todas != null) {
            for (Conta c : todas) {
                if (c != null && c.getTipoConta() == Entidades.Enums.TipoConta.COFRE) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    public List<Conta> getListaContaAtiva() {
        return contaFacade.listaContaAtivo();
    }

    public List<Conta> getListaContaInativa() {
        return contaFacade.listaContaInativo();
    }

    public Long getParamContaId() {
        return paramContaId;
    }

    public void setParamContaId(Long paramContaId) {
        this.paramContaId = paramContaId;
    }

    //metodos de somar o saldo
    public Double somarEntradas(Conta conta) {
        return somaPorTipo(conta, TipoLancamento.ENTRADA, null, null);
    }

    public Double somarSaidas(Conta conta) {
        return somaPorTipo(conta, TipoLancamento.SAIDA, null, null);
    }

    public Double totalEntradasMes(Conta conta) {
        Date[] mes = boundariesDoMesAtual();
        return somaPorTipo(conta, TipoLancamento.ENTRADA, mes[0], mes[1]);
    }

    public Double totalSaidasMes(Conta conta) {
        Date[] mes = boundariesDoMesAtual();
        return somaPorTipo(conta, TipoLancamento.SAIDA, mes[0], mes[1]);
    }

    private Double somaPorTipo(Conta conta, TipoLancamento tipo, Date ini, Date fim) {
        if (conta == null || conta.getId() == null) {
            return 0.0;
        }
        Double soma = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, tipo, ini, fim);
        return (soma != null) ? soma : 0.0;
    }

    //metodo de atualizar o saldo
    private void atualizarSaldoMaterializado(Conta conta) {
        Double inicial = conta.getValorInicial() != null ? conta.getValorInicial() : 0.0;
        Double entradas = somaPorTipo(conta, TipoLancamento.ENTRADA, null, null);
        Double saidas = somaPorTipo(conta, TipoLancamento.SAIDA, null, null);

        if (entradas == null) {
            entradas = 0.0;
        }
        if (saidas == null) {
            saidas = 0.0;
        }

        Double saldo = inicial + entradas - saidas;   // <<--- aqui está a diferença
        conta.setSaldo(saldo);
        contaFacade.salvar(conta);
    }

    public LancamentoFinanceiro ultimaMovimentacao(Conta conta) {
        if (conta == null || conta.getId() == null) {
            return null;
        }
        return lancamentoFinanceiroFacade.buscarUltimoPorConta(conta);
    }

    //helpers
    private static Date atStartOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date atEndOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private static String vazioComoNull(String s) {
        return isBlank(s) ? null : s;
    }

    private static Date[] boundariesDoMesAtual() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        Date ini = atStartOfDay(c.getTime());
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date fim = atEndOfDay(c.getTime());
        return new Date[]{ini, fim};
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void addMsg(FacesMessage.Severity s, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, msg, null));
    }

    //metodos de limpar as coisas
    public void limparTransferencia() {
        contaDestino = null;
        valorTransferencia = null;
        descricaoTransferencia = null;
        dataTransferencia = new Date();
    }

    private void limparRetirada() {
        valorRetirada = null;
        motivoRetirada = null;
        dataRetirada = new Date();
    }

    private void limparMovimentacaoGenerica() {
        valorMovimentacao = null;
        tipoMovimentacao = "ENTRADA";
        descricaoMovimentacao = null;
        dataMovimentacao = new Date();
    }

    private void addMensagem(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, msg, null));
    }

    public void exportarPDFContasAtivas() throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_geral_contas.pdf");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20); // Paisagem
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        Locale ptBr = new Locale("pt", "BR");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(ptBr);
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr);
        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();
        java.util.function.Function<Number, String> sm = num -> (num == null) ? moeda.format(0) : moeda.format(num);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Relatório Geral de Contas Ativas", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            List<Conta> contasAtivas = contaFacade.listaContaAtivo();

            if (contasAtivas == null || contasAtivas.isEmpty()) {
                document.add(new Paragraph("Nenhuma conta ativa encontrada.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{0.8f, 3f, 1f, 3f, 1.5f, 1.5f});

                table.addCell(createHeaderCell("ID"));
                table.addCell(createHeaderCell("Nome da Conta"));
                table.addCell(createHeaderCell("Tipo"));
                table.addCell(createHeaderCell("Banco/Agência/Conta"));
                table.addCell(createHeaderCell("Vl. Inicial"));
                table.addCell(createHeaderCell("Saldo Atual"));

                double saldoTotalGeral = 0.0;

                for (Conta c : contasAtivas) {
                    table.addCell(createDataCell(s.apply(c.getId()), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(s.apply(c.getNome()), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(c.getTipoConta()), Element.ALIGN_CENTER));

                    String bancoInfo = "-";
                    if (c.getTipoConta() == TipoConta.BANCO) {
                        bancoInfo = String.format("Banco: %s / Ag: %s / CC: %s", s.apply(c.getBanco()), s.apply(c.getAgencia()), s.apply(c.getConta()));
                    } else if (c.getTipoConta() == TipoConta.COFRE) {
                        bancoInfo = "Cofre Físico";
                    }
                    table.addCell(createDataCell(bancoInfo, Element.ALIGN_LEFT));

                    table.addCell(createDataCell(sm.apply(c.getValorInicial()), Element.ALIGN_RIGHT));
                    table.addCell(createDataCell(sm.apply(c.getSaldo()), Element.ALIGN_RIGHT));

                    saldoTotalGeral += (c.getSaldo() != null ? c.getSaldo() : 0.0);
                }
                document.add(table);

                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph totalPar = new Paragraph("Saldo Total Geral (Contas Ativas): " + sm.apply(saldoTotalGeral), totalFont);
                totalPar.setAlignment(Element.ALIGN_RIGHT);
                totalPar.setSpacingBefore(15);
                document.add(totalPar);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(Math.max(15, currentY - document.bottomMargin() - footer.getTotalLeading()));
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    public void exportarPDFExtratoContaSelecionada() throws DocumentException, IOException {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Nenhuma conta selecionada para gerar o extrato.");
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=extrato_" + contaSelecionada.getNome().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");

        Document document = new Document(PageSize.A4, 30, 30, 40, 30);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        Locale ptBr = new Locale("pt", "BR");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(ptBr);
        DateFormat dfExtrato = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr);
        DateFormat dfRelatorio = DateFormat.getDateInstance(DateFormat.SHORT, ptBr);
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr);
        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "" : obj.toString();
        java.util.function.Function<Number, String> sm = num -> (num == null) ? "" : moeda.format(num);
        java.util.function.Function<Date, String> sd = dt -> (dt == null) ? "__/__/____" : dfRelatorio.format(dt);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Extrato de Conta", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            Font detailHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.BLACK);
            Font detailContentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            PdfPTable contaInfoTable = new PdfPTable(2);
            contaInfoTable.setWidthPercentage(100);
            contaInfoTable.setWidths(new float[]{1f, 3f});
            contaInfoTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            contaInfoTable.setSpacingAfter(10);
            contaInfoTable.addCell(new Phrase("Conta:", detailHeaderFont));
            contaInfoTable.addCell(new Phrase(s.apply(contaSelecionada.getNome()), detailContentFont));
            contaInfoTable.addCell(new Phrase("Tipo:", detailHeaderFont));
            contaInfoTable.addCell(new Phrase(s.apply(contaSelecionada.getTipoConta()), detailContentFont));
            if (contaSelecionada.getTipoConta() == TipoConta.BANCO) {
                contaInfoTable.addCell(new Phrase("Banco:", detailHeaderFont));
                contaInfoTable.addCell(new Phrase(s.apply(contaSelecionada.getBanco()), detailContentFont));
                contaInfoTable.addCell(new Phrase("Agência:", detailHeaderFont));
                contaInfoTable.addCell(new Phrase(s.apply(contaSelecionada.getAgencia()), detailContentFont));
                contaInfoTable.addCell(new Phrase("Conta Nº:", detailHeaderFont));
                contaInfoTable.addCell(new Phrase(s.apply(contaSelecionada.getConta()), detailContentFont));
            }
            contaInfoTable.addCell(new Phrase("Período:", detailHeaderFont));
            contaInfoTable.addCell(new Phrase(sd.apply(dataInicioRelatorio) + " a " + sd.apply(dataFimRelatorio), detailContentFont));
            contaInfoTable.addCell(new Phrase("Filtro Tipo:", detailHeaderFont));
            contaInfoTable.addCell(new Phrase(filtroTipoExtrato != null ? filtroTipoExtrato.toUpperCase() : "TODOS", detailContentFont)); // Mostra o filtro
            document.add(contaInfoTable);

            List<LancamentoFinanceiro> todosLancamentos = lancamentoFinanceiroFacade.buscarPorContaEPeriodoOrdenado(contaSelecionada, dataInicioRelatorio, dataFimRelatorio);

            List<LancamentoFinanceiro> lancamentosFiltrados;
            if ("ENTRADA".equals(filtroTipoExtrato)) {
                lancamentosFiltrados = todosLancamentos.stream()
                        .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                        .collect(Collectors.toList());
            } else if ("SAIDA".equals(filtroTipoExtrato)) {
                lancamentosFiltrados = todosLancamentos.stream()
                        .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                        .collect(Collectors.toList());
            } else {
                lancamentosFiltrados = todosLancamentos;
            }

            Double saldoAnterior = lancamentoFinanceiroFacade.calcularSaldoAnteriorAData(contaSelecionada, dataInicioRelatorio);
            Double saldoCorrente = saldoAnterior;

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4f, 1.5f, 1.5f, 1.8f});
            table.setHeaderRows(1);

            table.addCell(createHeaderCell("Data/Hora"));
            table.addCell(createHeaderCell("Descrição"));
            table.addCell(createHeaderCell("Entrada (R$)"));
            table.addCell(createHeaderCell("Saída (R$)"));
            table.addCell(createHeaderCell("Saldo (R$)"));

            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.DARK_GRAY);
            Phrase saldoAnteriorTextoPhrase = new Phrase("Saldo Anterior", italicFont);
            PdfPCell saldoAntCell = new PdfPCell(saldoAnteriorTextoPhrase);
            saldoAntCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            saldoAntCell.setColspan(4);
            saldoAntCell.setBorder(PdfPCell.BOTTOM);
            saldoAntCell.setBorderWidthBottom(0.5f);
            saldoAntCell.setPaddingBottom(4);
            table.addCell(saldoAntCell);
            Phrase saldoAnteriorValorPhrase = new Phrase(moeda(saldoAnterior), italicFont);
            PdfPCell saldoAntValorCell = new PdfPCell(saldoAnteriorValorPhrase);
            saldoAntValorCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            saldoAntValorCell.setBorder(PdfPCell.BOTTOM);
            saldoAntValorCell.setBorderWidthBottom(0.5f);
            saldoAntValorCell.setPaddingBottom(4);
            table.addCell(saldoAntValorCell);

            if (lancamentosFiltrados == null || lancamentosFiltrados.isEmpty()) {
                PdfPCell noMovCell = createDataCell("Nenhuma movimentação encontrada para os filtros selecionados.", Element.ALIGN_CENTER);
                noMovCell.setColspan(5);
                noMovCell.setPadding(10);
                table.addCell(noMovCell);
            } else {
                for (LancamentoFinanceiro mov : lancamentosFiltrados) {
                    Double valorEntrada = null;
                    Double valorSaida = null;
                    Double valorMovimento = (mov.getValor() != null ? mov.getValor() : 0.0);
                    boolean isEstornoMarcado = (mov.getStatus() == StatusLancamento.ESTORNADO);
                    boolean isLancamentoDeEstorno = (mov.getDescricao() != null && mov.getDescricao().toUpperCase().startsWith("ESTORNO"));
                    boolean ignorarNoSaldo = isEstornoMarcado || isLancamentoDeEstorno;

                    if (!ignorarNoSaldo) {
                        if (mov.getTipo() == TipoLancamento.ENTRADA) {
                            valorEntrada = valorMovimento;
                            saldoCorrente += valorMovimento;
                        } else if (mov.getTipo() == TipoLancamento.SAIDA) {
                            valorSaida = valorMovimento;
                            saldoCorrente -= valorMovimento;
                        }
                    } else {
                        if (mov.getTipo() == TipoLancamento.ENTRADA) {
                            valorEntrada = valorMovimento;
                        } else if (mov.getTipo() == TipoLancamento.SAIDA) {
                            valorSaida = valorMovimento;
                        }
                    }

                    table.addCell(createDataCell(dfExtrato.format(mov.getDataHora()), Element.ALIGN_CENTER));
                    String descricaoCompleta = s.apply(mov.getDescricao());
                    if (isEstornoMarcado) {
                        descricaoCompleta += " (ESTORNADO)";
                    }
                    table.addCell(createDataCell(descricaoCompleta, Element.ALIGN_LEFT));
                    table.addCell(createDataCell(sm.apply(valorEntrada), Element.ALIGN_RIGHT));
                    table.addCell(createDataCell(sm.apply(valorSaida), Element.ALIGN_RIGHT));
                    table.addCell(createDataCell(moeda(saldoCorrente), Element.ALIGN_RIGHT));
                }
            }
            document.add(table);

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(Math.max(15, currentY - document.bottomMargin() - footer.getTotalLeading()));
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    public void limparFiltroExtrato() {
        this.dataInicioRelatorio = null;
        this.dataFimRelatorio = null;
        this.filtroTipoExtrato = "TODOS"; // Volta para o padrão
    }

    private PdfPCell createHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(0, 51, 102));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthTop(0.5f);
        cell.setBorderWidthBottom(0.5f);

        return cell;
    }

    private String moeda(Double valor) {
        if (valor == null) {
            // Se o valor for nulo, retorna zero formatado
            return CURRENCY_FORMATTER.format(0.0);
        }
        // Formata o valor recebido
        return CURRENCY_FORMATTER.format(valor);
    }

    // get e set
    public void selecionarConta(Conta conta) {
        this.contaSelecionada = conta;
    }

    public Conta getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(Conta contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    public Date[] getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(Date[] filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public void setMovimentacoesFiltradas(List<LancamentoFinanceiro> movimentacoesFiltradas) {
        this.movimentacoesFiltradas = movimentacoesFiltradas;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }

    public Double getValorTransferencia() {
        return valorTransferencia;
    }

    public void setValorTransferencia(Double valorTransferencia) {
        this.valorTransferencia = valorTransferencia;
    }

    public Date getDataTransferencia() {
        return dataTransferencia;
    }

    public void setDataTransferencia(Date dataTransferencia) {
        this.dataTransferencia = dataTransferencia;
    }

    public String getDescricaoTransferencia() {
        return descricaoTransferencia;
    }

    public void setDescricaoTransferencia(String descricaoTransferencia) {
        this.descricaoTransferencia = descricaoTransferencia;
    }

    public Double getValorRetirada() {
        return valorRetirada;
    }

    public void setValorRetirada(Double valorRetirada) {
        this.valorRetirada = valorRetirada;
    }

    public Date getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(Date dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public String getMotivoRetirada() {
        return motivoRetirada;
    }

    public void setMotivoRetirada(String motivoRetirada) {
        this.motivoRetirada = motivoRetirada;
    }

    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public Double getValorMovimentacao() {
        return valorMovimentacao;
    }

    public void setValorMovimentacao(Double valorMovimentacao) {
        this.valorMovimentacao = valorMovimentacao;
    }

    public Date getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(Date dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public String getDescricaoMovimentacao() {
        return descricaoMovimentacao;
    }

    public void setDescricaoMovimentacao(String descricaoMovimentacao) {
        this.descricaoMovimentacao = descricaoMovimentacao;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public ContaFacade getContaFacade() {
        return contaFacade;
    }

    public void setContaFacade(ContaFacade contaFacade) {
        this.contaFacade = contaFacade;
    }

    public void setMovimentacoesDaConta(List<LancamentoFinanceiro> movimentacoesDaConta) {
        this.movimentacoesDaConta = movimentacoesDaConta;
    }

    public FiltroEstorno getFiltroEstorno() {
        return filtroEstorno;
    }

    public void setFiltroEstorno(FiltroEstorno filtroEstorno) {
        this.filtroEstorno = filtroEstorno;
    }

    public Date getDataInicioRelatorio() {
        return dataInicioRelatorio;
    }

    public void setDataInicioRelatorio(Date dataInicioRelatorio) {
        this.dataInicioRelatorio = dataInicioRelatorio;
    }

    public Date getDataFimRelatorio() {
        return dataFimRelatorio;
    }

    public void setDataFimRelatorio(Date dataFimRelatorio) {
        this.dataFimRelatorio = dataFimRelatorio;
    }

    public String getFiltroTipoExtrato() {
        return filtroTipoExtrato;
    }

    public void setFiltroTipoExtrato(String filtroTipoExtrato) {
        this.filtroTipoExtrato = filtroTipoExtrato;
    }

}
