package Entidades;

import Entidades.Pessoa;
import Entidades.Venda;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(ContaReceber.class)
public class ContaReceber_ { 

    public static volatile SingularAttribute<ContaReceber, Pessoa> cliente;
    public static volatile SingularAttribute<ContaReceber, Venda> venda;
    public static volatile SingularAttribute<ContaReceber, Date> dataVencimento;
    public static volatile SingularAttribute<ContaReceber, Double> valor;
    public static volatile SingularAttribute<ContaReceber, Long> id;
    public static volatile SingularAttribute<ContaReceber, Date> dataRecebimento;
    public static volatile SingularAttribute<ContaReceber, String> descricao;
    public static volatile SingularAttribute<ContaReceber, String> status;

}