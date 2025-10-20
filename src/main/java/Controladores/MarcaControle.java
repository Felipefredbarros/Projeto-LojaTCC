/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Marca;
import Facade.MarcaFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author felip
 */
@Named("marcaControle")
@ViewScoped
public class MarcaControle implements Serializable {

    private Marca marca = new Marca();
    private boolean mostrandoAtivas = true;

    @EJB
    private MarcaFacade marcaFacade;

    public void salvar() {
        marcaFacade.salvar(marca);
        marca = new Marca();
    }

    public void novo() {
        marca = new Marca();
    }

    public void excluir(Marca est) {
        if (marcaFacade.categoriaTemProduto(est.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Marca Inativada"));
            est.setAtivo(false);
            marcaFacade.salvar(est);
            return;
        }
        marcaFacade.remover(est);
    }

    public void editar(Marca est) {
        if (marcaFacade.categoriaTemProduto(est.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Esta marca ja tem um produto registrado e não pode ser editado"));
            return;
        }
        this.marca = est;
    }

    public boolean isMostrandoAtivas() {
        return mostrandoAtivas;
    }

    public void setMostrandoAtivas(boolean mostrandoAtivas) {
        this.mostrandoAtivas = mostrandoAtivas;
    }
    
    public void toggleLista() {
        this.mostrandoAtivas = !this.mostrandoAtivas;
    }
    
    public List<Marca> getListaMarcasAqui() {
    if (mostrandoAtivas) {
        return marcaFacade.listaMarcaAtiva(); 
    } else {
        return marcaFacade.listaMarcaInativa(); 
    }
}

    public List<Marca> getListaMarcas() {
        return marcaFacade.listaTodos();
    }

    public List<Marca> getListaMarcasAtivas() {
        return marcaFacade.listaMarcaAtiva();
    }

    public List<Marca> getListaMarcasInativas() {
        return marcaFacade.listaMarcaInativa();
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public MarcaFacade getMarcaFacade() {
        return marcaFacade;
    }

    public void setMarcaFacade(MarcaFacade marcaFacade) {
        this.marcaFacade = marcaFacade;
    }

}
