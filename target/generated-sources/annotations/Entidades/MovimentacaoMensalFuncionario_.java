package Entidades;

import Entidades.Enums.TipoBonus;
import Entidades.FolhaPagamento;
import Entidades.Pessoa;
import Entidades.Venda;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(MovimentacaoMensalFuncionario.class)
public class MovimentacaoMensalFuncionario_ { 

    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Venda> venda;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, FolhaPagamento> folhaPagamento;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Date> data;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Double> bonus;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Double> porcentagemCom;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Double> valorHora;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Long> id;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, Pessoa> funcionario;
    public static volatile SingularAttribute<MovimentacaoMensalFuncionario, TipoBonus> tipoBonus;

}