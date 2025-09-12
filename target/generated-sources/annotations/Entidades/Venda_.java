package Entidades;

import Entidades.ContaReceber;
import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.PlanoPagamento;
import Entidades.ItensVenda;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Pessoa;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:44")
@StaticMetamodel(Venda.class)
public class Venda_ { 

    public static volatile SingularAttribute<Venda, MovimentacaoMensalFuncionario> movimentacao;
    public static volatile SingularAttribute<Venda, Pessoa> cliente;
    public static volatile SingularAttribute<Venda, Date> dataVenda;
    public static volatile ListAttribute<Venda, ItensVenda> itensVenda;
    public static volatile SingularAttribute<Venda, MetodoPagamento> metodoPagamento;
    public static volatile SingularAttribute<Venda, PlanoPagamento> planoPagamento;
    public static volatile SingularAttribute<Venda, Double> valorTotal;
    public static volatile SingularAttribute<Venda, Date> dataVencimento;
    public static volatile ListAttribute<Venda, ContaReceber> contasReceber;
    public static volatile SingularAttribute<Venda, Long> id;
    public static volatile SingularAttribute<Venda, Pessoa> funcionario;
    public static volatile SingularAttribute<Venda, String> status;

}