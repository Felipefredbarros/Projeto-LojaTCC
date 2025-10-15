/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.ContaPagar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
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
public class ContaPagarFacade extends AbstractFacade<ContaPagar> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContaPagarFacade() {
        super(ContaPagar.class);
    }

    public void pagarConta(ContaPagar conta) {
        if (conta == null || conta.getId() == null) {
            throw new IllegalArgumentException("Conta inválida");
        }

        if (!"ABERTA".equals(conta.getStatus())) {
            throw new IllegalStateException("A conta não está em aberto");
        }
        em.merge(conta);
    }

    public List<ContaPagar> listaTodosReais() {
        Query q = getEntityManager().createQuery(
                "FROM ContaPagar v WHERE v.status NOT IN ('CANCELADA') ORDER BY v.id DESC"
        );
        return q.getResultList();
    }

    public List<ContaPagar> listaTodosCanceladas() {
        return em.createQuery(
                "FROM ContaPagar v WHERE v.status IN ('CANCELADA', 'ESTORNADA') ORDER BY v.id DESC",
                ContaPagar.class
        ).getResultList();
    }

    public ContaPagar findWithLancamentos(Long id) {
        return em.createQuery(
                "select distinct cp from ContaPagar cp "
                + "left join fetch cp.lancamentos "
                + "where cp.id = :id", ContaPagar.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public double findTotalAbertoPorMes(int mes, int ano) {
        LocalDate inicioLD = LocalDate.of(ano, mes, 1);
        LocalDate fimLD = inicioLD.plusMonths(1);

        Date inicio = Date.from(inicioLD.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date fim = Date.from(fimLD.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String jpql
                = "SELECT COALESCE(SUM(cr.valor), 0) "
                + "FROM ContaPagar cr "
                + "WHERE cr.status IN ('ABERTA') "
                + // ajuste se usar Enum
                "  AND cr.dataVencimento >= :inicio "
                + "  AND cr.dataVencimento < :fim";

        Double total = em.createQuery(jpql, Double.class)
                .setParameter("inicio", inicio, TemporalType.TIMESTAMP)
                .setParameter("fim", fim, TemporalType.TIMESTAMP)
                .getSingleResult();

        return total != null ? total : 0.0;
    }

    public List<ContaPagar> findContasEntreDatas(Date dataInicial, Date dataFinal) {
        String jpql = "SELECT cp FROM ContaPagar cp "
                + "WHERE cp.status IN ('ABERTA') "
                + // ajuste conforme seus status
                "  AND cp.dataVencimento BETWEEN :inicio AND :fim "
                + "ORDER BY cp.dataVencimento ASC";

        return em.createQuery(jpql, ContaPagar.class)
                .setParameter("inicio", dataInicial, TemporalType.TIMESTAMP)
                .setParameter("fim", dataFinal, TemporalType.TIMESTAMP)
                .getResultList();
    }

    public List<ContaPagar> buscarPorFiltrosPagas(Date ini, Date fim) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c FROM ContaPagar c "
                + "WHERE c.status = 'PAGA' "
        );

        if (ini != null) {
            jpql.append(" AND c.dataRecebimento >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND c.dataRecebimento < :fimExclusivo ");
        }

        jpql.append(" ORDER BY c.id DESC ");

        TypedQuery<ContaPagar> q = em.createQuery(jpql.toString(), ContaPagar.class);

        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fimExclusivo", proximoDia(fim), TemporalType.TIMESTAMP);
        }

        return q.getResultList();
    }

    public List<ContaPagar> buscarPorFiltrosPagar(Date ini, Date fim) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c FROM ContaPagar c "
                + "WHERE c.status = 'ABERTA' "
        );

        if (ini != null) {
            jpql.append(" AND c.dataVencimento >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND c.dataVencimento < :fimExclusivo ");
        }

        jpql.append(" ORDER BY c.id DESC ");

        TypedQuery<ContaPagar> q = em.createQuery(jpql.toString(), ContaPagar.class);

        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fimExclusivo", proximoDia(fim), TemporalType.TIMESTAMP);
        }

        return q.getResultList();
    }

    private Date proximoDia(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
