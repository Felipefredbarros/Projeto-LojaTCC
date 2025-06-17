/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Entidades.Enums;

/**
 *
 * @author felip
 */
public enum TipoBonus {
    HORA_EXTRA("Hora Extra"), 
    COMISSAO("Comissão");
    
    private final String descricao;

    private TipoBonus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
