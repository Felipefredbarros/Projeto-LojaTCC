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
import javax.ejb.EJB;
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
public class ContaFacade extends AbstractFacade<Conta> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @EJB
    private LancamentoFinanceiroFacade lancFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContaFacade() {
        super(Conta.class);
    }

    public List<Conta> listaContaAtivo() {
        Query q = getEntityManager().createQuery("from Conta as p where p.ativo = true order by p.id desc");
        return q.getResultList();
    }

    public List<Conta> listaContaInativo() {
        Query q = getEntityManager().createQuery("from Conta as p where p.ativo = false order by p.id desc");
        return q.getResultList();
    }

    
}
