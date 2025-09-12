/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Facade;

import Entidades.ContaReceber;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

}
