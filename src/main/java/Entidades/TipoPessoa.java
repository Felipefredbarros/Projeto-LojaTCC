/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

/**
 *
 * @author felip
 */
public enum TipoPessoa {
    CLIENTE("Cliente"),
    FORNECEDOR("Fornecedor"),
    FUNCIONARIO("Funcionário");

    private final String label;

    TipoPessoa(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
