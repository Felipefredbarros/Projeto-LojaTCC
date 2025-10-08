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
import Facade.ContaFacade;
import Facade.ContaReceberFacade;
import Facade.LancamentoFinanceiroFacade;
import Utilitario.FinanceDesc;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

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

    private String obsRecebimento;

    public void salvar() {
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

}
