/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.ContaReceber;
import Entidades.Venda;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author felip
 */
@Stateless
public class ContaReceberFacade extends AbstractFacade<ContaReceber> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContaReceberFacade() {
        super(ContaReceber.class);
    }

    public void receberConta(ContaReceber conta) {
        if (conta == null || conta.getId() == null) {
            throw new IllegalArgumentException("Conta inválida");
        }

        if (!"ABERTA".equals(conta.getStatus())) {
            throw new IllegalStateException("A conta não está em aberto");
        }
        conta.setStatus("RECEBIDA");
        conta.setDataRecebimento(new Date());
        em.merge(conta);

        // --- Lançamento financeiro do recebimento ---
        /*
        LancamentoFinanceiro lanc = new LancamentoFinanceiro();
        lanc.setTipo("ENTRADA");
        lanc.setDescricao("Recebimento da conta ID:" + conta.getId() 
                          + " (Venda ID:" + conta.getVenda().getId() + ")");
        lanc.setValor(conta.getValor());
        lanc.setData(new Date());
        lanc.setContaReceber(conta);

        lancamentoFinanceiroFacade.salvar(lanc);
        contaBancariaFacade.atualizarSaldo(lanc.getValor());
         */
    }

    public List<ContaReceber> listaTodosReais() {
        Query q = getEntityManager().createQuery(
                "FROM ContaReceber v WHERE v.status NOT IN ('CANCELADA') ORDER BY v.id DESC"
        );
        return q.getResultList();
    }

    public List<ContaReceber> listaTodosCanceladas() {
        return em.createQuery(
                "FROM ContaReceber v WHERE v.status IN ('CANCELADA') ORDER BY v.id DESC",
                ContaReceber.class
        ).getResultList();
    }

    public ContaReceber buscarAvistaDaVenda(Venda venda) {
        List<ContaReceber> list = em.createQuery(
                "select c from ContaReceber c "
                + "where c.venda = :v and c.status = 'RECEBIDA' "
                + "order by c.id desc", ContaReceber.class)
                .setParameter("v", venda)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public ContaReceber findWithLancamentos(Long id) {
        return em.createQuery(
                "select distinct cp from ContaReceber cp "
                + "left join fetch cp.lancamentos "
                + "where cp.id = :id", ContaReceber.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    // ContaReceberFacade
    public ContaReceber findAvistaByVendaIdWithLancs(Long vendaId) {
        List<ContaReceber> list = em.createQuery(
                "select cr from ContaReceber cr "
                + "left join fetch cr.lancamentos l "
                + "where cr.venda.id = :vid and cr.status = 'RECEBIDA' "
                + // à vista você marcou como RECEBIDA
                "order by cr.id desc", ContaReceber.class)
                .setParameter("vid", vendaId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

}
