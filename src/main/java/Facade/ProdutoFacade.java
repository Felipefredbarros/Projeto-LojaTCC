/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author felip
 */
@Stateless
public class ProdutoFacade extends AbstractFacade<Produto> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProdutoFacade() {
        super(Produto.class);
    }

    // Total de produtos cadastrados
    public Long totalProdutosCadastrados() {
        String jpql = "SELECT COUNT(p) FROM Produto p";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }
    
    public Long totalProdutosCadastradosAtivos() {
        String jpql = "SELECT COUNT(p) FROM Produto p WHERE p.ativo = true";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

// Produtos com estoque abaixo de 5
    public List<ProdutoDerivacao> listarProdutosEstoqueBaixo() {
        String jpql = "SELECT d FROM ProdutoDerivacao d WHERE d.quantidade < 5 AND d.produto.ativo = true";
        return em.createQuery(jpql, ProdutoDerivacao.class).getResultList();
    }

// Quantidade total de estoque
    public Double totalEstoqueGeral() {
        String jpql = "SELECT SUM(d.quantidade) FROM ProdutoDerivacao d WHERE d.produto.ativo = true";
        Double total = em.createQuery(jpql, Double.class).getSingleResult();
        return total != null ? total : 0.0;
    }

    public List<Produto> listaFiltrar(String filtro, String... atributos) {
        String hql = "from Produto obj where ";
        for (String atributo : atributos) {
            if ("categoria".equals(atributo)) {
                hql += "lower(obj.categoria.catagoria) like :filtro OR ";
            } else {
                hql += "lower(obj." + atributo + ") like :filtro OR ";
            }
        }
        hql = hql.substring(0, hql.length() - 3);  // Remove o último "OR"
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("filtro", "%" + filtro.toLowerCase() + "%");
        return q.getResultList();
    }

    public boolean produtoTemVendas(Long produtoId) {
        Query query = em.createQuery("SELECT COUNT(v) FROM Venda v JOIN v.itensVenda i WHERE i.produtoDerivacao.produto.id = :produtoId");
        query.setParameter("produtoId", produtoId);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    public boolean produtoTemCompras(Long produtoId) {
        Query query = em.createQuery("SELECT COUNT(c) FROM Compra c JOIN c.itensCompra i WHERE i.produtoDerivacao.produto.id = :produtoId");
        query.setParameter("produtoId", produtoId);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    public List<Produto> listarProdutosAtivos() {
        TypedQuery<Produto> query = em.createQuery("SELECT DISTINCT  p FROM Produto p LEFT JOIN FETCH p.variacoes WHERE p.ativo = true", Produto.class);
        return query.getResultList();
    }

    public List<Produto> listarProdutosInativos() {
        TypedQuery<Produto> query = em.createQuery("SELECT DISTINCT  p FROM Produto p LEFT JOIN FETCH p.variacoes WHERE p.ativo = false", Produto.class);
        return query.getResultList();
    }

    public List<Produto> listaFiltrada(String filtro) {
        String jpql = "SELECT p FROM Produto p WHERE LOWER(p.categoria.categoria) LIKE :filtro";
        return em.createQuery(jpql, Produto.class)
                .setParameter("filtro", "%" + filtro.toLowerCase() + "%")
                .getResultList();
    }

    public Produto findWithDerivacoes(Long id) {
        return em.createQuery(
                "SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.variacoes WHERE p.id = :id",
                Produto.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<ProdutoDerivacao> listarProdutosDerivacoesAtivas() {
        String jpql = "SELECT pd FROM ProdutoDerivacao pd WHERE pd.produto.ativo = true";
        return em.createQuery(jpql, ProdutoDerivacao.class).getResultList();
    }

}
