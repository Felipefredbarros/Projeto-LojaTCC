/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.ContaPagar;
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

}
