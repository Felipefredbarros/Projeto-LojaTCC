/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades.Enums;

/**
 *
 * @author felip
 */
public enum TipoBatida {
    ENTRADA("Entrada"),
    SAIDA("Saida"),
    PAUSA("Pausa");

    private final String label;

    TipoBatida(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
