package Controladores;

import Entidades.ContaPagar;
import Entidades.ProdutoDerivacao;
import Facade.CompraFacade;
import Facade.ContaPagarFacade;
import Facade.ContaReceberFacade;
import Facade.PessoaFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author felip
 */
@ManagedBean
@ViewScoped
public class dashBoardControle implements Serializable {

    @EJB
    private PessoaFacade pessoaFacade;
    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private CompraFacade compraFacade;
    @EJB
    private VendaFacade vendaFacade;
    @EJB
    private ContaPagarFacade contaPagarFacade;
    @EJB
    private ContaReceberFacade contaReceberFacade;

    private DashboardModel dashboardModel;
    private LineChartModel vendasMensalModel;
    private PieChartModel topProdutosModel;
    private LineChartModel comprasMensalModel; // 👈 novo

    // --- Dados para os KPIs ---
    private Double valorVendasHoje;
    private Double contasAReceberMes;
    private Double contasAPagarMes;
    private Long novosClientesMes;

    // --- Listas para as Tabelas ---
    private List<ProdutoDerivacao> produtosEstoqueBaixo;
    private List<ContaPagar> contasPagarProximas;

    @PostConstruct
    public void init() {
        carregarKPIs();
        carregarTabelas();
        createVendasMensalModel();
        createComprasMensalModel(); // 👈 novo
        createTopProdutosModel();
    }

    private void carregarKPIs() {
        valorVendasHoje = vendaFacade.findValorTotalVendasPorData(new Date());
        if (valorVendasHoje == null) {
            valorVendasHoje = 0.0;
        }

        contasAReceberMes = contaReceberFacade.findTotalAbertoPorMes(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        if (contasAReceberMes == null) {
            contasAReceberMes = 0.0;
        }

        contasAPagarMes = contaPagarFacade.findTotalAbertoPorMes(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        if (contasAPagarMes == null) {
            contasAPagarMes = 0.0;
        }
    }

    private void carregarTabelas() {
        produtosEstoqueBaixo = produtoFacade.listarProdutosEstoqueBaixo();

        LocalDate hoje = LocalDate.now();
        Date dataInicial = Date.from(hoje.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dataFinal = Date.from(hoje.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        contasPagarProximas = contaPagarFacade.findContasEntreDatas(dataInicial, dataFinal);
    }

    private LineChartModel montarLinhaUltimos30Dias(Map<Object, Number> dados) {
        LineChartModel model = new LineChartModel();

        LineChartSeries series = new LineChartSeries();
        series.setLabel("");
        series.setFill(false);

        LocalDate hoje = LocalDate.now();
        LocalDate inicioLD = hoje.minusDays(29);
        int paddingDias = 5; // “sobra” à direita no eixo
        LocalDate fimEixo = hoje.plusDays(paddingDias);

        // normaliza para yyyy-MM-dd -> valor
        Map<String, Number> porDia = new java.util.HashMap<>();
        if (dados != null) {
            for (Map.Entry<Object, Number> e : dados.entrySet()) {
                Object k = e.getKey();
                String dia;
                if (k instanceof LocalDate) {
                    dia = ((LocalDate) k).toString();
                } else if (k instanceof Date) {
                    dia = ((Date) k).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                } else {
                    dia = String.valueOf(k); // assume yyyy-MM-dd
                }
                porDia.put(dia, e.getValue() == null ? 0 : e.getValue());
            }
        }

        // preencher todos os dias com 0 quando necessário
        for (LocalDate d = inicioLD; !d.isAfter(hoje); d = d.plusDays(1)) {
            String chave = d.toString();
            series.set(chave, porDia.getOrDefault(chave, 0));
        }
        // (opcional) zeros nos dias “pra frente” só para estender o eixo
        for (LocalDate d = hoje.plusDays(1); !d.isAfter(fimEixo); d = d.plusDays(1)) {
            series.set(d.toString(), 0);
        }

        if (series.getData().isEmpty()) {
            series.set(hoje.toString(), 0);
        }

        model.addSeries(series);

        Axis y = model.getAxis(AxisType.Y);
        y.setLabel("Valor (R$)");
        y.setMin(0);

        DateAxis x = new DateAxis("Data");
        x.setTickAngle(-35);
        x.setTickFormat("%d/%m");
        x.setMin(inicioLD.toString());
        x.setMax(fimEixo.toString());
        x.setTickCount(8);
        model.getAxes().put(AxisType.X, x);

        model.setAnimate(true);
        return model;
    }

    public void createVendasMensalModel() {
        vendasMensalModel = montarLinhaUltimos30Dias(
                vendaFacade.findVendasUltimos30Dias()
        );
    }

    public void createComprasMensalModel() {
        comprasMensalModel = montarLinhaUltimos30Dias(
                compraFacade.findComprasUltimos30Dias()
        );
    }

    public void createTopProdutosModel() {
        topProdutosModel = new PieChartModel();

        Map<String, Number> dadosProdutos = vendaFacade.findTop5ProdutosVendidosMes();

        if (dadosProdutos != null && !dadosProdutos.isEmpty()) {
            dadosProdutos.forEach(topProdutosModel::set);
        } else {
            topProdutosModel.set("Nenhuma venda no mês", 1);
        }

        topProdutosModel.setLegendPosition("e");
        topProdutosModel.setFill(true);
        topProdutosModel.setShowDataLabels(true);
        topProdutosModel.setDiameter(180);
        topProdutosModel.setShadow(false);
        topProdutosModel.setTitle("Top 5 Produtos");
    }

    public List<ProdutoDerivacao> getProdutosEstoqueBaixoTable() {
        // Já existe listarProdutosEstoqueBaixo() no ProdutoFacade
        return produtoFacade.listarProdutosEstoqueBaixo();
    }

    public Long getTotalProdutos() {
        return produtoFacade.totalProdutosCadastradosAtivos();
    }

    public List<String> getProdutosEstoqueBaixoDescricoes() {
        List<ProdutoDerivacao> produtosBaixoEstoque = produtoFacade.listarProdutosEstoqueBaixo();
        List<String> descricoes = new ArrayList<>();
        for (ProdutoDerivacao derivacao : produtosBaixoEstoque) {
            descricoes.add(derivacao.getTexto()); // ou monte: derivacao.getProduto().getNome() + " - " + derivacao.getTamanho() + "/" + derivacao.getCor()
        }
        return descricoes;
    }

    public Double getTotalEstoque() {
        return produtoFacade.totalEstoqueGeral();
    }

    public Long getTotalVendas() {
        return vendaFacade.totalVendasCadastradas();
    }

    public Double getValorTotalVendido() {
        return vendaFacade.valorVendaVendido();
    }

    public Long getTotalCompras() {
        return compraFacade.totalComprasCadastradas();
    }

    public Double getValorTotalComprado() {
        return compraFacade.valorComprasVendido();
    }

    public Double getLucroLiquido() {
        return vendaFacade.valorVendaVendido() - compraFacade.valorComprasVendido();
    }

    public LineChartModel getVendasMensalModel() {
        return vendasMensalModel;
    }

    public PieChartModel getTopProdutosModel() {
        return topProdutosModel;
    } // CORRIGIDO: Retorna PieChartModel

    public Double getValorVendasHoje() {
        return valorVendasHoje;
    }

    public Double getContasAReceberMes() {
        return contasAReceberMes;
    }

    public Double getContasAPagarMes() {
        return contasAPagarMes;
    }

    public Long getNovosClientesMes() {
        return novosClientesMes;
    }

    public List<ContaPagar> getContasPagarProximas() {
        return contasPagarProximas;
    }

    public List<ProdutoDerivacao> getProdutosEstoqueBaixo() {
        return produtosEstoqueBaixo;
    }

    public LineChartModel getComprasMensalModel() {
        return comprasMensalModel;
    } // 👈 novo

}
