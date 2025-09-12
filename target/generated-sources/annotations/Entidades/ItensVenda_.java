package Entidades;

import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(ItensVenda.class)
public class ItensVenda_ { 

    public static volatile SingularAttribute<ItensVenda, Venda> venda;
    public static volatile SingularAttribute<ItensVenda, ProdutoDerivacao> produtoDerivacao;
    public static volatile SingularAttribute<ItensVenda, Long> id;
    public static volatile SingularAttribute<ItensVenda, Double> descontoTotal;
    public static volatile SingularAttribute<ItensVenda, Double> quantidade;
    public static volatile SingularAttribute<ItensVenda, Double> custoUnitario;
    public static volatile SingularAttribute<ItensVenda, Double> valorUnitario;
    public static volatile SingularAttribute<ItensVenda, String> desc;

}