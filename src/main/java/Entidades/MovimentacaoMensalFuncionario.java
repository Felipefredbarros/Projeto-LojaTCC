/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.TipoBonus;
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
@Table(name = "movimentacaoMensalFuncionario")
public class MovimentacaoMensalFuncionario implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movFunc_id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Pessoa funcionario;
    @Column(name = "movFunc_data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;
    @Column(name = "movFunc_bonus")
    private Double bonus = 0d;
    @Column(name = "movFunc_porCom")
    private Double porcentagemCom = 0.05d;
    @Column(name = "movFunc_tipoBonus")
    @Enumerated(EnumType.STRING)
    private TipoBonus tipoBonus;
    @OneToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;
    @ManyToOne
    @JoinColumn(name = "folha_pagamento_id")
    private FolhaPagamento folhaPagamento;

    @Override
    public Long getId() {
        return id;
    }

    public void calcularBonus(Double valor) {
        if(getTipoBonus().equals(tipoBonus.COMISSAO)){
            this.bonus = valor * porcentagemCom;
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pessoa getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Pessoa funcionario) {
        this.funcionario = funcionario;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Double getBonus() {
        return bonus;
    }

    public void setBonus(Double bonus) {
        calcularBonus(bonus);
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public FolhaPagamento getFolhaPagamento() {
        return folhaPagamento;
    }

    public void setFolhaPagamento(FolhaPagamento folhaPagamento) {
        this.folhaPagamento = folhaPagamento;
    }

    public TipoBonus getTipoBonus() {
        return tipoBonus;
    }

    public void setTipoBonus(TipoBonus tipoBonus) {
        this.tipoBonus = tipoBonus;
    }

    public Double getPorcentagemCom() {
        return porcentagemCom;
    }

    public void setPorcentagemCom(Double porcentagemCom) {
        this.porcentagemCom = porcentagemCom;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final MovimentacaoMensalFuncionario other = (MovimentacaoMensalFuncionario) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "MovimentacaoMensalFuncionario{" + "id=" + id + '}';
    }

}
