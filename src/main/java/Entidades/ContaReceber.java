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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "contaReceber")
public class ContaReceber implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contaReceber_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Pessoa cliente;

    @Column(name = "contaReceber_descricao")
    private String descricao;

    @Column(name = "contaReceber_status")
    private String status;

    @Column(name = "contaReceber_valor")
    private Double valor;

    @Column(name = "contaReceber_metodo")
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "contaReceber_dataVencimento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVencimento;

    @Column(name = "contaReceber_dataRecebimento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRecebimento;
    
    @Column(name = "contaReceber_dataCriação")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriação;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "conta_id")
    private Conta contaRecebimento;

    @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<LancamentoFinanceiro> lancamentos = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }
    
    public void addLancamento(LancamentoFinanceiro l) {
    l.setContaReceber(this);
    this.lancamentos.add(l);
}

    public void setId(Long id) {
        this.id = id;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
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

    public Pessoa getCliente() {
        return cliente;
    }

    public void setCliente(Pessoa cliente) {
        this.cliente = cliente;
    }

    public Conta getContaRecebimento() {
        return contaRecebimento;
    }

    public void setContaRecebimento(Conta contaRecebimento) {
        this.contaRecebimento = contaRecebimento;
    }
    
    
    
    

    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.id);
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
        final ContaReceber other = (ContaReceber) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ContaReceber{" + "id=" + id + '}';
    }

}
