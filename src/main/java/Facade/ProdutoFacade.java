/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import dto.ProdutoRankingDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
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

    public Long totalProdutosCadastrados() {
        String jpql = "SELECT COUNT(p) FROM Produto p";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long totalProdutosCadastradosAtivos() {
        String jpql = "SELECT COUNT(p) FROM Produto p WHERE p.ativo = true";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public List<ProdutoDerivacao> listarProdutosEstoqueBaixo() {
        String jpql = "SELECT d FROM ProdutoDerivacao d WHERE d.quantidade < 3 AND d.produto.ativo = true";
        return em.createQuery(jpql, ProdutoDerivacao.class).getResultList();
    }

    public Double totalEstoqueGeral() {
        String jpql = "SELECT SUM(d.quantidade) FROM ProdutoDerivacao d WHERE d.produto.ativo = true";
        Double total = em.createQuery(jpql, Double.class).getSingleResult();
        return total != null ? total : 0.0;
    }

    public List<Produto> listaFiltrar(String filtro, String... atributos) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return getEntityManager().createQuery("FROM Produto", Produto.class).getResultList();
        }

        StringBuilder hql = new StringBuilder("FROM Produto obj WHERE ");
        for (String atributo : atributos) {
            switch (atributo) {
                case "categoria":
                    hql.append("LOWER(obj.categoria.categoria) LIKE :filtro OR ");
                    break;
                case "marca":
                    hql.append("LOWER(obj.marca.marca) LIKE :filtro OR ");
                    break;
                case "ncm":
                    hql.append("LOWER(obj.ncm) LIKE :filtro OR ");
                    break;
                default:
                    break;
            }
        }

        int posOr = hql.lastIndexOf(" OR ");
        String hqlFinal = (posOr > 0) ? hql.substring(0, posOr) : "FROM Produto";

        Query q = getEntityManager().createQuery(hqlFinal);
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
    

    public List<ProdutoRankingDTO> topMaisVendidos(Date ini, Date fim, int limit) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p, SUM(iv.quantidade)"
                + "FROM ItensVenda iv "
                + "JOIN iv.venda v "
                + "JOIN iv.produtoDerivacao pd "
                + "JOIN pd.produto p "
                + "WHERE v.status = 'FECHADA' "
        );
        if (ini != null) {
            jpql.append(" AND v.dataVenda >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND v.dataVenda <= :fim ");
        }
        jpql.append("GROUP BY p ");
        jpql.append("ORDER BY SUM(iv.quantidade) DESC ");

        javax.persistence.Query q = em.createQuery(jpql.toString());
        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fim", fim, TemporalType.TIMESTAMP);
        }
        q.setMaxResults(limit > 0 ? limit : 10);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<ProdutoRankingDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new ProdutoRankingDTO(
                    (Produto) r[0],
                    ((Number) r[1]).doubleValue(),
                    r[2] != null ? ((Number) r[2]).doubleValue() : 0d
            ));
        }
        return out;
    }

     public List<ProdutoRankingDTO> topMaisComprados(Date ini, Date fim, int limit) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p, SUM(ic.quantidade)"
                + "FROM ItensCompra ic "
                + "JOIN ic.compra c "
                + "JOIN ic.produtoDerivacao pd "
                + "JOIN pd.produto p "
                + "WHERE c.status = 'FECHADA' "
        );
        if (ini != null) {
            jpql.append(" AND c.dataCompra >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND c.dataCompra <= :fim ");
        }
        jpql.append("GROUP BY p ");
        jpql.append("ORDER BY SUM(ic.quantidade) DESC ");

        javax.persistence.Query q = em.createQuery(jpql.toString());
        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fim", fim, TemporalType.TIMESTAMP);
        }
        q.setMaxResults(limit > 0 ? limit : 10);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<ProdutoRankingDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new ProdutoRankingDTO(
                    (Produto) r[0],
                    ((Number) r[1]).doubleValue(),
                    r[2] != null ? ((Number) r[2]).doubleValue() : 0d
            ));
        }
        return out;
    }

}
