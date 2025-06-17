/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "contratoTrabalho")
public class ContratoTrabalho implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contrato_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "funcionario_id")
    private Pessoa funcionario;

    @Column(name = "contrato_horastotal")
    private Double jornadaDiariaHoras; // ex: 8.0

    @Column(name = "contrato_datainicio")
    @Temporal(TemporalType.DATE)
    private Date dataInicio;

    @Column(name = "contrato_status")
    private boolean status;
    
    @Column(name = "contrato_salario")
    private Double salario;
    
    @Column(name = "contrato_cargo")
    private String cargo;
    
    @Column(name = "contrato_dia_pagamentos")
    @Temporal(TemporalType.TIMESTAMP)
    private Date diaPagamentos;

    @Override
    public Long getId() {
        return id;
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

    public Double getJornadaDiariaHoras() {
        return jornadaDiariaHoras;
    }

    public void setJornadaDiariaHoras(Double jornadaDiariaHoras) {
        this.jornadaDiariaHoras = jornadaDiariaHoras;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Double getSalario() {
        return salario;
    }

    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Date getDiaPagamentos() {
        return diaPagamentos;
    }

    public void setDiaPagamentos(Date diaPagamentos) {
        this.diaPagamentos = diaPagamentos;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.id);
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
        final ContratoTrabalho other = (ContratoTrabalho) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ContratoTrabalho{" + "id=" + id + '}';
    }

}
