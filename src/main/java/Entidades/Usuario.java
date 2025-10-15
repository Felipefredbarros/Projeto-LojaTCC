/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @Column(name = "usuario_login", unique = true, nullable = false)
    private String login;

    @Column(name = "usuario_senha", nullable = false)
    private String senha;

    @ManyToOne
    @JoinColumn(name = "pessoa_id", unique = true)
    private Pessoa pessoa;
    
    public boolean verificarSenha(String senhaPura) {
    if (senhaPura == null || this.senha == null) {
        return false;
    }
    // O método checkpw compara a senha em texto puro com o hash armazenado.
    // Ele extrai o "sal" do hash automaticamente para fazer a comparação.
    return BCrypt.checkpw(senhaPura, this.senha);
}

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setSenha(String senhaPura) {
        // Só faz o hash se a senha não for nula ou vazia
        if (senhaPura != null && !senhaPura.isEmpty()) {
            // BCrypt.gensalt() cria um "sal" aleatório para cada senha, tornando-a muito mais segura.
            // O sal é armazenado junto com o hash final, então você não precisa guardá-lo separadamente.
            this.senha = BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)); // O "12" é a força do hash. 10 a 12 é o padrão.
        }
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Usuario other = (Usuario) obj;
        return Objects.equals(this.id, other.id);
    }

}
