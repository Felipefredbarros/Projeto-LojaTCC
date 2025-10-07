/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.ContaPagar;
import Entidades.ContaReceber;
import Facade.ContaReceberFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

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

    private List<ContaReceber> listaContaRecebers;

    private List<ContaReceber> listaContas = new ArrayList<>();


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

    public void contaReceberItem(ContaReceber contaSelecionada) {
        contaReceberFacade.receberConta(contaSelecionada);
    }
    public void cancelarConta(ContaReceber contaSelecionada){
        if ("RECEBIDA".equals(contaSelecionada.getStatus())) {
                contaSelecionada.setStatus("ESTORNADA");
            } else {
                contaSelecionada.setStatus("CANCELADA");
            }
        contaReceberFacade.salvar(contaSelecionada);
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

}
