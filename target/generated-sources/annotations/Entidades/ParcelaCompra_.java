package Entidades;

import Entidades.Compra;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:44")
@StaticMetamodel(ParcelaCompra.class)
public class ParcelaCompra_ { 

    public static volatile SingularAttribute<ParcelaCompra, Compra> compra;
    public static volatile SingularAttribute<ParcelaCompra, Double> valorParcela;
    public static volatile SingularAttribute<ParcelaCompra, Date> dataVencimento;
    public static volatile SingularAttribute<ParcelaCompra, Long> id;

}