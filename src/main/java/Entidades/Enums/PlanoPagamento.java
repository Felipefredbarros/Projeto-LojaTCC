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
public enum PlanoPagamento {
    A_VISTA("À Vista"),
    PARCELADO_EM_1X("Parcelado em 1x"),
    PARCELADO_EM_2X("Parcelado em 2x"),
    PARCELADO_EM_3X("Parcelado em 3x"),
    FIADO("Fiado"),
    PARCELADO_COMPRA("Parcelado");

    private final String descricao;

    PlanoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static List<PlanoPagamento> getPlanosPagamento() {
        List<PlanoPagamento> planos = new ArrayList<>();
        planos.add(A_VISTA);
        planos.add(PARCELADO_EM_1X);
        planos.add(PARCELADO_EM_2X);
        planos.add(PARCELADO_EM_3X);
        planos.add(FIADO);
        return planos;
    }
}
