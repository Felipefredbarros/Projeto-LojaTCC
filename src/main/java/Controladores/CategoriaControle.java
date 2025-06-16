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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
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
        categoriaFacade.remover(est);
    }

    public void editar(Categoria est) {
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
