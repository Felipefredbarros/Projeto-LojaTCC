/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

/**
 *
 * @author felip
 */
@Entity
@Table(name = "compra")
public class Compra implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_id")
    private Long id;

    @Column(name = "com_metodoPagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "com_planoPagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanoPagamento planoPagamento;

    @Column(name = "com_valorTotal", nullable = false)
    private Double valorTotal = 0d;

    @Column(name = "com_dataCompra", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCompra;

    @Column(name = "com_dataVencimento", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVencimento;

    @Column(name = "com_parcelas", nullable = false)
    private Integer parcelas;

    @ManyToOne
    @JoinColumn(nullable = false, name = "fornecedor_id")
    private Pessoa fornecedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ItensCompra> itensCompra;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ParcelaCompra> parcelasCompra;

    public List<ParcelaCompra> getParcelasCompra() {
        return parcelasCompra;
    }

    public void setParcelasCompra(List<ParcelaCompra> parcelasCompra) {
        this.parcelasCompra = parcelasCompra;
    }

    public void calcularParcelas() {
        if (parcelas == null || parcelas <= 0) {
            throw new IllegalStateException("O número de parcelas não está definido ou é inválido.");
        }

        if (dataVencimento == null) {
            throw new IllegalStateException("A data de vencimento não está definida.");
        }

        parcelasCompra.clear();
        if (parcelas > 0 && valorTotal != null) {
            double valorParcela = valorTotal / parcelas;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataVencimento);  // Certifique-se de que dataVencimento é válida

            for (int i = 0; i < parcelas; i++) {
                ParcelaCompra parcela = new ParcelaCompra();
                parcela.setCompra(this);
                parcela.setValorParcela(valorParcela);
                parcela.setDataVencimento(calendar.getTime());  // Gera a data de vencimento corretamente
                parcelasCompra.add(parcela);
                calendar.add(Calendar.MONTH, 1);  // Incrementa o mês para a próxima parcela
            }
        }
    }

    public Compra() {
        itensCompra = new ArrayList<>();
        this.parcelasCompra = new ArrayList<>();  // Inicializando a lista de parcelas
        dataCompra = new Date();
    }

    public Double getTotal() {
        valorTotal = 0d;
        for (ItensCompra ic : itensCompra) {
            valorTotal = valorTotal + ic.getSubTotal();
        }
        return valorTotal;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public PlanoPagamento getPlanoPagamento() {
        return planoPagamento;
    }

    public void setPlanoPagamento(PlanoPagamento planoPagamento) {
        this.planoPagamento = planoPagamento;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Date getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Date dataCompra) {
        this.dataCompra = dataCompra;
    }

    public Pessoa getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Pessoa fornecedor) {
        this.fornecedor = fornecedor;
    }

    public List<ItensCompra> getItensCompra() {
        return itensCompra;
    }

    public void setItensCompra(List<ItensCompra> itensCompra) {
        this.itensCompra = itensCompra;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final Compra other = (Compra) obj;
        return Objects.equals(this.id, other.id);
    }

}
