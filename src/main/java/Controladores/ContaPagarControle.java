/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.ContaPagar;
import Entidades.Enums.MetodoPagamento;
import Facade.ContaPagarFacade;
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
public class ContaPagarControle implements Serializable {

    private ContaPagar contaPagar = new ContaPagar();

    @EJB
    private ContaPagarFacade contaPagarFacade;

    private List<ContaPagar> listaContaPagar;

    private List<ContaPagar> listaContas = new ArrayList<>();

    private ContaPagar contaSelecionada;
    private MetodoPagamento metodoSelecionado;

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
        this.contaSelecionada = conta;
        this.metodoSelecionado = null; // limpa seleção anterior
    }

    public void confirmarPagamento() {
        if (contaSelecionada != null && metodoSelecionado != null) {
            contaSelecionada.setMetodoPagamento(metodoSelecionado);
            contaPagarFacade.pagarConta(contaSelecionada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Conta paga via " + metodoSelecionado.getDescricao(), null));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Selecione um método de pagamento!", null));
        }
    }

    public MetodoPagamento[] getMetodosPagamento() {
        return MetodoPagamento.values();
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

}
