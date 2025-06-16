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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class MarcaControle implements Serializable{

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
        marcaFacade.remover(est);
    }

    public void editar(Marca est) {
        this.marca = est;
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
    
    public List<Marca> getListaMarcas() {
        return marcaFacade.listaTodos();
    }
    
    
}
