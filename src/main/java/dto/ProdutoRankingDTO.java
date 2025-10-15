/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import Entidades.Produto;

/**
 *
 * @author felip
 */
public class ProdutoRankingDTO {

    private final Produto produto;
    private final Double quantidade;
    private final Double total;

    public ProdutoRankingDTO(Produto produto, Double quantidade, Double total) {
        this.produto = produto;
        this.quantidade = quantidade != null ? quantidade : 0d;
        this.total = total != null ? total : 0d;
    }

    public Produto getProduto() {
        return produto;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public Double getTotal() {
        return total;
    }
}
