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

/**
 *
 * @author felip
 */
@Entity
@Table(name = "produtoDerivacao")
public class ProdutoDerivacao implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prodVar_id")
    private Long id;
    @Column(name = "prodVar_descricao")
    private String descricao;
    @Column(name = "prodVar_tamanho")
    private String tamanho;
    @Column(name = "prodVar_cor")
    private String cor;
    @Column(name = "prodVar_quantidade")
    private Double quantidade;
    @Column(name = "prodVar_reservado")
    private Double reservado = 0.0;
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public Double getReservado() {
        return reservado;
    }

    public void setReservado(Double reservado) {
        this.reservado = reservado;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        // 1. Verifica se é a exata mesma instância na memória
        if (this == obj) {
            return true;
        }

        // 2. Verifica se é nulo ou de classe diferente
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        // 3. Converte o objeto
        final ProdutoDerivacao other = (ProdutoDerivacao) obj;

        // --- LÓGICA CORRIGIDA ---
        // 4. Se 'this.id' for nulo, o objeto só pode ser igual
        //    a ele mesmo (o que já foi checado no passo 1).
        //    Como 'this' é diferente de 'obj', retornamos 'false'.
        if (this.id == null) {
            return false;
        }

        // 5. Se o 'this.id' não é nulo, aí sim comparamos pelo ID.
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ProdutoDerivacao{" + "id=" + id + '}';
    }

    public String getTexto() {
        return "Produto: " + getProduto().getMarca().getMarca() + " "
                + getProduto().getCategoria().getCategoria() + " "
                + getDescricao()
                + " | Tamanho: " + getTamanho()
                + " | Cor: " + getCor();
    }

}
