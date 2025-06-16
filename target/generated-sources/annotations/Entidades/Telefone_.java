package Entidades;

import Entidades.Pessoa;
import Entidades.tipoTelefone;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-06-16T00:41:31")
@StaticMetamodel(Telefone.class)
public class Telefone_ { 

    public static volatile SingularAttribute<Telefone, tipoTelefone> tipoTelefone;
    public static volatile SingularAttribute<Telefone, Pessoa> pessoa;
    public static volatile SingularAttribute<Telefone, String> numero;
    public static volatile SingularAttribute<Telefone, Long> id;
    public static volatile SingularAttribute<Telefone, String> operadora;

}