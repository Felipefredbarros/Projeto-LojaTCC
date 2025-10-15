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
import Entidades.ParcelaCompra;
import Entidades.Pessoa;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import Utilitario.FinanceDesc;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
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

        venda.setStatus("FECHADA");

        List<ContaReceber> contas = new ArrayList<>();

        switch (venda.getPlanoPagamento()) {
            case A_VISTA:
                ContaReceber contaAvista = new ContaReceber();
                contaAvista.setVenda(venda);
                contaAvista.setCliente(venda.getCliente());
                contaAvista.setDescricao("Venda à vista - ID:" + venda.getId());
                contaAvista.setValor(venda.getValorTotal());
                contaAvista.setDataVencimento(new Date());
                contaAvista.setDataRecebimento(new Date());
                contaAvista.setDataCriação(new Date());
                contaAvista.setStatus("RECEBIDA");
                contaAvista.setMetodoPagamento(venda.getMetodoPagamento());
                contas.add(contaAvista);
                break;

            case FIADO:
                for (int i = 0; i < venda.getParcelasVenda().size(); i++) {
                    ParcelaCompra parcela = venda.getParcelasVenda().get(i);

                    ContaReceber conta = new ContaReceber();
                    conta.setVenda(venda);
                    conta.setCliente(venda.getCliente());
                    conta.setDescricao("Parcela " + (i + 1) + "/" + venda.getParcelasVenda().size()
                            + " - ID:" + venda.getId());
                    conta.setValor(parcela.getValorParcela());
                    conta.setDataVencimento(parcela.getDataVencimento());
                    conta.setDataCriação(new Date());
                    conta.setMetodoPagamento(venda.getMetodoPagamento());
                    conta.setStatus("ABERTA");

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
            }
            if ("ABERTA".equals(cr.getStatus())) {
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

        String desc = FinanceDesc.estornoRecebimentoCR(cr, motivo);

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
        return em.createQuery("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itensVenda where v.status != 'CANCELADA' and v.status != 'Aberta' order by v.id desc", Venda.class).getResultList();
    }

    public List<Venda> listaTodasReais() {
        Query q = getEntityManager().createQuery("From Venda as v where v.status != 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    public List<Venda> listaVendasCanceladas() {
        Query q = getEntityManager().createQuery("From Venda as v where v.status = 'CANCELADA' order by v.id desc ");
        return q.getResultList();
    }

    // VendaFacade.java (Java 8 ok)
    public double findValorTotalVendasPorData(Date dataAlvo) {
        if (dataAlvo == null) {
            return 0.0;
        }

        LocalDate ld = dataAlvo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date inicio = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date fim = Date.from(ld.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // TROQUE v.valorTotal pelo nome REAL do campo na sua entidade Venda
        String jpql
                = "SELECT COALESCE(SUM(v.valorTotal), 0) "
                + "FROM Venda v "
                + "WHERE v.status IN ('FECHADA')"
                + "  AND v.dataVenda >= :inicio AND v.dataVenda < :fim";

        Double total = em.createQuery(jpql, Double.class)
                .setParameter("inicio", inicio, TemporalType.TIMESTAMP)
                .setParameter("fim", fim, TemporalType.TIMESTAMP)
                .getSingleResult();

        return total != null ? total : 0.0;
    }

    public Map<Object, Number> findVendasUltimos30Dias() {
        Map<Object, Number> resultado = new LinkedHashMap<>();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioLD = hoje.minusDays(29);

        Date inicio = Date.from(inicioLD.atStartOfDay(zone).toInstant());
        Date fim = Date.from(hoje.plusDays(1).atStartOfDay(zone).toInstant());

        // PREENCHE 30 dias com zero (mantém o gráfico contínuo)
        for (int i = 0; i < 30; i++) {
            LocalDate d = inicioLD.plusDays(i);
            Date chave = Date.from(d.atStartOfDay(zone).toInstant());
            resultado.put(chave, 0.0);
        }

        String jpql
                = "SELECT YEAR(v.dataVenda), MONTH(v.dataVenda), DAY(v.dataVenda), "
                + "       COALESCE(SUM(v.valorTotal), 0) "
                + // <-- troque 'valorTotal' se o nome for outro
                "FROM Venda v "
                + "WHERE v.status <> 'CANCELADA' "
                + // se enum: use parâmetro
                "  AND v.dataVenda >= :inicio AND v.dataVenda < :fim "
                + "GROUP BY YEAR(v.dataVenda), MONTH(v.dataVenda), DAY(v.dataVenda) "
                + "ORDER BY YEAR(v.dataVenda), MONTH(v.dataVenda), DAY(v.dataVenda)";

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

    public Map<String, Number> findTop5ProdutosVendidosMes() {
        Map<String, Number> resultado = new LinkedHashMap<>();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate inicioLD = LocalDate.now().withDayOfMonth(1);
        LocalDate fimLD = inicioLD.plusMonths(1);

        Date inicio = Date.from(inicioLD.atStartOfDay(zone).toInstant());
        Date fim = Date.from(fimLD.atStartOfDay(zone).toInstant());

        // 1) Buscamos pelo ID da derivação (campo mapeado certo) + soma das quantidades
        String jpql
                = "SELECT pd.id, SUM(iv.quantidade) "
                + "FROM ItensVenda iv "
                + "JOIN iv.venda v "
                + "JOIN iv.produtoDerivacao pd "
                + "WHERE v.status <> 'CANCELADA' "
                + "  AND v.dataVenda >= :inicio AND v.dataVenda < :fim "
                + "GROUP BY pd.id "
                + "ORDER BY SUM(iv.quantidade) DESC";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(jpql)
                .setParameter("inicio", inicio, TemporalType.TIMESTAMP)
                .setParameter("fim", fim, TemporalType.TIMESTAMP)
                .setMaxResults(5)
                .getResultList();

        // 2) Para cada ID, carregamos a derivação e usamos getTexto() como rótulo do gráfico
        for (Object[] r : rows) {
            Long derivacaoId = (Long) r[0];
            Number qtd = (Number) r[1];

            ProdutoDerivacao pd = em.find(ProdutoDerivacao.class, derivacaoId);
            String rotulo = (pd != null && pd.getTexto() != null) ? pd.getTexto() : ("Derivação #" + derivacaoId);

            resultado.put(rotulo, qtd);
        }

        return resultado;
    }

    public List<Venda> buscarPorFiltros(Pessoa cliente, Pessoa funcionario, Produto produto, Date ini, Date fim) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c FROM Venda c "
                + "LEFT JOIN FETCH c.itensVenda ic "
                + "LEFT JOIN ic.produtoDerivacao pd "
                + "LEFT JOIN pd.produto p "
                + "WHERE c.status <> 'CANCELADA' AND c.status <> 'Aberta' ");

        if (cliente != null) {
            jpql.append(" AND c.cliente = :cliente ");
        }

        if (funcionario != null) {
            jpql.append(" AND c.funcionario = :funcionario ");
        }

        if (produto != null) {
            jpql.append(
                    " AND EXISTS ( "
                    + "   SELECT 1 FROM ItensVenda ic2 "
                    + "   JOIN ic2.produtoDerivacao pd2 "
                    + "   JOIN pd2.produto p2 "
                    + "   WHERE ic2.venda = c AND p2 = :produto "
                    + " ) "
            );
        }
        if (ini != null) {
            jpql.append(" AND c.dataVenda >= :ini ");
        }
        if (fim != null) {
            jpql.append(" AND c.dataVenda <= :fim ");
        }
        jpql.append(" ORDER BY c.id DESC ");

        TypedQuery<Venda> q = em.createQuery(jpql.toString(), Venda.class);

        if (cliente != null) {
            q.setParameter("cliente", cliente);
        }
        if (funcionario != null) {
            q.setParameter("funcionario", funcionario);
        }
        if (produto != null) {
            q.setParameter("produto", produto);
        }
        if (ini != null) {
            q.setParameter("ini", ini, TemporalType.TIMESTAMP);
        }
        if (fim != null) {
            q.setParameter("fim", fim, TemporalType.TIMESTAMP);
        }

        return q.getResultList();
    }

    public VendaFacade() {
        super(Venda.class);
    }

}
