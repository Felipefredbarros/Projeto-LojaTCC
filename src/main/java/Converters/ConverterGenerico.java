/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Converters;

import Entidades.ClassePai;
import Facade.AbstractFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author felip
 */
public class ConverterGenerico implements Converter{
    
    private AbstractFacade abstractFacade;

    public ConverterGenerico(AbstractFacade abstractFacade) {
        this.abstractFacade = abstractFacade;
    }
    
   
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            Long id = Long.valueOf(value);
            return abstractFacade.buscar(id); // seu método do facade
        } catch (NumberFormatException e) {
            return null; // não é número => não converte
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        if (o == null) {
            return "";
        }
        // Quando o renderer te entrega a própria String, só devolve
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Entidades.ClassePai) {
            Long id = ((Entidades.ClassePai) o).getId();
            return id == null ? "" : String.valueOf(id);
        }
        // Tipo inesperado: evita NPE/ClassCast
        return "";
    }
    
}