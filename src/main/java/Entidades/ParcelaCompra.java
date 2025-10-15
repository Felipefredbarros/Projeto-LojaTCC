/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.MetodoPagamento;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "parcela_compra")
public class ParcelaCompra implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "par_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;
    
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Temporal(TemporalType.DATE)
    @Column(name = "par_data_vencimento")
    private Date dataVencimento;

    @Column(name = "par_valor_parcela")
    private Double valorParcela;
    
    @Column(name = "par_metodoPagamento", nullable = true)
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(Double valorParcela) {
        this.valorParcela = valorParcela;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
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
        final ParcelaCompra other = (ParcelaCompra) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ParcelaCompra{" + "id=" + id + '}';
    }
    
    
    
    
    
    
}
