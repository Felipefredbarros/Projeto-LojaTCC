/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Converters.ConverterGenerico;
import Entidades.Pessoa;
import Entidades.Usuario;
import Facade.PessoaFacade;
import Facade.UsuarioFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class UsuarioControle implements Serializable {

    private Usuario usuario = new Usuario();
    @EJB
    private UsuarioFacade usuarioFacade;
    @EJB
    private PessoaFacade pessoaFacade;
    private ConverterGenerico pessoaConverter;

    public ConverterGenerico getPessoaConverter() {
        if (pessoaConverter == null) {
            pessoaConverter = new ConverterGenerico(pessoaFacade);
        }
        return pessoaConverter;
    }

    public List<Pessoa> getListaFuncionarios() {
        return pessoaFacade.listaFuncionarioAtivo();
    }

    public void salvar() {
        usuarioFacade.salvar(usuario);
        usuario = new Usuario();
    }

    public void novo() {
        usuario = new Usuario();
    }

    public void excluir(Usuario est) {
        usuarioFacade.remover(est);
    }

    public void editar(Usuario est) {
        this.usuario = est;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public UsuarioFacade getUsuarioFacade() {
        return usuarioFacade;
    }

    public void setUsuarioFacade(UsuarioFacade usuarioFacade) {
        this.usuarioFacade = usuarioFacade;
    }

    public List<Usuario> getListaUsuarios() {
        return usuarioFacade.listaTodos();
    }

}
