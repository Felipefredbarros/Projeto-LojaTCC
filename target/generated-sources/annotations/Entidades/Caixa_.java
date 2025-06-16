package Entidades;

import Entidades.MovimentacaoCaixa;
import Entidades.Pessoa;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-06-16T00:41:31")
@StaticMetamodel(Caixa.class)
public class Caixa_ { 

    public static volatile SingularAttribute<Caixa, LocalDateTime> data_abertura;
    public static volatile SingularAttribute<Caixa, Pessoa> criouCaixa;
    public static volatile SingularAttribute<Caixa, Boolean> fechado;
    public static volatile ListAttribute<Caixa, MovimentacaoCaixa> movimentacoes;
    public static volatile SingularAttribute<Caixa, Double> valor;
    public static volatile SingularAttribute<Caixa, LocalDateTime> data_fechamento;
    public static volatile SingularAttribute<Caixa, Long> id;

}