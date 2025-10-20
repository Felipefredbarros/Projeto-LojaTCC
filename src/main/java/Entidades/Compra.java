/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.PlanoPagamento;
import Entidades.Enums.MetodoPagamento;
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
import javax.persistence.OrderColumn;
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
    @Column(name = "com_metodoPagamento", nullable = true)
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "com_planoPagamento", nullable = true)
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

    @Column(name = "ven_status")
    private String status = "Aberta";

    @ManyToOne
    @JoinColumn(nullable = false, name = "fornecedor_id")
    private Pessoa fornecedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItensCompra> itensCompra;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderColumn(name = "ordem")
    private List<ParcelaCompra> parcelasCompra;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContaPagar> contasPagar = new ArrayList<>();

    public void addItem(ItensCompra item) {
        itensCompra.add(item);
        item.setCompra(this);
    }

    public void removeItem(ItensCompra item) {
        itensCompra.remove(item);
        item.setCompra(null);   // <<< zera o dono do relacionamento
    }

    public void addParcela(ParcelaCompra p) {
        parcelasCompra.add(p);
        p.setCompra(this);
    }

    public void removeParcela(ParcelaCompra p) {
        parcelasCompra.remove(p);
        p.setCompra(null);
    }

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

        for (int i = parcelasCompra.size() - 1; i >= 0; i--) {
            removeParcela(parcelasCompra.get(i));
        }

        double valorParcela = (valorTotal != null ? valorTotal : 0d) / parcelas;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataVencimento);

        for (int i = 0; i < parcelas; i++) {
            ParcelaCompra p = new ParcelaCompra();
            p.setValorParcela(valorParcela);
            p.setMetodoPagamento(MetodoPagamento.A_DENIFIR);
            p.setDataVencimento(cal.getTime());
            addParcela(p);
            cal.add(Calendar.MONTH, 1);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ContaPagar> getContasPagar() {
        return contasPagar;
    }

    public void setContasPagar(List<ContaPagar> contasPagar) {
        this.contasPagar = contasPagar;
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
