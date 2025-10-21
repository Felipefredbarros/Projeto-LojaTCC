/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.Categoria;
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
public class CategoriaFacade extends AbstractFacade<Categoria> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CategoriaFacade() {
        super(Categoria.class);
    }

    public boolean categoriaTemProduto(Long categoriaId) {
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM Produto p WHERE p.categoria.id = :categoriaId", Long.class)
                .setParameter("categoriaId", categoriaId)
                .getSingleResult();

        return count != null && count > 0;
    }
    
    public List<Categoria> listaCategoriaAtiva() {
        Query q = getEntityManager().createQuery("from Categoria as p where p.ativo = true order by p.id desc");
        return q.getResultList();
    }
    
    public List<Categoria> listaCategoriaInativa() {
        Query q = getEntityManager().createQuery("from Categoria as p where p.ativo = false order by p.id desc");
        return q.getResultList();
    }
}
