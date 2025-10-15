/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.MetodoPagamento;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "contaPagar")
public class ContaPagar implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contaPagar_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Pessoa fornecedor;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @Column(name = "contaPagar_descricao")
    private String descricao;

    @Column(name = "contaPagar_status")
    private String status;

    @Column(name = "contaPagar_valor")
    private Double valor;

    @Column(name = "contaPagar_metodo")
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "contaPagar_dataVencimento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVencimento;

    @Column(name = "contaPagar_dataRecebimento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRecebimento;
    
    @Column(name = "contaReceber_dataCriação")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriação;

    @ManyToOne(optional = true)
    @JoinColumn(name = "conta_id")
    private Conta contaPagamento;

    @OneToMany(mappedBy = "contaPagar", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<LancamentoFinanceiro> lancamentos = new ArrayList<>();

    public void addLancamento(LancamentoFinanceiro l) {
    l.setContaPagar(this);
    this.lancamentos.add(l);
}
    
    @Override
    public Long getId() {
        return id;
    }

    public Pessoa getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Pessoa fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Date getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(Date dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Conta getContaPagamento() {
        return contaPagamento;
    }

    public void setContaPagamento(Conta contaPagamento) {
        this.contaPagamento = contaPagamento;
    }

    public List<LancamentoFinanceiro> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<LancamentoFinanceiro> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public Date getDataCriação() {
        return dataCriação;
    }

    public void setDataCriação(Date dataCriação) {
        this.dataCriação = dataCriação;
    }
    
    

    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
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
        final ContaPagar other = (ContaPagar) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ContaPagar{" + "id=" + id + '}';
    }

}
