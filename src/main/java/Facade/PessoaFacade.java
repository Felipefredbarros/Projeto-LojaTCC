/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Pessoa;
import Entidades.Enums.TipoPessoa;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 *
 * @author felip
 */
@Stateless
public class PessoaFacade extends AbstractFacade<Pessoa> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PessoaFacade() {
        super(Pessoa.class);
    }

    public Pessoa findWithAll(Long id) {
        System.out.println("PessoaFacade.findWithAll - Buscando ID: " + id);
        try {
            Pessoa p = em.createQuery(
                    "SELECT DISTINCT p FROM Pessoa p "
                    + "LEFT JOIN FETCH p.listaEnderecos e "
                    + "LEFT JOIN FETCH e.cidade c "
                    + "LEFT JOIN FETCH c.estado "
                    + "LEFT JOIN FETCH p.listaTelefones "
                    + "WHERE p.id = :id",
                    Pessoa.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return p;
        } catch (javax.persistence.NoResultException nre) {
            return null;
        }
    }

    public List<Pessoa> listaPessoaAtivo() {
        Query q = getEntityManager().createQuery("from Pessoa as p where p.ativo = true order by p.id desc");
        return q.getResultList();
    }

    public List<Pessoa> listaPessoaInativo() {
        Query q = getEntityManager().createQuery("from Pessoa as p where p.ativo = false order by p.id desc");
        return q.getResultList();
    }

    public List<Pessoa> listaGeralAtivo() {
        String query = "SELECT DISTINCT p FROM Pessoa p "
                + "LEFT JOIN FETCH p.listaEnderecos "
                + "LEFT JOIN FETCH p.listaTelefones "
                + "WHERE p.ativo = true";
        return getEntityManager().createQuery(query, Pessoa.class)
                .getResultList();
    }

    private List<Pessoa> listaPessoasAtivasPorTipo(TipoPessoa tipo) {
        String query = "SELECT DISTINCT p FROM Pessoa p "
                + "LEFT JOIN FETCH p.listaEnderecos e "
                + "LEFT JOIN FETCH e.cidade c "
                + "LEFT JOIN FETCH c.estado "
                + "LEFT JOIN FETCH p.listaTelefones "
                + "WHERE p.ativo = true AND p.tipo = :tipo";

        return getEntityManager().createQuery(query, Pessoa.class)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    public List<Pessoa> listaCliAtivo() {
        return listaPessoasAtivasPorTipo(TipoPessoa.CLIENTE);
    }

    public List<Pessoa> listaFuncAtivo() {
        return listaPessoasAtivasPorTipo(TipoPessoa.FUNCIONARIO);
    }

    public List<Pessoa> listaFornAtivo() {
        return listaPessoasAtivasPorTipo(TipoPessoa.FORNECEDOR);
    }

    public List<Pessoa> listaPorTipoAtivo(TipoPessoa tipo) {
        Query q = getEntityManager().createQuery("from Pessoa as p where p.tipo = :tipo and p.ativo = true order by p.id desc");
        q.setParameter("tipo", tipo);
        return q.getResultList();
    }

    public List<Pessoa> listaClienteAtivo() {
        return listaPorTipoAtivo(TipoPessoa.CLIENTE);
    }

    public List<Pessoa> listaFornecedorAtivo() {
        return listaPorTipoAtivo(TipoPessoa.FORNECEDOR);
    }

    public List<Pessoa> listaFuncionarioAtivo() {
        return listaPorTipoAtivo(TipoPessoa.FUNCIONARIO);
    }

//INATIVOS
    public List<Pessoa> listaPorTipoInativo(TipoPessoa tipo) {
        Query q = getEntityManager().createQuery("from Pessoa as p where p.tipo = :tipo and p.ativo = false order by p.id desc");
        q.setParameter("tipo", tipo);
        return q.getResultList();
    }

    public List<Pessoa> listaClienteInativo() {
        return listaPorTipoInativo(TipoPessoa.CLIENTE);
    }

    public List<Pessoa> listaFornecedorInativo() {
        return listaPorTipoInativo(TipoPessoa.FORNECEDOR);
    }

    public List<Pessoa> listaFuncionarioInativo() {
        return listaPorTipoInativo(TipoPessoa.FUNCIONARIO);
    }

    public boolean pessoaTemVinculos(Pessoa pessoa) {
        if (pessoa == null || pessoa.getId() == null) {
            return false;
        }

        Long count = 0L;

        if (pessoa.getTipo() == TipoPessoa.CLIENTE) {
            count = (Long) em.createQuery("SELECT COUNT(v) FROM Venda v WHERE v.cliente.id = :id")
                    .setParameter("id", pessoa.getId())
                    .getSingleResult();
        } else if (pessoa.getTipo() == TipoPessoa.FUNCIONARIO) {
            Long vendas = (Long) em.createQuery("SELECT COUNT(v) FROM Venda v WHERE v.funcionario.id = :id")
                    .setParameter("id", pessoa.getId())
                    .getSingleResult();

            Long movimentacoes = (Long) em.createQuery("SELECT COUNT(m) FROM MovimentacaoMensalFuncionario m WHERE m.funcionario.id = :id")
                    .setParameter("id", pessoa.getId())
                    .getSingleResult();

            Long folhas = (Long) em.createQuery("SELECT COUNT(f) FROM FolhaPagamento f WHERE f.funcionario.id = :id")
                    .setParameter("id", pessoa.getId())
                    .getSingleResult();

            count = vendas + movimentacoes + folhas;
        } else if (pessoa.getTipo() == TipoPessoa.FORNECEDOR) {
            count = (Long) em.createQuery("SELECT COUNT(c) FROM Compra c WHERE c.fornecedor.id = :id")
                    .setParameter("id", pessoa.getId())
                    .getSingleResult();
        }

        return count > 0;
    }

    public List<Pessoa> listaFornecedoresFisicos() {
        List<Pessoa> fornecedores = listaFornecedorAtivo();
        List<Pessoa> fornecedoresFisicos = new ArrayList<>();

        for (Pessoa fornecedor : fornecedores) {
            if ("FISICA".equals(fornecedor.getTipoPessoa())) {
                fornecedoresFisicos.add(fornecedor);
            }
        }
        return fornecedoresFisicos;
    }

    public List<Pessoa> listaFornecedoresJuridicos() {
        List<Pessoa> fornecedores = listaFornecedorAtivo();
        List<Pessoa> fornecedoresJuridicos = new ArrayList<>();

        for (Pessoa fornecedor : fornecedores) {
            if ("JURIDICA".equals(fornecedor.getTipoPessoa())) {
                fornecedoresJuridicos.add(fornecedor);
            }
        }
        return fornecedoresJuridicos;
    }

    public List<Pessoa> listaFiltrandoPorTipo(TipoPessoa tipo, String filtro, String... atributos) {
        String hql = "from Pessoa as p where p.tipo = :tipo AND p.ativo = true AND (";
        for (String atributo : atributos) {
            hql += "lower(p." + atributo + ") like :filtro OR ";
        }
        hql = hql.substring(0, hql.length() - 4) + ")"; // Remove o último " OR "
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("tipo", tipo);
        q.setParameter("filtro", "%" + filtro.toLowerCase() + "%");
        return q.getResultList();
    }

    public List<Pessoa> listaClienteFiltrando(String filtro, String... atributos) {
        return listaFiltrandoPorTipo(TipoPessoa.CLIENTE, filtro, atributos);
    }

    public List<Pessoa> listaFornecedorFiltrando(String filtro, String... atributos) {
        return listaFiltrandoPorTipo(TipoPessoa.FORNECEDOR, filtro, atributos);
    }

    public List<Pessoa> listaFuncionarioFiltrando(String filtro, String... atributos) {
        return listaFiltrandoPorTipo(TipoPessoa.FUNCIONARIO, filtro, atributos);
    }
}
