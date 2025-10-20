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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
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

    public List<LancamentoFinanceiro> buscarPorContaEPeriodoOrdenado(Conta conta, Date ini, Date fim) {
        StringBuilder jpql = new StringBuilder("SELECT l FROM LancamentoFinanceiro l WHERE l.conta = :conta ");

        if (ini != null) {
            jpql.append(" AND l.dataHora >= :dataInicio ");
        }
        if (fim != null) {
            jpql.append(" AND l.dataHora <= :dataFim ");
        }
        jpql.append(" ORDER BY l.dataHora ASC, l.id ASC");

        TypedQuery<LancamentoFinanceiro> query = em.createQuery(jpql.toString(), LancamentoFinanceiro.class);
        query.setParameter("conta", conta);

        if (ini != null) {
            query.setParameter("dataInicio", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            query.setParameter("dataFim", ajustarDataFim(fim), TemporalType.TIMESTAMP);
        }
        return query.getResultList();
    }

    public Double calcularSaldoAnteriorAData(Conta conta, Date dataInicioPeriodo) {
        if (conta == null || conta.getId() == null) {
            return 0.0;
        }
        Double valorInicial = conta.getValorInicial() != null ? conta.getValorInicial() : 0.0;

        if (dataInicioPeriodo == null) {
            return valorInicial;
        }
        Date inicioDoDia = obterInicioDoDia(dataInicioPeriodo); 
        Double entradasAnteriores = somarPorContaETipoEPeriodoCerto(conta, TipoLancamento.ENTRADA, null, inicioDoDia, false);
        Double saidasAnteriores = somarPorContaETipoEPeriodoCerto(conta, TipoLancamento.SAIDA, null, inicioDoDia, false);

        return valorInicial + (entradasAnteriores != null ? entradasAnteriores : 0.0) - (saidasAnteriores != null ? saidasAnteriores : 0.0);
    }

    public Double somarPorContaETipoEPeriodoCerto(Conta conta, TipoLancamento tipo, Date ini, Date fim, boolean incluirDataFim) {
        String jpql = "SELECT COALESCE(SUM(l.valor), 0) "
                + "FROM LancamentoFinanceiro l "
                + "WHERE l.conta = :conta "
                + "  AND l.tipo = :tipo "
                + "  AND (l.status IS NULL OR l.status <> :statusEstornado) "
                + "  AND (l.descricao IS NULL OR UPPER(l.descricao) NOT LIKE 'ESTORNO%') "
                + (ini != null ? " AND l.dataHora >= :ini" : "")
                + (fim != null ? (incluirDataFim ? " AND l.dataHora <= :fim" : " AND l.dataHora < :fim") : "");

        TypedQuery<Double> q = em.createQuery(jpql, Double.class);
        q.setParameter("conta", conta);
        q.setParameter("tipo", tipo);
        q.setParameter("statusEstornado", StatusLancamento.ESTORNADO);
        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fim", incluirDataFim ? ajustarDataFim(fim) : fim, TemporalType.TIMESTAMP);
        }

        Double resultado = q.getSingleResult();
        return resultado != null ? resultado : 0.0;
    }

    private Date ajustarDataFim(Date dataFim) {
        if (dataFim == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataFim);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private Date obterInicioDoDia(Date data) {
        if (data == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}
