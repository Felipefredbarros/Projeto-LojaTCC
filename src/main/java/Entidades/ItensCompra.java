/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "itenscompra")
public class ItensCompra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ic_id")
    private Long id;
    @Column(name = "ic_quantidade")
    private Integer quantidade;
    @Column(name = "ic_valorUnitario")
    private Double valorUnitario;
    @Column(name = "ic_desc")
    private String desc;
    @ManyToOne
    @JoinColumn(nullable = false, name = "compra_id")
    private Compra compra;
    @ManyToOne
    @JoinColumn(name = "produtoDerivacao")
    private ProdutoDerivacao produtoDerivacao;

    public Double getSubTotal() {
        return quantidade * valorUnitario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public ProdutoDerivacao getProdutoDerivacao() {
        return produtoDerivacao;
    }

    public void setProdutoDerivacao(ProdutoDerivacao produtoDerivacao) {
        this.produtoDerivacao = produtoDerivacao;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final ItensCompra other = (ItensCompra) obj;

        // 4. LÓGICA CORRIGIDA: Se 'this.id' for nulo, o objeto só pode ser igual
        //    a ele mesmo (o que já foi checado no passo 1).
        //    Como 'this' é diferente de 'obj', retornamos 'false'.
        if (this.id == null) {
            return false;
        }

        // 5. Se o 'this.id' não é nulo, aí sim comparamos pelo ID.
        return Objects.equals(this.id, other.id);
    }

}
