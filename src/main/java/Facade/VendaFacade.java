/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.ItensVenda;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author felip
 */
@Stateless
public class VendaFacade extends AbstractFacade<Venda> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    // Total de vendas cadastrados
    public Long totalVendasCadastradas() {
        String jpql = "SELECT COUNT(v) FROM Venda v";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    // Valor total somado de todas as vendas
    public Double valorVendaVendido() {
        String jpql = "SELECT SUM(v.valorTotal) FROM Venda v";
        Double result = em.createQuery(jpql, Double.class).getSingleResult();
        return result != null ? result : 0.0;  // Se o resultado for null, retorna 0.0
    }

    public void salvarVenda(Venda entity, Boolean edit) {
        if (!edit) {
            for (ItensVenda iv : entity.getItensVenda()) {
                ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
                derivacao.setQuantidade(derivacao.getQuantidade() - iv.getQuantidade());
                em.merge(derivacao);
                iv.setDesc(derivacao.getProduto().getTexto());
            }
        }

        em.merge(entity);
    }

    @Override
    public void remover(Venda entity) {
        entity = em.find(Venda.class, entity.getId());
        entity.getItensVenda().size(); // Força carregar itens

        for (ItensVenda iv : entity.getItensVenda()) {
            ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
            derivacao.setQuantidade(derivacao.getQuantidade()+ iv.getQuantidade());
            em.merge(derivacao);
        }

        super.remover(entity);
    }

    public Venda findWithItens(Long id) {
        return em.createQuery("SELECT v FROM Venda v LEFT JOIN FETCH v.itensVenda WHERE v.id = :id", Venda.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Venda> listaTodosComItens() {
        return em.createQuery("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itensVenda", Venda.class).getResultList();
    }

    public VendaFacade() {
        super(Venda.class);
    }

}
