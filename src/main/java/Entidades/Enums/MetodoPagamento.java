/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades.Enums;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author felip
 */
public enum MetodoPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    PIX("PIX"),
    BOLETO("BOLETO");

    private final String descricao;

    MetodoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
    
     public static List<MetodoPagamento> getMetodosPagamentoAVista() {
        List<MetodoPagamento> metodosAVista = new ArrayList<>();
        metodosAVista.add(PIX);
        metodosAVista.add(CARTAO_DEBITO);
        metodosAVista.add(DINHEIRO);
        return metodosAVista;
    }

    public static List<MetodoPagamento> getMetodosPagamentoNaoAVista() {
        List<MetodoPagamento> metodosNaoAVista = new ArrayList<>();
        metodosNaoAVista.add(CARTAO_CREDITO);
        return metodosNaoAVista;
    }

}
