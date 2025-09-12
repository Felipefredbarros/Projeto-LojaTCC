package Entidades;

import Entidades.Categoria;
import Entidades.Marca;
import Entidades.ProdutoDerivacao;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(Produto.class)
public class Produto_ { 

    public static volatile SingularAttribute<Produto, Marca> marca;
    public static volatile SingularAttribute<Produto, Double> valorUnitarioCompra;
    public static volatile SingularAttribute<Produto, Boolean> ativo;
    public static volatile SingularAttribute<Produto, Double> valorUnitarioVenda;
    public static volatile SingularAttribute<Produto, Categoria> categoria;
    public static volatile SingularAttribute<Produto, String> ncm;
    public static volatile SingularAttribute<Produto, Long> id;
    public static volatile SingularAttribute<Produto, Double> cont;
    public static volatile ListAttribute<Produto, ProdutoDerivacao> variacoes;

}