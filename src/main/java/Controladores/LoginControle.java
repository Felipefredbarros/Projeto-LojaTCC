/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Entidades.Usuario;
import Facade.UsuarioFacade;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author felip
 */
@Named("LoginControle")
@RequestScoped
public class LoginControle {

    @EJB
    private UsuarioFacade usuarioFacade;

    private String login;
    private String senha;

    public String entrar() {
        Usuario u = usuarioFacade.findByLogin(login);
        if (u != null && u.verificarSenha(this.senha)) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("usuarioLogado", u);
            return "/index.xhtml?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login ou senha inválidos", null));
        return null;
    }

    public String sair() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

}
