package Entidades;

import Entidades.Pessoa;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:44")
@StaticMetamodel(ContratoTrabalho.class)
public class ContratoTrabalho_ { 

    public static volatile SingularAttribute<ContratoTrabalho, Date> diaPagamentos;
    public static volatile SingularAttribute<ContratoTrabalho, Double> jornadaDiariaHoras;
    public static volatile SingularAttribute<ContratoTrabalho, Double> salario;
    public static volatile SingularAttribute<ContratoTrabalho, Long> id;
    public static volatile SingularAttribute<ContratoTrabalho, Pessoa> funcionario;
    public static volatile SingularAttribute<ContratoTrabalho, Date> dataInicio;
    public static volatile SingularAttribute<ContratoTrabalho, String> cargo;
    public static volatile SingularAttribute<ContratoTrabalho, Boolean> status;

}