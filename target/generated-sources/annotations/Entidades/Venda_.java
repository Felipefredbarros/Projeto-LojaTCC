package Entidades;

import Entidades.ItensVenda;
import Entidades.MetodoPagamento;
import Entidades.Pessoa;
import Entidades.PlanoPagamento;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-06-16T00:41:31")
@StaticMetamodel(Venda.class)
public class Venda_ { 

    public static volatile SingularAttribute<Venda, Pessoa> cliente;
    public static volatile SingularAttribute<Venda, Date> dataVenda;
    public static volatile ListAttribute<Venda, ItensVenda> itensVenda;
    public static volatile SingularAttribute<Venda, MetodoPagamento> metodoPagamento;
    public static volatile SingularAttribute<Venda, PlanoPagamento> planoPagamento;
    public static volatile SingularAttribute<Venda, Double> valorTotal;
    public static volatile SingularAttribute<Venda, Date> dataVencimento;
    public static volatile SingularAttribute<Venda, Long> id;
    public static volatile SingularAttribute<Venda, Pessoa> funcionario;

}