/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Categoria;
import Facade.CategoriaFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author felip
 */
@Named("categoriaControle")
@ViewScoped
public class CategoriaControle implements Serializable{

    private Categoria categoria = new Categoria();
    @EJB
    private CategoriaFacade categoriaFacade;

    public void salvar() {
        categoriaFacade.salvar(categoria);
        categoria = new Categoria();
    }

    public void novo() {
        categoria = new Categoria();
    }

    public void excluir(Categoria est) {
        if (categoriaFacade.categoriaTemProduto(est.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Esta categoria ja tem um produto registrado e não pode ser excluida"));
            return;
        }
        categoriaFacade.remover(est);
    }

    public void editar(Categoria est) {
        if (categoriaFacade.categoriaTemProduto(est.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Esta categoria ja tem um produto registrado e não pode ser editado"));
            return;
        }
        this.categoria = est;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public CategoriaFacade getCategoriaFacade() {
        return categoriaFacade;
    }

    public void setCategoriaFacade(CategoriaFacade categoriaFacade) {
        this.categoriaFacade = categoriaFacade;
    }
    
    public List<Categoria> getListaCategorias() {
        return categoriaFacade.listaTodos();
    }
}
