package Entidades;

import Entidades.Caixa;
import Entidades.Compra;
import Entidades.Enums.TipoMovimentacao;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(MovimentacaoCaixa.class)
public class MovimentacaoCaixa_ { 

    public static volatile SingularAttribute<MovimentacaoCaixa, TipoMovimentacao> tipo;
    public static volatile SingularAttribute<MovimentacaoCaixa, Compra> venda;
    public static volatile SingularAttribute<MovimentacaoCaixa, Caixa> caixa;
    public static volatile SingularAttribute<MovimentacaoCaixa, Double> valor;
    public static volatile SingularAttribute<MovimentacaoCaixa, Long> id;
    public static volatile SingularAttribute<MovimentacaoCaixa, LocalDateTime> dataMovimentacao;
    public static volatile SingularAttribute<MovimentacaoCaixa, String> descricao;

}