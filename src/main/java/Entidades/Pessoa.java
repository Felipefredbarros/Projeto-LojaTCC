/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.TipoPessoa;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "pessoa")
public class Pessoa implements Serializable, ClassePai {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pes_id")
    private Long id;
    @Column(name = "pes_nome")
    private String nome;
    @Column(name = "tipoPessoa")
    private String tipoPessoa;
    @Column(name = "pes_telefone")
    private String telefone;
    @Column(name = "pes_endereco")
    private String endereco;
    @Column(name = "pes_email")
    private String email;
    @Column(name = "pes_cpfcnpj")
    private String cpfcnpj;
    @Column(name = "pes_regiao")
    private String regiao;
    @Column(name = "pes_tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoPessoa tipo;
    @Column(name = "pes_telefone_principal", length = 20)
    private String telefonePrincipal;
    @Column(name = "pes_endereco_principal", length = 255)
    private String enderecoPrincipal;
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Endereco> listaEnderecos = new HashSet<>();
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Telefone> listaTelefones = new HashSet<>();
    @Column(name = "pes_ativo")
    private Boolean ativo = true;
    @OneToOne(mappedBy = "funcionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContratoTrabalho contrato;
    @OneToMany(mappedBy = "funcionario", fetch = FetchType.LAZY)
    private List<MovimentacaoMensalFuncionario> movimentacoes;
    @OneToMany(mappedBy = "funcionario", fetch = FetchType.LAZY)
    private List<FolhaPagamento> folhasPagamento;

    public Pessoa() {
        this.ativo = true;
        this.listaEnderecos = new HashSet<>();
        this.listaTelefones = new HashSet<>();
        this.contrato = new ContratoTrabalho();
        this.tipoPessoa = "FISICA"; // Default to Pessoa Física
    }

    public void prepararParaSalvar() {
        limparCPFCNPJ();
        // Limpar outros campos formatados se houver
        if (this.telefonePrincipal != null) {
            this.telefonePrincipal = this.telefonePrincipal.replaceAll("\\D", "");
        }
        for (Telefone tel : listaTelefones) {
            if (tel.getNumero() != null) {
                tel.setNumero(tel.getNumero().replaceAll("\\D", ""));
            }
        }
        for (Endereco end : listaEnderecos) {
            if (end.getCep() != null) {
                end.setCep(end.getCep().replaceAll("\\D", ""));
            }
        }
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getStatus() {
        if (ativo == true) {
            return "Ativo";
        } else {
            return "Inativo";
        }
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String formatarCPFouCNPJ(String cpfcnpj) {
        if (cpfcnpj == null || cpfcnpj.isEmpty()) {
            return "";
        }

        cpfcnpj = cpfcnpj.replaceAll("\\D", "");

        if (cpfcnpj.length() == 11) {
            return cpfcnpj.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        } else if (cpfcnpj.length() == 14) {
            return cpfcnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }

        return cpfcnpj;
    }

    public String formatarRG(String rg) {
        if (rg == null || rg.isEmpty()) {
            return null;
        }

        if (rg.length() == 9) {
            return rg.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{1})", "$1.$2.$3-$4");
        }

        return rg;
    }

    public String getCpfcnpjFormatado() {
        return formatarCPFouCNPJ(cpfcnpj);

    }

    public void limparCPFCNPJ() {
        if (this.cpfcnpj != null && !this.cpfcnpj.isEmpty()) {
            this.cpfcnpj = this.cpfcnpj.replaceAll("[^\\d]", "");
        }
    }

    public void limparDadosPessoais() {
        limparCPFCNPJ();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpfcnpj() {
        return cpfcnpj;
    }

    public void setCpfcnpj(String cpfcnpj) {
        this.cpfcnpj = cpfcnpj;
    }

    //public Double getSalario() {
    //return salario;
    //}
    //public void setSalario(Double salario) {
    //this.salario = salario;
    //}
    //public String getCargo() {
    //  return cargo;
    //}
    //public void setCargo(String cargo) {
    //    this.cargo = cargo;
    //}
    // public String getRegiao() {
    //    return regiao;
    // }
    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    //public Date getDiaPagamentos() {
    //   return diaPagamentos;
    // }
    // public void setDiaPagamentos(Date diaPagamentos) {
    //    this.diaPagamentos = diaPagamentos;
    // }
    public TipoPessoa getTipo() {
        return tipo;
    }

    public void setTipo(TipoPessoa tipo) {
        this.tipo = tipo;
    }

    public Set<Endereco> getListaEnderecos() {
        return listaEnderecos;
    }

    public void setListaEnderecos(Set<Endereco> listaEnderecos) {
        this.listaEnderecos = listaEnderecos;
    }

    public Set<Telefone> getListaTelefones() {
        return listaTelefones;
    }

    public void setListaTelefones(Set<Telefone> listaTelefones) {
        this.listaTelefones = listaTelefones;
    }

    public String getTelefonePrincipal() {
        return telefonePrincipal;
    }

    public void setTelefonePrincipal(String telefonePrincipal) {
        this.telefonePrincipal = telefonePrincipal;
    }

    public String getEnderecoPrincipal() {
        return enderecoPrincipal;
    }

    public void setEnderecoPrincipal(String enderecoPrincipal) {
        this.enderecoPrincipal = enderecoPrincipal;
    }

    public List<MovimentacaoMensalFuncionario> getMovimentacoes() {
        return movimentacoes;
    }

    public void setMovimentacoes(List<MovimentacaoMensalFuncionario> movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    public List<FolhaPagamento> getFolhasPagamento() {
        return folhasPagamento;
    }

    public void setFolhasPagamento(List<FolhaPagamento> folhasPagamento) {
        this.folhasPagamento = folhasPagamento;
    }

    public ContratoTrabalho getContrato() {
        return contrato;
    }

    public void setContrato(ContratoTrabalho contrato) {
        this.contrato = contrato;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final Pessoa other = (Pessoa) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Pessoa{" + "id=" + id + '}';
    }

}
