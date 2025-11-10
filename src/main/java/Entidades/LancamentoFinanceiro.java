/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoLancamento;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "lancamentofinanceiro")
public class LancamentoFinanceiro implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lancamento_id")
    private Long id;

    @Column(name = "lancamento_datahora")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;

    @Column(name = "lancamento_valor")
    private Double valor;

    @Column(name = "lancamento_tipoLancamento")
    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    @Column(name = "lancamento_descricao")
    private String descricao;

    @Column(name = "lancamento_metodo")
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodo;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusLancamento status;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "contaReceber_id")  // sem unique
    private ContaReceber contaReceber;

    @ManyToOne
    @JoinColumn(name = "contaPagar_id")  // sem unique
    private ContaPagar contaPagar;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = metodo;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public StatusLancamento getStatus() {
        return status;
    }

    public void setStatus(StatusLancamento status) {
        this.status = status;
    }

    public ContaPagar getContaPagar() {
        return contaPagar;
    }

    public void setContaPagar(ContaPagar contaPagar) {
        this.contaPagar = contaPagar;
    }

    public ContaReceber getContaReceber() {
        return contaReceber;
    }

    public void setContaReceber(ContaReceber contaReceber) {
        this.contaReceber = contaReceber;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.id);
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
        final LancamentoFinanceiro other = (LancamentoFinanceiro) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "LancamentoFinanceiro{" + "id=" + id + '}';
    }

}
