/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "endereco")
public class Endereco implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "end_id")
    private Long id;
    @Column(name = "end_numero")
    private String numero;
    @Column(name = "end_bairro")
    private String bairro;
    @Column(name = "end_rua")
    private String rua;
    @Column(name = "end_cep")
    private String cep;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_id")
    private Cidade cidade;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endereco that = (Endereco) o;

        // Se ambos os objetos têm IDs (já foram persistidos), compare pelos IDs.
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // Se estamos aqui, pelo menos um dos IDs é null.
        // Se ambos os IDs são null (objetos novos, ainda não persistidos),
        // compare pelos campos de negócio para determinar a igualdade.
        if (this.id == null && that.id == null) {
            return Objects.equals(rua, that.rua)
                    && Objects.equals(numero, that.numero)
                    && Objects.equals(bairro, that.bairro)
                    && Objects.equals(cep, that.cep)
                    && Objects.equals(cidade, that.cidade); // Atenção com 'cidade' se puder ser null
        }

        // Se um tem ID e o outro não, eles são considerados diferentes.
        return false;
    }

    @Override
    public int hashCode() {
        // Se o ID existe (objeto persistido), use o hashCode do ID.
        if (id != null) {
            return Objects.hash(id);
        }
        // Para objetos novos (ID é null), use o hashCode dos campos de negócio.
        return Objects.hash(rua, numero, bairro, cep, cidade); // Atenção com 'cidade' se puder ser null
    }

    @Override
    public String toString() {
        return "Endereco{" + "id=" + id + '}';
    }

}
