package Entidades;

import Entidades.ContratoTrabalho;
import Entidades.Endereco;
import Entidades.Enums.TipoPessoa;
import Entidades.FolhaPagamento;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Telefone;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2025-09-10T19:56:43")
@StaticMetamodel(Pessoa.class)
public class Pessoa_ { 

    public static volatile SetAttribute<Pessoa, Telefone> listaTelefones;
    public static volatile SingularAttribute<Pessoa, String> tipoPessoa;
    public static volatile SingularAttribute<Pessoa, String> telefone;
    public static volatile SingularAttribute<Pessoa, TipoPessoa> tipo;
    public static volatile SingularAttribute<Pessoa, Boolean> ativo;
    public static volatile SingularAttribute<Pessoa, String> endereco;
    public static volatile SingularAttribute<Pessoa, String> telefonePrincipal;
    public static volatile SetAttribute<Pessoa, Endereco> listaEnderecos;
    public static volatile SingularAttribute<Pessoa, ContratoTrabalho> contrato;
    public static volatile SingularAttribute<Pessoa, String> nome;
    public static volatile SingularAttribute<Pessoa, String> regiao;
    public static volatile SingularAttribute<Pessoa, String> cpfcnpj;
    public static volatile ListAttribute<Pessoa, MovimentacaoMensalFuncionario> movimentacoes;
    public static volatile SingularAttribute<Pessoa, String> enderecoPrincipal;
    public static volatile ListAttribute<Pessoa, FolhaPagamento> folhasPagamento;
    public static volatile SingularAttribute<Pessoa, Long> id;
    public static volatile SingularAttribute<Pessoa, String> email;

}