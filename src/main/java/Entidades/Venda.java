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
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "venda")
public class Venda implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ven_id")
    private Long id;

    @Column(name = "ven_metodoPagamento", nullable = true)
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "ven_planoPagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanoPagamento planoPagamento;

    @Column(name = "ven_valorTotal")
    private Double valorTotal = 0d;

    @Column(name = "ven_dataVenda", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVenda;

    @Column(name = "ven_dataVencimento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVencimento;
    
    @Column(name = "ven_status")
    private String status = "Aberta";

    @ManyToOne
    @JoinColumn(nullable = false, name = "funcionario_id")
    private Pessoa funcionario;

    @ManyToOne
    @JoinColumn(nullable = false, name = "cliente_id")
    private Pessoa cliente;

    @OneToOne
    @JoinColumn(name = "movimentacao_id")
    private MovimentacaoMensalFuncionario movimentacao;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItensVenda> itensVenda;
    
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContaReceber> contasReceber = new ArrayList<>();
    
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "ordem")
    private List<ParcelaCompra> parcelasVenda;
    
    @Column(name = "ven_parcelas")
    private Integer parcelas;

    public Venda() {
        itensVenda = new ArrayList<>();
        dataVenda = new Date();
        this.parcelasVenda = new ArrayList<>();
    }
    
    public void calcularParcelas() {
        if (parcelas == null || parcelas <= 0) {
            throw new IllegalStateException("O número de parcelas não está definido ou é inválido.");
        }

        if (dataVencimento == null) {
            throw new IllegalStateException("A data de vencimento não está definida.");
        }

        parcelasVenda.clear();
        if (parcelas > 0 && valorTotal != null) {
            double valorParcela = valorTotal / parcelas;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataVencimento);  

            for (int i = 0; i < parcelas; i++) {
                ParcelaCompra parcela = new ParcelaCompra();
                parcela.setVenda(this);
                parcela.setValorParcela(valorParcela);
                parcela.setMetodoPagamento(MetodoPagamento.A_DENIFIR);
                parcela.setDataVencimento(calendar.getTime());  
                parcelasVenda.add(parcela);
                calendar.add(Calendar.MONTH, 1);  
            }
        }
    }

    public Double getTotal() {
        valorTotal = 0d;
        for (ItensVenda it : itensVenda) {
            valorTotal = valorTotal + it.getSubTotal();
        }
        return valorTotal;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Pessoa getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Pessoa funcionario) {
        this.funcionario = funcionario;
    }

    public Pessoa getCliente() {
        return cliente;
    }

    public void setCliente(Pessoa cliente) {
        this.cliente = cliente;
    }

    public List<ItensVenda> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItensVenda> itensVenda) {
        this.itensVenda = itensVenda;
    }

    public MovimentacaoMensalFuncionario getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(MovimentacaoMensalFuncionario movimentacao) {
        this.movimentacao = movimentacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ContaReceber> getContasReceber() {
        return contasReceber;
    }

    public void setContasReceber(List<ContaReceber> contasReceber) {
        this.contasReceber = contasReceber;
    }

    public List<ParcelaCompra> getParcelasVenda() {
        return parcelasVenda;
    }

    public void setParcelasVenda(List<ParcelaCompra> parcelasVenda) {
        this.parcelasVenda = parcelasVenda;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }
    
    
    
    
    
    
    
    

    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final Venda other = (Venda) obj;
        return Objects.equals(this.id, other.id);
    }
}
