/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Marca;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author felip
 */
@Stateless
public class MarcaFacade extends AbstractFacade<Marca>{

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MarcaFacade() {
        super(Marca.class);
    }
    
    public boolean categoriaTemProduto(Long marcaId) {
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM Produto p WHERE p.marca.id = :marcaId", Long.class)
                .setParameter("marcaId", marcaId)
                .getSingleResult();

        return count != null && count > 0;
    }

}
