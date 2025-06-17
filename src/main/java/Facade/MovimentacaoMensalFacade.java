/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Pessoa;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author felip
 */
@Stateless
public class MovimentacaoMensalFacade extends AbstractFacade<MovimentacaoMensalFuncionario> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MovimentacaoMensalFacade() {
        super(MovimentacaoMensalFuncionario.class);
    }

    @Override
    public void remover(MovimentacaoMensalFuncionario entity) {
        entity = em.find(MovimentacaoMensalFuncionario.class, entity.getId());
        em.remove(entity);
    }

    @Override
    public void salvar(MovimentacaoMensalFuncionario entity) {
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            em.merge(entity);
        }
    }

    public List<MovimentacaoMensalFuncionario> buscarPorFuncionarioECompetencia(Pessoa funcionario, Date competencia) {
        String competenciaStr = new java.text.SimpleDateFormat("MM/yyyy").format(competencia);

        return em.createQuery("SELECT m FROM MovimentacaoMensalFuncionario m "
                + "WHERE m.funcionario = :funcionario "
                + "AND FUNCTION('TO_CHAR', m.data, 'MM/yyyy') = :competencia", MovimentacaoMensalFuncionario.class)
                .setParameter("funcionario", funcionario)
                .setParameter("competencia", competenciaStr)
                .getResultList();
    }

    public MovimentacaoMensalFuncionario findWithVendaAndItens(Long id) {
        return em.createQuery(
                "SELECT m FROM MovimentacaoMensalFuncionario m "
                + "JOIN FETCH m.venda v "
                + "LEFT JOIN FETCH v.itensVenda "
                + "WHERE m.id = :id", MovimentacaoMensalFuncionario.class)
                .setParameter("id", id)
                .getSingleResult();
    }

}
