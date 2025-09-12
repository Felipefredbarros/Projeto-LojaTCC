package Entidades;

import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Pessoa;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:44")
@StaticMetamodel(FolhaPagamento.class)
public class FolhaPagamento_ { 

    public static volatile SingularAttribute<FolhaPagamento, Double> salarioLiquido;
    public static volatile ListAttribute<FolhaPagamento, MovimentacaoMensalFuncionario> movimentacoes;
    public static volatile SingularAttribute<FolhaPagamento, Double> comissao;
    public static volatile SingularAttribute<FolhaPagamento, Double> inss;
    public static volatile SingularAttribute<FolhaPagamento, Double> salarioBase;
    public static volatile SingularAttribute<FolhaPagamento, Long> id;
    public static volatile SingularAttribute<FolhaPagamento, Pessoa> funcionario;
    public static volatile SingularAttribute<FolhaPagamento, Double> adicional;
    public static volatile SingularAttribute<FolhaPagamento, Double> irrf;
    public static volatile SingularAttribute<FolhaPagamento, Date> competencia;
    public static volatile SingularAttribute<FolhaPagamento, Double> fgts;
    public static volatile SingularAttribute<FolhaPagamento, Date> dataGeracao;

}