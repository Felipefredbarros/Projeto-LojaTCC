/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Cidade;
import java.util.Collections;
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
public class CidadeFacade extends AbstractFacade<Cidade>{

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CidadeFacade() {
        super(Cidade.class);
    }
    
    public List<Cidade> buscarPorEstadoId(Long estadoId) {
        if (estadoId == null) {
            return Collections.emptyList();
        }

        String jpql = "SELECT c FROM Cidade c WHERE c.estado.id = :idDoEstado ORDER BY c.nome ASC";
        TypedQuery<Cidade> query = getEntityManager().createQuery(jpql, Cidade.class);
        query.setParameter("idDoEstado", estadoId);
        return query.getResultList();
    }

}
