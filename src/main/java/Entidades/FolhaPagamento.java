/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "folhaPagamento")
public class FolhaPagamento implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pag_id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Pessoa funcionario;
    @Column(name = "pag_salarioBase")
    private Double salarioBase;
    @Column(name = "pag_adicional")
    private Double adicional;
    @Column(name = "pag_comissao")
    private Double comissao;
    @Column(name = "pag_inss")
    private Double inss;
    @Column(name = "pag_irrf")
    private Double irrf;
    @Column(name = "pag_fgts")
    private Double fgts;
    @Column(name = "pag_salarioLiquido")
    private Double salarioLiquido;
    @Column(name = "pag_competencia")
    @Temporal(TemporalType.DATE)
    private Date competencia; // ex: 2025-06-01 (junho de 2025)
    @CreationTimestamp
    @Column(name = "pag_dataGeracao", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataGeracao;
    @OneToMany(mappedBy = "folhaPagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentacaoMensalFuncionario> movimentacoes;

    public double calcularINSS(double salario) {
        if (salario <= 1412.00) {
            return salario * 0.075;
        }
        if (salario <= 2666.68) {
            return salario * 0.09;
        }
        if (salario <= 4000.03) {
            return salario * 0.12;
        }
        if (salario <= 7786.02) {
            return salario * 0.14;
        }
        return 0.0;
    }

    public double calcularIRRF(double salarioBruto, double inss) {
        double base = salarioBruto - inss;
        double irrf;
        if (base <= 2112.00) {
            irrf = 0.0;
        } else if (base <= 2826.65) {
            irrf = base * 0.075 - 158.40;
        } else if (base <= 3751.05) {
            irrf = base * 0.15 - 370.40;
        } else if (base <= 4664.68) {
            irrf = base * 0.225 - 651.73;
        } else {
            irrf = base * 0.275 - 884.96;
        }
        return Math.max(irrf, 0.0);
    }

    public double calcularFGTS(double salario) {
        return salario * 0.08;
    }

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

    public Double getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(Double salarioBase) {
        this.salarioBase = salarioBase;
    }

    public Double getAdicional() {
        return adicional;
    }

    public void setAdicional(Double adicional) {
        this.adicional = adicional;
    }

    public Double getComissao() {
        return comissao;
    }

    public void setComissao(Double comissao) {
        this.comissao = comissao;
    }

    public Double getInss() {
        return inss;
    }

    public void setInss(Double inss) {
        this.inss = inss;
    }

    public Double getIrrf() {
        return irrf;
    }

    public void setIrrf(Double irrf) {
        this.irrf = irrf;
    }

    public Double getFgts() {
        return fgts;
    }

    public void setFgts(Double fgts) {
        this.fgts = fgts;
    }

    public Double getSalarioLiquido() {
        return salarioLiquido;
    }

    public void setSalarioLiquido(Double salarioLiquido) {
        this.salarioLiquido = salarioLiquido;
    }

    public Date getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Date competencia) {
        this.competencia = competencia;
    }

    public Date getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Date dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public List<MovimentacaoMensalFuncionario> getMovimentacoes() {
        return movimentacoes;
    }

    public void setMovimentacoes(List<MovimentacaoMensalFuncionario> movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final FolhaPagamento other = (FolhaPagamento) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "FolhaPagamento{" + "id=" + id + '}';
    }

}
