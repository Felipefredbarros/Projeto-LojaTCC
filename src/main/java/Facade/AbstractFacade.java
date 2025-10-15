/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.Root; // Importe esta classe
import javax.persistence.criteria.CriteriaQuery;


/**
 *
 * @author felip
 */
public abstract class AbstractFacade<T> {

    
    private Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void salvar(T entity) {
        getEntityManager().merge(entity);
    }

    public void remover(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T buscar(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> listaTodos() {
        Query q = getEntityManager().createQuery("from "
                + entityClass.getSimpleName() + " order by id desc");
        return q.getResultList();
    }
    

    public List<T> listaFiltrando(String filtro, String... atributos) {
        String hql = "from " + entityClass.getSimpleName() + " obj where ";
        for (String atributo : atributos) {
            hql += "lower(obj." + atributo + ") like :filtro OR ";
        }
        hql = hql.substring(0, hql.length() - 3);
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("filtro", "%" + filtro.toLowerCase() + "%");
        return q.getResultList();
    }
    
     public int count() {
        // 1. Cria uma query de critérios (CriteriaQuery)
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        
        Root<T> rt = cq.from(entityClass);
        
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        
        return ((Long) q.getSingleResult()).intValue();
    }
}
