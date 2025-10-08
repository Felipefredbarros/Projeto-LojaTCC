/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.Conta;
import Entidades.ContaReceber;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoBonus;
import Entidades.Enums.TipoLancamento;
import Entidades.ItensVenda;
import Entidades.LancamentoFinanceiro;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

    @EJB
    private ContaFacade contaFacade;
    @EJB
    private LancamentoFinanceiroFacade lancFacade;

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
        // --- validação da combinação plano x método ---

        venda.setStatus("FECHADA");

        List<ContaReceber> contas = new ArrayList<>();

        switch (venda.getPlanoPagamento()) {
            case A_VISTA:
                ContaReceber contaAvista = new ContaReceber();
                contaAvista.setVenda(venda);
                contaAvista.setCliente(venda.getCliente());
                contaAvista.setDescricao("Venda à vista - ID:" + venda.getId());
                contaAvista.setValor(venda.getValorTotal());
                contaAvista.setDataVencimento(venda.getDataVenda());
                contaAvista.setDataRecebimento(venda.getDataVenda());
                contaAvista.setStatus("RECEBIDA");
                contaAvista.setMetodoPagamento(venda.getMetodoPagamento());
                contas.add(contaAvista);
                break;

            case FIADO:
                ContaReceber contaPrazo = new ContaReceber();
                contaPrazo.setVenda(venda);
                contaPrazo.setCliente(venda.getCliente());
                contaPrazo.setDescricao("Venda a prazo - ID:" + venda.getId());
                contaPrazo.setValor(venda.getValorTotal());
                contaPrazo.setDataVencimento(addMonths(venda.getDataVenda(), 1));
                contaPrazo.setStatus("ABERTA");
                contaPrazo.setMetodoPagamento(venda.getMetodoPagamento());
                contas.add(contaPrazo);
                break;

            case PARCELADO_EM_1X:
                ContaReceber conta1x = new ContaReceber();
                conta1x.setVenda(venda);
                conta1x.setCliente(venda.getCliente());
                conta1x.setDescricao("Venda parcelada em 1x - ID:" + venda.getId());
                conta1x.setValor(venda.getValorTotal());
                conta1x.setDataVencimento(addMonths(venda.getDataVenda(), 1));
                conta1x.setStatus("ABERTA");
                conta1x.setMetodoPagamento(venda.getMetodoPagamento());
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
                    conta.setMetodoPagamento(venda.getMetodoPagamento());
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
                    conta.setMetodoPagamento(venda.getMetodoPagamento());
                    contas.add(conta);
                }
                break;
        }

        venda.setContasReceber(contas);

        // comissão / movimentação
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

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void cancelarVenda(Venda venda) {
        venda = em.find(Venda.class, venda.getId());
        venda.getItensVenda().size();

        if (venda.getMovimentacao() != null) {
            MovimentacaoMensalFuncionario mov = venda.getMovimentacao();
            venda.setMovimentacao(null);
            mov.setVenda(null);
            em.merge(venda);
            movimentacaoMensalFacade.remover(mov);
        }

        for (ItensVenda iv : venda.getItensVenda()) {
            ProdutoDerivacao derivacao = iv.getProdutoDerivacao();
            derivacao.setQuantidade(derivacao.getQuantidade() + iv.getQuantidade());
            em.merge(derivacao);
        }

        venda.setStatus("CANCELADA");

        for (ContaReceber cr : venda.getContasReceber()) {
            if ("RECEBIDA".equals(cr.getStatus())) {
                estornarContaReceber(cr, "Cancelamento da venda #" + venda.getId());
            } else {
                cr.setStatus("CANCELADA");
                contaReceberFacade.salvar(cr);
            }

        }
        em.merge(venda);
    }

    private void estornarContaReceber(ContaReceber cr, String motivo) {
        LancamentoFinanceiro original = lancFacade.buscarOriginalRecebimento(cr);

        if (original == null) {
            cr.setStatus("ESTORNADA");
            contaReceberFacade.salvar(cr);
            return;
        }

        original.setStatus(StatusLancamento.ESTORNADO);
        lancFacade.salvar(original);

        LancamentoFinanceiro reverso = new LancamentoFinanceiro();
        reverso.setConta(original.getConta());
        reverso.setTipo(TipoLancamento.SAIDA);
        reverso.setValor(original.getValor());
        reverso.setDataHora(new Date());
        reverso.setMetodo(original.getMetodo());
        reverso.setContaReceber(cr);
        reverso.setStatus(StatusLancamento.NORMAL);

        String desc = "Estorno de recebimento da Conta a Receber #" + cr.getId()
                + " (Venda #" + cr.getVenda().getId() + ")";

        if (cr.getDescricao() != null && !cr.getDescricao().trim().isEmpty()) {
            desc += " - " + cr.getDescricao().trim();
        }
        if (motivo != null && !motivo.trim().isEmpty()) {
            desc += " - Motivo: " + motivo.trim();
        }

        reverso.setDescricao(desc);

        lancFacade.salvar(reverso);

        cr.setStatus("ESTORNADA");
        contaReceberFacade.salvar(cr);

        recomputarSaldo(original.getConta());
    }

    private void recomputarSaldo(Conta conta) {
        Double inicial = conta.getValorInicial() != null ? conta.getValorInicial() : 0.0;
        Double entradas = lancFacade.somarPorContaETipoEPeriodo(conta, TipoLancamento.ENTRADA, null, null);
        Double saidas = lancFacade.somarPorContaETipoEPeriodo(conta, TipoLancamento.SAIDA, null, null);
        if (entradas == null) {
            entradas = 0d;
        }
        if (saidas == null) {
            saidas = 0d;
        }
        conta.setSaldo(inicial + entradas - saidas);
        contaFacade.salvar(conta);
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

    public List<Venda> listaVendasCanceladas() {
        Query q = getEntityManager().createQuery("From Venda as v where v.status = 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public VendaFacade() {
        super(Venda.class);
    }

}
