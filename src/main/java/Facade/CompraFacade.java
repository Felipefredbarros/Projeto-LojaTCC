/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facade;

import Entidades.Compra;
import Entidades.Conta;
import Entidades.ContaPagar;
import Entidades.Enums.StatusLancamento;
import Entidades.Enums.TipoLancamento;
import Entidades.ItensCompra;
import Entidades.LancamentoFinanceiro;
import Entidades.ParcelaCompra;
import Entidades.Pessoa;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Utilitario.FinanceDesc;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

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
    @EJB
    private ContaFacade contaFacade;
    @EJB
    private LancamentoFinanceiroFacade lancFacade;

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
            conta.setDataCriação(new Date());
            conta.setMetodoPagamento(compra.getMetodoPagamento());
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

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void cancelarCompra(Compra compraParam) {

        Compra compra = em.createQuery(
                "select c from Compra c "
                + "left join fetch c.itensCompra i "
                + "where c.id = :id", Compra.class)
                .setParameter("id", compraParam.getId())
                .getSingleResult();

        for (ItensCompra ic : compra.getItensCompra()) {
            ProdutoDerivacao deriv = ic.getProdutoDerivacao();
            deriv.setQuantidade(deriv.getQuantidade() - ic.getQuantidade());
            em.merge(deriv);
        }

        List<ContaPagar> contas = em.createQuery(
                "select distinct cp from ContaPagar cp "
                + "left join fetch cp.lancamentos l "
                + "left join fetch l.conta "
                + "where cp.compra = :compra", ContaPagar.class)
                .setParameter("compra", compra)
                .getResultList();

        for (ContaPagar cp : contas) {
            String st = cp.getStatus();
            if ("PAGA".equalsIgnoreCase(st)) {
                estornarContaPagar(cp, "Cancelamento da compra #" + compra.getId());
            }
            if ("ABERTA".equalsIgnoreCase(st)) {
                cp.setStatus("CANCELADA");
                contaPagarFacade.salvar(cp);
            }
        }

        compra.setStatus("CANCELADA");
        em.merge(compra);
    }

    private void estornarContaPagar(ContaPagar cp, String motivo) {
        LancamentoFinanceiro original = lancFacade.buscarOriginalPagamento(cp);

        if (original == null) {
            cp.setStatus("ESTORNADA");
            contaPagarFacade.salvar(cp);
            return;
        }

        original.setStatus(StatusLancamento.ESTORNADO);
        lancFacade.salvar(original);

        LancamentoFinanceiro reverso = new LancamentoFinanceiro();
        reverso.setConta(original.getConta());
        reverso.setTipo(TipoLancamento.ENTRADA);
        reverso.setValor(original.getValor());
        reverso.setDataHora(new Date());
        reverso.setMetodo(original.getMetodo());
        reverso.setContaPagar(cp);
        reverso.setStatus(StatusLancamento.NORMAL);

        reverso.setDescricao(FinanceDesc.estornoPagamentoCP(cp, motivo));

        lancFacade.salvar(reverso);

        cp.setStatus("ESTORNADA");
        contaPagarFacade.salvar(cp);

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

    public Compra findWithItens(Long id) {
        return em.createQuery("SELECT c FROM Compra c LEFT JOIN FETCH c.itensCompra WHERE c.id = :id", Compra.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Compra> listaTodosComItens() {
        return em.createQuery("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.itensCompra where c.status != 'CANCELADA' and c.status != 'Aberta' order by c.id desc", Compra.class).getResultList();
    }

    public List<Compra> listaTodasReais() {
        Query q = getEntityManager().createQuery("From Compra as v where v.status != 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public List<Compra> listaComprasCanceladas() {
        Query q = getEntityManager().createQuery("From Compra as v where v.status = 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public Map<Object, Number> findComprasUltimos30Dias() {
        Map<Object, Number> resultado = new LinkedHashMap<>();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioLD = hoje.minusDays(29);

        Date inicio = Date.from(inicioLD.atStartOfDay(zone).toInstant());
        Date fim = Date.from(hoje.plusDays(1).atStartOfDay(zone).toInstant());

        // preenche zeros
        for (int i = 0; i < 30; i++) {
            LocalDate d = inicioLD.plusDays(i);
            resultado.put(Date.from(d.atStartOfDay(zone).toInstant()), 0.0);
        }

        String jpql
                = "SELECT YEAR(c.dataCompra), MONTH(c.dataCompra), DAY(c.dataCompra), "
                + "       COALESCE(SUM(c.valorTotal), 0) "
                + "FROM Compra c "
                + "WHERE c.status = 'FECHADA' "
                + // ajuste se for outro valor ou enum
                "AND c.dataCompra >= :inicio AND c.dataCompra < :fim "
                + "GROUP BY YEAR(c.dataCompra), MONTH(c.dataCompra), DAY(c.dataCompra) "
                + "ORDER BY YEAR(c.dataCompra), MONTH(c.dataCompra), DAY(c.dataCompra)";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(jpql)
                .setParameter("inicio", inicio, TemporalType.TIMESTAMP)
                .setParameter("fim", fim, TemporalType.TIMESTAMP)
                .getResultList();

        for (Object[] r : rows) {
            int ano = ((Number) r[0]).intValue();
            int mes = ((Number) r[1]).intValue();
            int dia = ((Number) r[2]).intValue();
            double total = ((Number) r[3]).doubleValue();

            LocalDate d = LocalDate.of(ano, mes, dia);
            Date chave = Date.from(d.atStartOfDay(zone).toInstant());
            resultado.put(chave, total);
        }
        return resultado;
    }

    public List<Compra> buscarPorFiltros(Pessoa fornecedor, Produto produto, Date ini, Date fim) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c FROM Compra c "
                + "LEFT JOIN FETCH c.itensCompra ic "
                + "LEFT JOIN FETCH ic.produtoDerivacao pd "
                + "LEFT JOIN FETCH pd.produto pFetch "
                + "WHERE c.status <> 'CANCELADA' AND c.status <> 'Aberta' "
        );

        if (fornecedor != null) {
            jpql.append(" AND c.fornecedor = :fornecedor ");
        }
        if (ini != null) {
            jpql.append(" AND c.dataCompra >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND c.dataCompra <= :fim ");
        }
        if (produto != null) {
            jpql.append(
                    " AND EXISTS ( "
                    + "   SELECT 1 FROM ItensCompra ic2 "
                    + "   JOIN ic2.produtoDerivacao pd2 "
                    + "   JOIN pd2.produto p2 "
                    + "   WHERE ic2.compra = c AND p2 = :produto "
                    + " ) "
            );
        }

        jpql.append(" ORDER BY c.id DESC ");

        TypedQuery<Compra> q = em.createQuery(jpql.toString(), Compra.class);

        if (fornecedor != null) {
            q.setParameter("fornecedor", fornecedor);
        }
        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fim", fim, TemporalType.TIMESTAMP);
        }
        if (produto != null) {
            q.setParameter("produto", produto);
        }

        return q.getResultList();
    }

    public CompraFacade() {
        super(Compra.class);
    }

}
