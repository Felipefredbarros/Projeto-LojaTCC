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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.bean.ViewScoped;

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

    public void salvar() {
        contaPagar.setStatus("ABERTA");
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

    public void prepararPagamento(ContaPagar conta) {
        System.out.println("aquiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
        this.contaSelecionada = conta;
        System.out.println("CONTA: " + contaSelecionada.getDescricao() + "contacuzaopreto:" + contaSelecionada.getStatus());
        this.metodoSelecionado = null; 
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

        String desc = "Pagamento Conta #" + cp.getId()
                + (cp.getDescricao() != null ? " - " + cp.getDescricao() : "");
        if (obsPagamento != null && !obsPagamento.trim().isEmpty()) {
            desc += " (" + obsPagamento.trim() + ")";
        }
        lanc.setDescricao(desc);

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

            String desc = "ESTORNO pagamento ContaPagar #" + cp.getId();
            if (cp.getDescricao() != null && !cp.getDescricao().trim().isEmpty()) {
                desc += " - " + cp.getDescricao();
            }

            reverso.setDescricao(desc);

            lancamentoFinanceiroFacade.salvar(reverso);
            cp.setStatus("ESTORNADA");
            contaPagarFacade.salvar(cp);
            recomputarSaldo(original.getConta());

        } else {
            cp.setStatus("CANCELADA");
            contaPagarFacade.salvar(cp);

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

}
