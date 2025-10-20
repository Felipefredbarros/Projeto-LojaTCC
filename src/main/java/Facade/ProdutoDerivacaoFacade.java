/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.ProdutoDerivacao;
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
public class ProdutoDerivacaoFacade extends AbstractFacade<ProdutoDerivacao>{

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProdutoDerivacaoFacade() {
        super(ProdutoDerivacao.class);
    }
    
    public List<ProdutoDerivacao> listarTodasOrdenadasComProduto() {
        TypedQuery<ProdutoDerivacao> query = em.createQuery(
            "SELECT pd FROM ProdutoDerivacao pd JOIN FETCH pd.produto p ORDER BY p.categoria.categoria, pd.tamanho, pd.cor",
            ProdutoDerivacao.class);
        return query.getResultList();
    }
    

}
