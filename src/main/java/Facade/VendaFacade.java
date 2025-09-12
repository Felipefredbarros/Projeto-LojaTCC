/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.ContaReceber;
import Entidades.Enums.MetodoPagamento;
import Entidades.Enums.TipoBonus;
import Entidades.ItensVenda;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author felip
 */
@Stateless
public class VendaFacade extends AbstractFacade<Venda> {

    @EJB
    private MovimentacaoMensalFacade movimentacaoMensalFacade;

    @EJB
    private ContaReceberFacade contaReceberFacade;

    @PersistenceContext(unitName = "projetotestPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Long totalVendasCadastradas() {
        String jpql = "SELECT COUNT(v) FROM Venda v";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public Double valorVendaVendido() {
        String jpql = "SELECT SUM(v.valorTotal) FROM Venda v";
        Double result = em.createQuery(jpql, Double.class).getSingleResult();
        return result != null ? result : 0.0;  // Se o resultado for null, retorna 0.0
    }

    public void salvarVenda(Venda entity, Boolean edit) {
        if (!edit) {
            for (ItensVenda iv : entity.getItensVenda()) {
                ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
                derivacao.setQuantidade(derivacao.getQuantidade() - iv.getQuantidade());
                em.merge(derivacao);
                iv.setDesc(derivacao.getProduto().getTexto());
            }

            if (entity.getId() == null) {
                em.persist(entity); // novo
            } else {
                em.merge(entity);   // já existe (deve ser atualizado)
            }
        } else {
            em.merge(entity); // edição
        }
    }

    public void fecharVenda(Venda venda) {
        venda.setStatus("FECHADA");

        List<ContaReceber> contas = new ArrayList<>();

        // Caso seja pagamento à vista
        if (MetodoPagamento.getMetodosPagamentoAVista().contains(venda.getMetodoPagamento())) {
            ContaReceber contaAvista = new ContaReceber();
            contaAvista.setVenda(venda);
            contaAvista.setCliente(venda.getCliente());
            contaAvista.setDescricao("Venda à vista - ID:" + venda.getId());
            contaAvista.setValor(venda.getValorTotal());
            contaAvista.setDataVencimento(venda.getDataVenda()); // vence no ato
            contaAvista.setDataRecebimento(venda.getDataVenda()); // recebido no ato
            contaAvista.setStatus("RECEBIDA"); // já pago
            contas.add(contaAvista);

            // --- Lançamento financeiro automático (à vista) ---
            /*
        LancamentoFinanceiro lanc = new LancamentoFinanceiro();
        lanc.setTipo("ENTRADA");
        lanc.setDescricao("Recebimento à vista da venda ID:" + venda.getId());
        lanc.setValor(venda.getValorTotal());
        lanc.setData(new Date());
        lanc.setContaReceber(contaAvista);

        lancamentoFinanceiroFacade.salvar(lanc);
        contaBancariaFacade.atualizarSaldo(lanc.getValor());
             */
        } else {
            // Parcelado
            switch (venda.getPlanoPagamento()) {
                case PARCELADO_EM_1X:
                    ContaReceber conta1x = new ContaReceber();
                    conta1x.setVenda(venda);
                    conta1x.setCliente(venda.getCliente());
                    conta1x.setDescricao("Venda parcelada em 1x - ID:" + venda.getId());
                    conta1x.setValor(venda.getValorTotal());
                    conta1x.setDataVencimento(addMonths(venda.getDataVenda(), 1)); // próximo mês
                    conta1x.setStatus("ABERTA");
                    contas.add(conta1x);
                    break;

                case PARCELADO_EM_2X:
                    for (int i = 0; i < 2; i++) {
                        ContaReceber conta = new ContaReceber();
                        conta.setVenda(venda);
                        conta.setCliente(venda.getCliente());
                        conta.setDescricao("Parcela " + (i + 1) + "/2 - ID:" + venda.getId());
                        conta.setValor(venda.getValorTotal() / 2);
                        conta.setDataVencimento(addMonths(venda.getDataVenda(), i + 1));
                        conta.setStatus("ABERTA");
                        contas.add(conta);
                    }
                    break;

                case PARCELADO_EM_3X:
                    for (int i = 0; i < 3; i++) {
                        ContaReceber conta = new ContaReceber();
                        conta.setVenda(venda);
                        conta.setCliente(venda.getCliente());
                        conta.setDescricao("Parcela " + (i + 1) + "/3 - ID:" + venda.getId());
                        conta.setValor(venda.getValorTotal() / 3);
                        conta.setDataVencimento(addMonths(venda.getDataVenda(), i + 1));
                        conta.setStatus("ABERTA");
                        contas.add(conta);
                    }
                    break;
            }
        }

        venda.setContasReceber(contas);

        // Movimentação de comissão do funcionário
        MovimentacaoMensalFuncionario mov = new MovimentacaoMensalFuncionario();
        mov.setFuncionario(venda.getFuncionario());
        mov.setData(venda.getDataVenda());
        mov.setTipoBonus(TipoBonus.COMISSAO);
        mov.setBonus(venda.getValorTotal());
        mov.setVenda(venda);

        movimentacaoMensalFacade.salvar(mov);
        venda.setMovimentacao(mov);

        em.merge(venda);
    }

    private Date addMonths(Date data, int meses) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        cal.add(Calendar.MONTH, meses);
        return cal.getTime();
    }

    public void cancelarVenda(Venda venda) {
        venda = em.find(Venda.class, venda.getId());
        venda.getItensVenda().size();

        // Remove movimentação de comissão
        if (venda.getMovimentacao() != null) {
            MovimentacaoMensalFuncionario mov = venda.getMovimentacao();
            venda.setMovimentacao(null);
            mov.setVenda(null);
            em.merge(venda);
            movimentacaoMensalFacade.remover(mov);
        }

        // Devolve estoque
        for (ItensVenda iv : venda.getItensVenda()) {
            ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
            derivacao.setQuantidade(derivacao.getQuantidade() + iv.getQuantidade());
            em.merge(derivacao);
        }

        venda.setStatus("CANCELADA");

        // Cancela ou estorna contas vinculadas
        for (ContaReceber conta : venda.getContasReceber()) {
            if ("RECEBIDA".equals(conta.getStatus())) {
                conta.setStatus("ESTORNADA");

                // --- Lançamento de estorno (saída) ---
                /*
            LancamentoFinanceiro estorno = new LancamentoFinanceiro();
            estorno.setData(new Date());
            estorno.setTipo("SAIDA");
            estorno.setValor(conta.getValor());
            estorno.setDescricao("Estorno de venda cancelada - ID Venda: " + venda.getId());

            lancamentoFinanceiroFacade.salvar(estorno);
            contaBancariaFacade.atualizarSaldo(estorno.getValor() * -1);
                 */
            } else {
                conta.setStatus("CANCELADA");
            }

            contaReceberFacade.salvar(conta);
        }

        em.merge(venda);
    }

    @Override
    public void remover(Venda entity) {
        entity = em.find(Venda.class, entity.getId());
        entity.getItensVenda().size();
        entity.getMovimentacao();

        for (ItensVenda iv : entity.getItensVenda()) {
            ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
            derivacao.setQuantidade(derivacao.getQuantidade() + iv.getQuantidade());
            em.merge(derivacao);
        }

        super.remover(entity);
    }

    public Venda findWithItens(Long id) {
        return em.createQuery("SELECT v FROM Venda v LEFT JOIN FETCH v.itensVenda WHERE v.id = :id", Venda.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Venda> listaTodosComItens() {
        return em.createQuery("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itensVenda", Venda.class).getResultList();
    }

    public List<Venda> listaTodasReais() {
        Query q = getEntityManager().createQuery("From Venda as v where v.status != 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public VendaFacade() {
        super(Venda.class);
    }

}
