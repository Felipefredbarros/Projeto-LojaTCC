/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Conta;
import Entidades.Enums.TipoLancamento;
import Entidades.LancamentoFinanceiro;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author felip
 */
@Stateless
public class LancamentoFinanceiroFacade extends AbstractFacade<LancamentoFinanceiro> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LancamentoFinanceiroFacade() {
        super(LancamentoFinanceiro.class);
    }

    public List<LancamentoFinanceiro> listarPorConta(Conta conta) {
        return em.createQuery("SELECT l FROM LancamentoFinanceiro l WHERE l.conta = :conta", LancamentoFinanceiro.class)
                .setParameter("conta", conta)
                .getResultList();
    }

    public List<LancamentoFinanceiro> buscarPorConta(Conta conta) {
        String jpql = "SELECT l FROM LancamentoFinanceiro l "
                + "WHERE l.conta = :conta "
                + "ORDER BY l.dataHora DESC";
        return em.createQuery(jpql, LancamentoFinanceiro.class)
                .setParameter("conta", conta)
                .getResultList();
    }

    public Double somarPorContaETipoEPeriodo(Conta conta, TipoLancamento tipo, Date ini, Date fim) {
        String jpql = "SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l WHERE l.conta = :conta AND l.tipo = :tipo"
                + (ini != null ? " AND l.dataHora >= :ini" : "")
                + (fim != null ? " AND l.dataHora <= :fim" : "");

        TypedQuery<Double> q = em.createQuery(jpql, Double.class);
        q.setParameter("conta", conta);
        q.setParameter("tipo", tipo);
        if (ini != null) {
            q.setParameter("ini", ini);
        }
        if (fim != null) {
            q.setParameter("fim", fim);
        }

        Double resultado = q.getSingleResult();
        return resultado != null ? resultado : 0.0;
    }

    public LancamentoFinanceiro buscarUltimoPorConta(Conta conta) {
        String jpql = "SELECT l FROM LancamentoFinanceiro l WHERE l.conta = :conta ORDER BY l.dataHora DESC";
        TypedQuery<LancamentoFinanceiro> q = em.createQuery(jpql, LancamentoFinanceiro.class);
        q.setParameter("conta", conta);
        q.setMaxResults(1);

        List<LancamentoFinanceiro> resultados = q.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

}
