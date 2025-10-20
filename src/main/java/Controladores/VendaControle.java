package Controladores;

import Converters.ConverterGenerico;
import Entidades.Conta;
import Entidades.ContaReceber;
import Entidades.ItensVenda;
import Entidades.Enums.MetodoPagamento;
import Entidades.Pessoa;
import Entidades.Enums.PlanoPagamento;
import Entidades.LancamentoFinanceiro;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Entidades.Venda;
import Facade.MovimentacaoMensalFacade;
import Facade.PessoaFacade;
import Facade.ProdutoDerivacaoFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import Utilitario.FinanceDesc;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named("vendaControle")
@ViewScoped
public class VendaControle implements Serializable {

    private Venda venda = new Venda();
    private ItensVenda itensVenda = new ItensVenda();
    private Date dataInicio;
    private Date dataFim;
    private List<Venda> listaVendasFiltradas = new ArrayList<>();
    private Boolean edit = false;
    private Pessoa clienteFiltro;
    private Produto produtoFiltro;
    private Pessoa funcionarioFiltro;

    @EJB
    private VendaFacade vendaFacade;

    @EJB
    private PessoaFacade pessoaFacade;

    @EJB
    private ProdutoFacade produtoFacade;

    @EJB
    private ProdutoDerivacaoFacade produtoDevFacade;

    @EJB
    private MovimentacaoMensalFacade movimentacaoMensalFacade;

    @EJB
    private Facade.LancamentoFinanceiroFacade lancamentoFinanceiroFacade;
    @EJB
    private Facade.ContaFacade contaFacade;

    @EJB
    private Facade.ContaReceberFacade contaReceberFacade;

    private ConverterGenerico pessoaConverter;
    private ConverterGenerico produtoConverter;
    private ConverterGenerico produtoDevConverter;
    private List<MetodoPagamento> metodosPagamentoFiltrados;
    private List<PlanoPagamento> planosPagamentos;
    private Venda vendaSelecionado;
    private Venda vendaParaFecharAVista;
    private Conta contaFinanceiraSelecionada;
    private String obsRecebimento;

    public boolean isMetodoDinheiroDaVendaParaFechar() {
        return vendaParaFecharAVista != null
                && vendaParaFecharAVista.getMetodoPagamento() == Entidades.Enums.MetodoPagamento.DINHEIRO;
    }

    @ManagedProperty("#{produtoControle}")
    private ProdutoControle produtoControle;

    @PostConstruct
    public void init() {
        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        Object id = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getFlash()
                .get("vendaId");

        if (id != null) {
            Long pid = (id instanceof Long) ? (Long) id : Long.valueOf(id.toString());
            this.venda = vendaFacade.findWithItens(pid);
            this.edit = true; 
            this.itensVenda = new ItensVenda(); 

        } else {
            this.edit = false;
            this.venda = new Venda();
            this.itensVenda = new ItensVenda();

            this.venda.setPlanoPagamento(PlanoPagamento.A_VISTA);
        }
        atualizarMetodosPagamento();
    }

    // Construtor
    public VendaControle() {
        this.planosPagamentos = PlanoPagamento.getPlanosPagamento();
        this.metodosPagamentoFiltrados = MetodoPagamento.getMetodosPagamentoAVista();

    }

    public void atualizarPreco() {
        if (itensVenda.getProdutoDerivacao() != null) {
            Produto produto = itensVenda.getProdutoDerivacao().getProduto();
            if (produto != null) {
                itensVenda.setValorUnitario(produto.getValorUnitarioVenda());
            }
        }
    }

    public void prepararVisualizacao(Venda ven) {
        this.vendaSelecionado = vendaFacade.findWithItens(ven.getId());
    }

    public void editar(Venda ven) {
        edit = true;
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("vendaId", ven.getId());

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("vendaCadastro.xhtml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void excluir(Venda ven) {
        vendaFacade.remover(ven);
    }

    public String novo() {
        edit = false;
        venda = new Venda();
        itensVenda = new ItensVenda();

        FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("vendaId");
        return "vendaCadastro.xhtml?faces-redirect=true";
    }

    public void atualizarMetodosPagamento() {
        if (null == venda.getPlanoPagamento()) {
            // Se não se enquadrar, não deixa nenhum método
            this.metodosPagamentoFiltrados = new ArrayList<>();
        } else {
            switch (venda.getPlanoPagamento()) {
                case A_VISTA:
                    // PIX, Débito e Dinheiro
                    this.metodosPagamentoFiltrados = new ArrayList<>();
                    this.metodosPagamentoFiltrados.add(MetodoPagamento.PIX);
                    this.metodosPagamentoFiltrados.add(MetodoPagamento.CARTAO_DEBITO);
                    this.metodosPagamentoFiltrados.add(MetodoPagamento.DINHEIRO);
                    break;
                case FIADO:
                    // Apenas PIX e Dinheiro
                    this.metodosPagamentoFiltrados = new ArrayList<>();
                    this.metodosPagamentoFiltrados.add(MetodoPagamento.A_DENIFIR);
                    this.metodosPagamentoFiltrados.add(MetodoPagamento.CARTAO_CREDITO);
                    break;
                default:
                    // Se não se enquadrar, não deixa nenhum método
                    this.metodosPagamentoFiltrados = new ArrayList<>();
                    break;
            }
        }
    }

    public void adicionarItem() {
        if (itensVenda.getProdutoDerivacao() == null || itensVenda.getQuantidade() == null
                || itensVenda.getValorUnitario() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                            "Preencha todos os campos para adicionar um item"));
            return;
        }

        // Pegando o estoque da derivação do produto
        Double estoque = itensVenda.getProdutoDerivacao().getQuantidade();
        ItensVenda itemTemp = null;

        for (ItensVenda it : venda.getItensVenda()) {
            if (it.getProdutoDerivacao().getId().equals(itensVenda.getProdutoDerivacao().getId())) {
                itemTemp = it;
                estoque -= it.getQuantidade();
            }
        }

        // Verificando se há estoque suficiente
        if (estoque < itensVenda.getQuantidade()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Estoque Insuficiente!",
                            "Restam apenas: " + estoque));
            return;
        }

        // Adicionando ou atualizando o item na venda
        if (itemTemp == null) {
            itensVenda.setVenda(venda);  // Associando o item à venda
            venda.getItensVenda().add(itensVenda);  // Adicionando o item à lista de itens da venda
        } else {
            if (!Objects.equals(itensVenda.getValorUnitario(), itemTemp.getValorUnitario())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                                "O valor do produto precisa ser o mesmo do já cadastrado"));
                return;
            }
            itemTemp.setQuantidade(itemTemp.getQuantidade() + itensVenda.getQuantidade());  // Atualizando quantidade
        }

        // Limpando o item temporário
        itensVenda = new ItensVenda();
    }

    public void removerItem(ItensVenda item) {
        venda.getItensVenda().remove(item);
        venda.setValorTotal(venda.getValorTotal() - item.getSubTotal());
        itensVenda = new ItensVenda();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Item removido com sucesso!"));
    }

    public void salvar() {
        StringBuilder mensagemErro = new StringBuilder("Preencha os Campos: ");
        boolean hasError = false;

        if (venda.getCliente() == null) {
            mensagemErro.append("Cliente, ");
            hasError = true;
        }
        if (venda.getFuncionario() == null) {
            mensagemErro.append("Funcionário, ");
            hasError = true;
        }
        if (venda.getDataVenda() == null) {
            mensagemErro.append("Data da Venda, ");
            hasError = true;
        }

        if (venda.getMetodoPagamento() == null) {
            mensagemErro.append("Metodo de Pagamento, ");
            hasError = true;
        }

        if (venda.getPlanoPagamento() == PlanoPagamento.FIADO) {
            if (venda.getDataVencimento() == null) {
                mensagemErro.append("Data de Vencimento, ");
                hasError = true;
            }
            if (venda.getParcelas() == null || venda.getParcelas() <= 0) {
                mensagemErro.append("Parcelas, ");
                hasError = true;
            }
        }

        if (hasError) {
            mensagemErro.setLength(mensagemErro.length() - 2);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagemErro.toString()));
            return;
        }
        if (venda.getPlanoPagamento() == PlanoPagamento.FIADO) {
            venda.calcularParcelas();
        }

        vendaFacade.salvarVenda(venda, edit);

        if (venda.getId() == null) {
            throw new IllegalStateException("Venda não foi persistida corretamente.");
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda salva com sucesso!"));
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("listaVenda.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        atualizarMetodosPagamento();
        edit = false;
        venda = new Venda();
    }

    public void fecharVenda(Venda ven) {
        try {
            vendaFacade.fecharVenda(ven);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Venda fechada com sucesso!", null));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao fechar a venda!", null));
        }
    }

    public void cancelarVenda(Venda venda) {
        try {
            vendaFacade.cancelarVenda(venda);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Venda cancelada e estoque devolvido com sucesso!", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao cancelar a venda!", null));
            e.printStackTrace();
        }
    }

    public void prepararFechamentoAVista(Venda ven) {
        this.vendaParaFecharAVista = vendaFacade.findWithItens(ven.getId());
        this.contaFinanceiraSelecionada = null;
        this.obsRecebimento = null;
    }

    public void confirmarFechamentoAVista() {
        if (vendaParaFecharAVista == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Venda não selecionada."));
            return;
        }
        if (contaFinanceiraSelecionada == null || contaFinanceiraSelecionada.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione a conta financeira."));
            return;
        }

        vendaFacade.fecharVenda(vendaParaFecharAVista);

        // pega a conta a receber gerada pra essa venda à vista
        ContaReceber cr = contaReceberFacade.findAvistaByVendaIdWithLancs(vendaParaFecharAVista.getId());
        if (cr == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível localizar a conta a receber gerada."));
            return;
        }

        // cria lançamento de entrada e vincula a conta a receber
        LancamentoFinanceiro lanc = new LancamentoFinanceiro();
        lanc.setConta(contaFinanceiraSelecionada);
        lanc.setTipo(Entidades.Enums.TipoLancamento.ENTRADA);
        lanc.setValor(vendaParaFecharAVista.getValorTotal());
        lanc.setDataHora(new Date());
        lanc.setMetodo(vendaParaFecharAVista.getMetodoPagamento());
        lanc.setStatus(Entidades.Enums.StatusLancamento.NORMAL);

        String desc = FinanceDesc.recebimentoVendaAVista(vendaParaFecharAVista, cr, obsRecebimento);

        lanc.setDescricao(desc);
        cr.addLancamento(lanc);

        lancamentoFinanceiroFacade.salvar(lanc);

        recomputarSaldo(contaFinanceiraSelecionada);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda fechada e recebimento registrado!"));

        vendaParaFecharAVista = null;
        contaFinanceiraSelecionada = null;
        obsRecebimento = null;
    }

    private void recomputarSaldo(Entidades.Conta conta) {
        Double inicial = conta.getValorInicial() != null ? conta.getValorInicial() : 0.0;
        Double entradas = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, Entidades.Enums.TipoLancamento.ENTRADA, null, null);
        Double saidas = lancamentoFinanceiroFacade.somarPorContaETipoEPeriodo(conta, Entidades.Enums.TipoLancamento.SAIDA, null, null);
        if (entradas == null) {
            entradas = 0d;
        }
        if (saidas == null) {
            saidas = 0d;
        }
        conta.setSaldo(inicial + entradas - saidas);
        contaFacade.salvar(conta);
    }

    public void exportarPDFFiltrado() throws DocumentException, IOException {
        List<Venda> vendasParaExportar = vendaFacade.buscarPorFiltros(clienteFiltro, funcionarioFiltro, produtoFiltro, dataInicio, dataFim);

        // Passa as datas de filtro para o método principal de exportação
        exportarPDF(vendasParaExportar, "Relatório de Vendas Detalhado", dataInicio, dataFim);
    }

    public void aplicarFiltro() {
        listaVendasFiltradas = vendaFacade.buscarPorFiltros(clienteFiltro, funcionarioFiltro, produtoFiltro, dataInicio, dataFim);
    }

    public void limparFiltros() {
        clienteFiltro = null;
        funcionarioFiltro = null;
        produtoFiltro = null;
        dataInicio = null;
        dataFim = null;
        listaVendasFiltradas = vendaFacade.listaTodosComItens(); // ou vazio, como preferir
    }

    public void exportarPDF(List<Venda> vendasParaExportar, String tituloRelatorio, Date dataIni, Date dataFim) throws DocumentException, IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_vendas.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        Locale ptBr = new Locale("pt", "BR");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(ptBr);
        DateFormat dfRelatorio = DateFormat.getDateInstance(DateFormat.SHORT, ptBr); // 31/10/2025
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr); // 31/10/2025 10:30

        java.util.function.Function<Object, String> s = obj -> (obj == null) ? "-" : obj.toString();
        java.util.function.Function<Date, String> sd = dt -> (dt == null) ? "-" : dfRelatorio.format(dt);
        java.util.function.Function<Number, String> sm = num -> (num == null) ? moeda.format(0) : moeda.format(num);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph(tituloRelatorio, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            String periodoStr = "Período: ";
            if (dataIni == null && dataFim == null) {
                periodoStr += "Todos os registros";
            } else {
                periodoStr += (dataIni != null ? sd.apply(dataIni) : "__/__/____") + " até " + (dataFim != null ? sd.apply(dataFim) : "__/__/____");
            }
            Paragraph periodo = new Paragraph(periodoStr, periodFont);
            periodo.setAlignment(Element.ALIGN_CENTER);
            periodo.setSpacingAfter(20);
            document.add(periodo);

            if (vendasParaExportar == null || vendasParaExportar.isEmpty()) {
                document.add(new Paragraph("Nenhum registro encontrado para o período.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {

                double valorTotalRelatorio = 0.0;

                for (Venda ven : vendasParaExportar) {

                    PdfPTable vendaTable = new PdfPTable(4);
                    vendaTable.setWidthPercentage(100);
                    vendaTable.setSpacingBefore(15);
                    vendaTable.setWidths(new float[]{1.5f, 3f, 1.5f, 3f});

                    vendaTable.addCell(createHeaderCell("Venda ID:"));
                    vendaTable.addCell(createDataCell(s.apply(ven.getId()), Element.ALIGN_LEFT));
                    vendaTable.addCell(createHeaderCell("Cliente:"));
                    vendaTable.addCell(createDataCell(s.apply(ven.getCliente().getNome()), Element.ALIGN_LEFT));

                    vendaTable.addCell(createHeaderCell("Data:"));
                    vendaTable.addCell(createDataCell(sd.apply(ven.getDataVenda()), Element.ALIGN_LEFT));
                    vendaTable.addCell(createHeaderCell("Funcionário:"));
                    vendaTable.addCell(createDataCell(s.apply(ven.getFuncionario().getNome()), Element.ALIGN_LEFT));

                    vendaTable.addCell(createHeaderCell("Status:"));
                    vendaTable.addCell(createDataCell(s.apply(ven.getStatus()), Element.ALIGN_LEFT));
                    vendaTable.addCell(createHeaderCell("Valor Total Venda:"));
                    vendaTable.addCell(createDataCell(sm.apply(ven.getValorTotal()), Element.ALIGN_RIGHT));

                    document.add(vendaTable);

                    if (ven.getItensVenda() != null && !ven.getItensVenda().isEmpty()) {
                        PdfPTable itemTable = new PdfPTable(4); // 4 colunas
                        itemTable.setWidthPercentage(100);
                        itemTable.setSpacingBefore(5);
                        itemTable.setWidths(new float[]{4f, 1f, 1.5f, 1.5f});

                        itemTable.addCell(createSubHeaderCell("Produto (Derivação)"));
                        itemTable.addCell(createSubHeaderCell("Qtd."));
                        itemTable.addCell(createSubHeaderCell("Vl. Unit."));
                        itemTable.addCell(createSubHeaderCell("Subtotal"));

                        for (ItensVenda item : ven.getItensVenda()) {
                            itemTable.addCell(createDataCell(s.apply(item.getProdutoDerivacao().getTexto()), Element.ALIGN_LEFT));
                            itemTable.addCell(createDataCell(s.apply(item.getQuantidade()), Element.ALIGN_CENTER));
                            itemTable.addCell(createDataCell(sm.apply(item.getValorUnitario()), Element.ALIGN_RIGHT));
                            itemTable.addCell(createDataCell(sm.apply(item.getSubTotal()), Element.ALIGN_RIGHT));

                            if (item.getSubTotal() != null) {
                                valorTotalRelatorio += item.getSubTotal();
                            }
                        }
                        document.add(itemTable);
                    }

                    Paragraph separator = new Paragraph(" ");
                    separator.setSpacingAfter(10);
                    document.add(separator);
                }

                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph totalPar = new Paragraph("Valor Total (Soma de todos os itens): " + sm.apply(valorTotalRelatorio), totalFont);
                totalPar.setAlignment(Element.ALIGN_RIGHT);
                totalPar.setSpacingBefore(15);
                document.add(totalPar);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

        } finally {
            document.close();
            writer.flush();
            writer.close();
            facesContext.responseComplete();
        }
    }

    private PdfPCell createHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(0, 51, 102));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createSubHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.BLACK); // Texto preto
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    // Getters e Setters adicionais
    public List<Venda> getListaVendas() {
        return vendaFacade.listaTodos();
    }

    public List<Venda> getListaVendasReais() {
        return vendaFacade.listaTodasReais();
    }

    public List<Venda> getListaVendasCanceladas() {
        return vendaFacade.listaVendasCanceladas();
    }

    public List<Produto> getListaProdutos() {
        return produtoFacade.listarProdutosAtivos();
    }

    public List<ProdutoDerivacao> getListaDerivacoes() {
        return produtoFacade.listarProdutosDerivacoesAtivas();
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public ItensVenda getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(ItensVenda itensVenda) {
        this.itensVenda = itensVenda;
    }

    public ProdutoControle getProdutoControle() {
        return produtoControle;
    }

    public void setProdutoControle(ProdutoControle produtoControle) {
        this.produtoControle = produtoControle;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public List<MetodoPagamento> getMetodosPagamentoFiltrados() {
        return metodosPagamentoFiltrados;
    }

    public List<PlanoPagamento> getPlanosPagamentos() {
        return planosPagamentos;
    }

    public List<Pessoa> getListaFuncionariosFiltrando(String filtro) {
        return pessoaFacade.listaFuncionarioFiltrando(filtro, "nome", "cpfcnpj");
    }

    public List<Pessoa> getListaClientesFiltrando(String filtro) {
        return pessoaFacade.listaClienteFiltrando(filtro, "nome", "cpfcnpj");
    }

    public List<Produto> getListaProdutosFiltrando(String filtro) {
        return produtoFacade.listaFiltrar(filtro, "categoria");
    }

    public ConverterGenerico getPessoaConverter() {
        if (pessoaConverter == null) {
            pessoaConverter = new ConverterGenerico(pessoaFacade);
        }
        return pessoaConverter;
    }

    public ConverterGenerico getProdutoConverter() {
        if (produtoConverter == null) {
            produtoConverter = new ConverterGenerico(produtoFacade);
        }
        return produtoConverter;
    }

    public ConverterGenerico getProdutoDevConverter() {
        if (produtoDevConverter == null) {
            produtoDevConverter = new ConverterGenerico(produtoDevFacade);
        }
        return produtoDevConverter;
    }

    public void setProdutoConverter(ConverterGenerico produtoConverter) {
        this.produtoConverter = produtoConverter;
    }

    public void setProdutoDevConverter(ConverterGenerico produtoDevConverter) {
        this.produtoDevConverter = produtoDevConverter;
    }

    public void setClienteConverter(ConverterGenerico estadoConverter) {
        this.pessoaConverter = estadoConverter;
    }

    public PessoaFacade getClienteFacade() {
        if (pessoaConverter == null) {
            pessoaConverter = new ConverterGenerico(pessoaFacade);
        }
        return pessoaFacade;
    }

    public void setClienteFacade(PessoaFacade estadoFacade) {
        this.pessoaFacade = estadoFacade;
    }

    public ProdutoFacade getProdutoFacade() {
        if (produtoConverter == null) {
            produtoConverter = new ConverterGenerico(produtoFacade);
        }
        return produtoFacade;
    }

    public VendaFacade getVendaFacade() {
        return vendaFacade;
    }

    public void setVendaFacade(VendaFacade vendaFacade) {
        this.vendaFacade = vendaFacade;
    }

    public MovimentacaoMensalFacade getMovimentacaoMensalFacade() {
        return movimentacaoMensalFacade;
    }

    public void setMovimentacaoMensalFacade(MovimentacaoMensalFacade movimentacaoMensalFacade) {
        this.movimentacaoMensalFacade = movimentacaoMensalFacade;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Venda getVendaSelecionado() {
        return vendaSelecionado;
    }

    public void setVendaSelecionado(Venda vendaSelecionado) {
        this.vendaSelecionado = vendaSelecionado;
    }

    public Venda getVendaParaFecharAVista() {
        return vendaParaFecharAVista;
    }

    public void setVendaParaFecharAVista(Venda vendaParaFecharAVista) {
        this.vendaParaFecharAVista = vendaParaFecharAVista;
    }

    public Conta getContaFinanceiraSelecionada() {
        return contaFinanceiraSelecionada;
    }

    public void setContaFinanceiraSelecionada(Conta contaFinanceiraSelecionada) {
        this.contaFinanceiraSelecionada = contaFinanceiraSelecionada;
    }

    public String getObsRecebimento() {
        return obsRecebimento;
    }

    public void setObsRecebimento(String obsRecebimento) {
        this.obsRecebimento = obsRecebimento;
    }

    public Pessoa getClienteFiltro() {
        return clienteFiltro;
    }

    public void setClienteFiltro(Pessoa clienteFiltro) {
        this.clienteFiltro = clienteFiltro;
    }

    public Produto getProdutoFiltro() {
        return produtoFiltro;
    }

    public void setProdutoFiltro(Produto produtoFiltro) {
        this.produtoFiltro = produtoFiltro;
    }

    public Pessoa getFuncionarioFiltro() {
        return funcionarioFiltro;
    }

    public void setFuncionarioFiltro(Pessoa funcionarioFiltro) {
        this.funcionarioFiltro = funcionarioFiltro;
    }

}
