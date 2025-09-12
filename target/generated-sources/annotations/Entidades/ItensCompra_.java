package Entidades;

import Entidades.Compra;
import Entidades.ProdutoDerivacao;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(ItensCompra.class)
public class ItensCompra_ { 

    public static volatile SingularAttribute<ItensCompra, Compra> compra;
    public static volatile SingularAttribute<ItensCompra, ProdutoDerivacao> produtoDerivacao;
    public static volatile SingularAttribute<ItensCompra, Long> id;
    public static volatile SingularAttribute<ItensCompra, Integer> quantidade;
    public static volatile SingularAttribute<ItensCompra, Double> valorUnitario;
    public static volatile SingularAttribute<ItensCompra, String> desc;

}