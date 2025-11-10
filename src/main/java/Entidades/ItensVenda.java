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
@Table(name = "itensvenda")
public class ItensVenda implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iv_id")
    private Long id;
    @Column(name = "iv_quantidade", nullable = false)
    private Double quantidade;
    @Column(name = "iv_descontoTotal")
    private Double descontoTotal;
    @Column(name = "iv_valorUnitario", nullable = false)
    private Double valorUnitario;
    @Column(name = "iv_custoUnitario")
    private Double custoUnitario;
    @Column(name = "iv_desc")
    private String desc;
    @ManyToOne
    @JoinColumn(nullable = false, name = "venda_id")
    private Venda venda;
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

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public Double getDescontoTotal() {
        return descontoTotal;
    }

    public void setDescontoTotal(Double descontoTotal) {
        this.descontoTotal = descontoTotal;
    }

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Double getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(Double custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
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
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final ItensVenda other = (ItensVenda) obj;

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
