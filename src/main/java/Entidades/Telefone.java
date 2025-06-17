/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import Entidades.Enums.tipoTelefone;
import java.io.Serializable;
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
import javax.persistence.Table;

/**
 *
 * @author felip
 */
@Entity
@Table(name = "telefone")
public class Telefone implements Serializable, ClassePai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tel_id")
    private Long id;
    @Column(name = "tel_numero")
    private String numero;
    @Column(name = "tel_operadora")
    private String operadora;
    @Column(name = "tel_tipotelefone")
    @Enumerated(EnumType.STRING)
    private tipoTelefone tipoTelefone;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Override
    public Long getId() {
        return id;
    }

    public String getNumeroFormatado() {
        if (numero != null && numero.length() == 11) {
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 7) + "-" + numero.substring(7);
        } else if (numero != null && numero.length() == 10) {
            // Caso o telefone fixo não tenha 9 na frente
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 6) + "-" + numero.substring(6);
        }
        return numero; // Retorna como está caso não tenha o tamanho esperado
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getOperadora() {
        return operadora;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    public tipoTelefone getTipoTelefone() {
        return tipoTelefone;
    }

    public void setTipoTelefone(tipoTelefone tipoTelefone) {
        this.tipoTelefone = tipoTelefone;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    @Override
    public int hashCode() {
        // Se o ID existe (objeto persistido), use o hashCode do ID.
        if (id != null) {
            return Objects.hash(id);
        }
        // Para objetos novos (ID é null), use o hashCode dos campos de negócio
        // que definem a unicidade de um telefone ANTES de ser persistido.
        // 'numero' e 'tipoTelefone' são bons candidatos. 'operadora' pode ser opcional.
        // A referência 'pessoa' não deve ser usada aqui para evitar recursão e
        // porque a unicidade é geralmente dentro da lista de uma pessoa específica.
        return Objects.hash(numero, tipoTelefone);
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
        final Telefone other = (Telefone) obj;

        // Se ambos os objetos têm IDs (já foram persistidos), compare pelos IDs.
        if (this.id != null && other.id != null) {
            return Objects.equals(this.id, other.id);
        }

        // Se estamos aqui, pelo menos um dos IDs é null.
        // Se ambos os IDs são null (objetos novos, ainda não persistidos),
        // compare pelos campos de negócio para determinar a igualdade.
        if (this.id == null && other.id == null) {
            return Objects.equals(this.numero, other.numero)
                    && Objects.equals(this.tipoTelefone, other.tipoTelefone);
            // Considere adicionar Objects.equals(this.operadora, other.operadora)
            // se a operadora também fizer parte da chave de unicidade para um telefone não salvo.
        }

        // Se um tem ID e o outro não, eles são considerados diferentes.
        return false;
    }

    @Override
    public String toString() {
        return "Telefone{" + "id=" + id + '}';
    }
}
