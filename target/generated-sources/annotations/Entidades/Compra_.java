package Entidades;

import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.PlanoPagamento;
import Entidades.ItensCompra;
import Entidades.ParcelaCompra;
import Entidades.Pessoa;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(Compra.class)
public class Compra_ { 

    public static volatile SingularAttribute<Compra, Integer> parcelas;
    public static volatile SingularAttribute<Compra, MetodoPagamento> metodoPagamento;
    public static volatile SingularAttribute<Compra, PlanoPagamento> planoPagamento;
    public static volatile SingularAttribute<Compra, Double> valorTotal;
    public static volatile SingularAttribute<Compra, Date> dataVencimento;
    public static volatile SingularAttribute<Compra, Long> id;
    public static volatile SingularAttribute<Compra, Pessoa> fornecedor;
    public static volatile ListAttribute<Compra, ItensCompra> itensCompra;
    public static volatile ListAttribute<Compra, ParcelaCompra> parcelasCompra;
    public static volatile SingularAttribute<Compra, Date> dataCompra;

}