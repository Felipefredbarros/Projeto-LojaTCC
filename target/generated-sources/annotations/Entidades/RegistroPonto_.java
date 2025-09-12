package Entidades;

import Entidades.Enums.TipoBatida;
import Entidades.Pessoa;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(RegistroPonto.class)
public class RegistroPonto_ { 

    public static volatile SingularAttribute<RegistroPonto, TipoBatida> tipoBatida;
    public static volatile SingularAttribute<RegistroPonto, Long> id;
    public static volatile SingularAttribute<RegistroPonto, Pessoa> funcionario;
    public static volatile SingularAttribute<RegistroPonto, Date> dataHora;

}