/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.Compra;
import Entidades.ContaPagar;
import Entidades.ItensCompra;
import Entidades.ParcelaCompra;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import static javax.ws.rs.client.Entity.entity;

/**
 *
 * @author felip
 */
@Stateless
public class CompraFacade extends AbstractFacade<Compra> {

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @EJB
    private ContaPagarFacade contaPagarFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Long totalComprasCadastradas() {
        String jpql = "SELECT COUNT(c) FROM Compra c";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public Double valorComprasVendido() {
        String jpql = "SELECT SUM(c.valorTotal) FROM Compra c";
        Double result = em.createQuery(jpql, Double.class).getSingleResult();
        return result != null ? result : 0.0;
    }

    public void salvarCompra(Compra entity, Boolean edit) {
        //if (!edit) {
        // for (ItensCompra ic : entity.getItensCompra()) {
        //  ProdutoDerivacao derivacao = ic.getProdutoDerivacao();

        //  derivacao.setQuantidade(derivacao.getQuantidade() + ic.getQuantidade());
        //   Produto produto = derivacao.getProduto();
        //   produto.setValorUnitarioCompra(ic.getValorUnitario());
        //   produto.setValorUnitarioVenda((ic.getValorUnitario() * 0.8) + ic.getValorUnitario());
        //   getEntityManager().merge(derivacao);
        //   getEntityManager().merge(produto);
        //   ic.setDesc(derivacao.getTexto());
        // }
        // }
        getEntityManager().merge(entity);
    }

    @Override
    public void remover(Compra entity) {
        entity = getEntityManager().find(Compra.class, entity.getId());
        entity.getItensCompra().size();

        super.remover(entity);
    }

    public void fecharCompra(Compra compra) {
        compra.setStatus("FECHADA");

        List<ContaPagar> contas = new ArrayList<>();

        for (int i = 0; i < compra.getParcelasCompra().size(); i++) {
            ParcelaCompra parcela = compra.getParcelasCompra().get(i);

            ContaPagar conta = new ContaPagar();
            conta.setCompra(compra);
            conta.setFornecedor(compra.getFornecedor());
            conta.setDescricao("Parcela " + (i + 1) + "/" + compra.getParcelasCompra().size()
                    + " - ID:" + compra.getId());
            conta.setValor(parcela.getValorParcela());
            conta.setDataVencimento(parcela.getDataVencimento());
            conta.setStatus("ABERTA");

            contas.add(conta);
        }
        for (ItensCompra ic : compra.getItensCompra()) {
            ProdutoDerivacao derivacao = ic.getProdutoDerivacao();

            derivacao.setQuantidade(derivacao.getQuantidade() + ic.getQuantidade());

            Produto produto = derivacao.getProduto();
            produto.setValorUnitarioCompra(ic.getValorUnitario());
            produto.setValorUnitarioVenda((ic.getValorUnitario() * 0.8) + ic.getValorUnitario());

            getEntityManager().merge(derivacao);
            getEntityManager().merge(produto);

            ic.setDesc(derivacao.getTexto());
        }

        compra.setContasPagar(contas);

        em.merge(compra);
    }

    public void cancelarCompra(Compra compra) {
        compra = em.find(Compra.class, compra.getId());
        compra.getItensCompra().size();

        // Volta o estoque
        for (ItensCompra ic : compra.getItensCompra()) {
            ProdutoDerivacao derivacao = ic.getProdutoDerivacao();
            derivacao.setQuantidade(derivacao.getQuantidade() - ic.getQuantidade());
            em.merge(derivacao);
        }

        // Atualiza status da compra
        compra.setStatus("CANCELADA");

        // Atualiza contas a pagar
        for (ContaPagar conta : compra.getContasPagar()) {
            if ("PAGA".equals(conta.getStatus())) {
                conta.setStatus("ESTORNADA");
            } else {
                conta.setStatus("CANCELADA");
            }
            contaPagarFacade.salvar(conta);
        }

        em.merge(compra);
    }

    public Compra findWithItens(Long id) {
        return em.createQuery("SELECT c FROM Compra c LEFT JOIN FETCH c.itensCompra WHERE c.id = :id", Compra.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Compra> listaTodosComItens() {
        return em.createQuery("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.itensCompra", Compra.class).getResultList();
    }

    public List<Compra> listaTodasReais() {
        Query q = getEntityManager().createQuery("From Compra as v where v.status != 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public List<Compra> listaComprasCanceladas() {
        Query q = getEntityManager().createQuery("From Compra as v where v.status = 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public CompraFacade() {
        super(Compra.class);
    }

}
