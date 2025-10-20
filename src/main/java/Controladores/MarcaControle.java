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
                            "Erro", "Esta marca ja tem um produto registrado e não pode ser excluida"));
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

    public List<Marca> getListaMarcas() {
        return marcaFacade.listaTodos();
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
