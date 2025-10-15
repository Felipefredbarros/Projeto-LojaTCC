/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Conta;
import Entidades.ContaPagar;
import Entidades.ContaReceber;
import Entidades.Enums.StatusLancamento;
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
                + "ORDER BY l.id DESC";
        return em.createQuery(jpql, LancamentoFinanceiro.class)
                .setParameter("conta", conta)
                .getResultList();
    }

    public Double somarPorContaETipoEPeriodo(Conta conta, TipoLancamento tipo, Date ini, Date fim) {
        String jpql = "SELECT COALESCE(SUM(l.valor), 0) "
                + "FROM LancamentoFinanceiro l "
                + "WHERE l.conta = :conta "
                + "  AND l.tipo = :tipo "
                + // Regra principal: ignora estornados
                "  AND (l.status IS NULL OR l.status <> :statusEstornado) "
                + // Blindagem extra por descrição (casos antigos sem status marcado)
                "  AND (l.descricao IS NULL OR UPPER(l.descricao) NOT LIKE 'ESTORNO%') "
                + (ini != null ? " AND l.dataHora >= :ini" : "")
                + (fim != null ? " AND l.dataHora <= :fim" : "");

        TypedQuery<Double> q = em.createQuery(jpql, Double.class);
        q.setParameter("conta", conta);
        q.setParameter("tipo", tipo);
        q.setParameter("statusEstornado", Entidades.Enums.StatusLancamento.ESTORNADO);
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

    public LancamentoFinanceiro buscarOriginalPagamento(ContaPagar cp) {
        List<LancamentoFinanceiro> list = em.createQuery(
                "select l from LancamentoFinanceiro l "
                + "where l.contaPagar = :cp and l.tipo = :tipo and l.status = :st "
                + "order by l.dataHora asc", LancamentoFinanceiro.class)
                .setParameter("cp", cp)
                .setParameter("tipo", TipoLancamento.SAIDA)
                .setParameter("st", StatusLancamento.NORMAL)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public LancamentoFinanceiro buscarOriginalRecebimento(ContaReceber cp) {
        List<LancamentoFinanceiro> list = em.createQuery(
                "select l from LancamentoFinanceiro l "
                + "where l.contaReceber = :cp and l.tipo = :tipo and l.status = :st "
                + "order by l.dataHora asc", LancamentoFinanceiro.class)
                .setParameter("cp", cp)
                .setParameter("tipo", TipoLancamento.ENTRADA)
                .setParameter("st", StatusLancamento.NORMAL)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

}
