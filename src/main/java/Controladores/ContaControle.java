package Controladores;

import Converters.ConverterGenerico;
import Entidades.Conta;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoConta;
import Entidades.Enums.TipoLancamento;
import Entidades.LancamentoFinanceiro;
import Facade.ContaFacade;
import Facade.LancamentoFinanceiroFacade;

import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class ContaControle implements Serializable {

    // Estado
    private Conta contaSelecionada;

    // Filtros tabela
    private String filtroTipo;              // "ENTRADA" | "SAIDA" | null
    private Date[] filtroPeriodo;           // [ini, fim]
    private List<LancamentoFinanceiro> movimentacoesFiltradas;

    // Transferência
    private Conta contaDestino;
    private Double valorTransferencia;
    private Date dataTransferencia;
    private String descricaoTransferencia;

    // Retirada (Cofre)
    private Double valorRetirada;
    private Date dataRetirada;
    private String motivoRetirada;

    // Movimentação genérica
    private String tipoMovimentacao;        // "ENTRADA" | "SAIDA"
    private Double valorMovimentacao;
    private Date dataMovimentacao;
    private String descricaoMovimentacao;

    @EJB
    private ContaFacade contaFacade;

    @EJB
    private LancamentoFinanceiroFacade lancamentoFinanceiroFacade;

    private Conta conta = new Conta();

    private ConverterGenerico contaConverter;

    private List<LancamentoFinanceiro> movimentacoesDaConta;

    private Long paramContaId;

    private FiltroEstorno filtroEstorno = FiltroEstorno.TODAS;

    public enum FiltroEstorno {
        TODAS, // mostra tudo
        SEM_ESTORNOS, // só lançamentos normais
        APENAS_ESTORNOS // apenas estornados + reversos
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
        if (movimentacoesDaConta == null) {
            return java.util.Collections.emptyList();
        }
        switch (filtroEstorno) {
            case SEM_ESTORNOS:
                return movimentacoesDaConta.stream()
                        .filter(l -> !isEstorno(l))
                        .collect(java.util.stream.Collectors.toList());
            case APENAS_ESTORNOS:
                return movimentacoesDaConta.stream()
                        .filter(this::isEstorno)
                        .collect(java.util.stream.Collectors.toList());
            default:
                return movimentacoesDaConta;
        }
    }

    public void initDetalhe() {
        if (contaSelecionada == null || (paramContaId != null && !paramContaId.equals(contaSelecionada.getId()))) {
            if (paramContaId != null) {
                contaSelecionada = contaFacade.buscar(paramContaId);
                carregarMovimentacoes();
            }
        }
    }

    public void carregarMovimentacoes() {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            this.movimentacoesDaConta = Collections.emptyList();
            return;
        }
        this.movimentacoesDaConta = lancamentoFinanceiroFacade
                .buscarPorConta(contaSelecionada);
    }

    public List<LancamentoFinanceiro> getMovimentacoesDaConta() {
        return movimentacoesDaConta;
    }

    public Long getParamContaId() {
        return paramContaId;
    }

    public void setParamContaId(Long paramContaId) {
        this.paramContaId = paramContaId;
    }

    @PostConstruct
    public void init() {
        carregarMovimentacoes();
        filtroTipo = "";
        filtroPeriodo = null;
        movimentacoesFiltradas = new ArrayList<>();
        tipoMovimentacao = "ENTRADA";
        dataMovimentacao = new Date();
        dataTransferencia = new Date();
        dataRetirada = new Date();
    }

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

    public LancamentoFinanceiro ultimaMovimentacao(Conta conta) {
        if (conta == null || conta.getId() == null) {
            return null;
        }
        return lancamentoFinanceiroFacade.buscarUltimoPorConta(conta);
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

    /* ====================== Suporte a Combos/Filtros ==================== */
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

        // Se não há origem definida ainda, apenas retorne todas (ou vazio, se preferir)
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


    /* ============================ Helpers =============================== */
    private Double somaPorTipo(Conta conta, TipoLancamento tipo, Date ini, Date fim) {
        if (conta == null || conta.getId() == null) {
            return 0.0;
        }
        Double soma = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, tipo, ini, fim);
        return (soma != null) ? soma : 0.0;
    }

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

    public List<Conta> getListaContaAtiva() {
        return contaFacade.listaContaAtivo();
    }

    public List<Conta> getListaContaInativa() {
        return contaFacade.listaContaInativo();
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

    private void addMensagem(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, msg, null));
    }

    public void selecionarConta(Conta conta) {
        this.contaSelecionada = conta;
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

    /* ============================ Getters/Setters ======================= */
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

    public ConverterGenerico getContaConverter() {
        if (contaConverter == null) {
            contaConverter = new ConverterGenerico(contaFacade);
        }
        return contaConverter;
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

}
