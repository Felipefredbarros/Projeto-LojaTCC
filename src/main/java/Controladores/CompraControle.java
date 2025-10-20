/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Converters.ConverterGenerico;
import Entidades.ItensCompra;
import Entidades.Enums.MetodoPagamento;
import Entidades.Pessoa;
import Entidades.Enums.PlanoPagamento;
import Entidades.Produto;
import Entidades.Compra;
import Entidades.ProdutoDerivacao;
import Facade.PessoaFacade;
import Facade.ProdutoFacade;
import Facade.CompraFacade;
import Facade.ProdutoDerivacaoFacade;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@Named("compraControle")
@ViewScoped
public class CompraControle implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(PT_BR);
    private Compra compra = new Compra();

    private ItensCompra itensCompra = new ItensCompra();
    private Date dataInicio;
    private Date dataFim;
    private Compra compraSelecionado;
    private Pessoa fornecedorFiltro;
    private Produto produtoFiltro;

    private boolean mostrarParcelas;
    private Boolean edit = false;

    private List<PlanoPagamento> planosPagamentos;
    private List<Compra> listaComprasFiltradas = new ArrayList<>();

    @EJB
    private CompraFacade compraFacade;
    @EJB
    private PessoaFacade pessoaFacade;
    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private ProdutoDerivacaoFacade produtoDevFacade;

    private ConverterGenerico pessoaConverter;
    private ConverterGenerico produtoConverter;
    private ConverterGenerico produtoDevConverter;

    @ManagedProperty("#{produtoControle}")
    private ProdutoControle produtoControle;

    @PostConstruct
    public void init() {
        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }
        edit = false;
        compra = new Compra();
        itensCompra = new ItensCompra();
        Object id = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getFlash()
                .get("compraId");
        if (id != null) {
            Long pid = (id instanceof Long) ? (Long) id : Long.valueOf(id.toString());
            this.compra = compraFacade.findWithItens(pid);
        }

    }

    public CompraControle() {
        this.planosPagamentos = PlanoPagamento.getPlanosPagamento();
        this.compra = new Compra();
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

    public String novo() {
        edit = false;
        compra = new Compra();
        itensCompra = new ItensCompra();

        FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("compraId");
        return "compraCadastro.xhtml?faces-redirect=true";
    }

    public void salvar() {
        StringBuilder mensagemErro = new StringBuilder("Preencha os Campos: ");
        boolean hasError = false;
        if (compra.getFornecedor() == null) {
            mensagemErro.append("Fornecedor, ");
            hasError = true;
        }
        if (compra.getDataCompra() == null) {
            mensagemErro.append("Data da Compra, ");
            hasError = true;
        }
        if (compra.getDataVencimento() == null) {
            mensagemErro.append("Data de Vencimento, ");
            hasError = true;
        }
        if (compra.getParcelas() == null || compra.getParcelas() <= 0) {
            mensagemErro.append("Parcelas, ");
            hasError = true;
        }

        if (hasError) {
            mensagemErro.setLength(mensagemErro.length() - 2);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", mensagemErro.toString()));
            return;
        }

        compra.calcularParcelas();
        compra.setPlanoPagamento(PlanoPagamento.PARCELADO_COMPRA);
        compra.setMetodoPagamento(MetodoPagamento.A_DENIFIR);
        compraFacade.salvarCompra(compra, edit);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Compra salva com sucesso!"));
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("listaCompra.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        edit = false;
        compra = new Compra();
    }

    public void editar(Compra ven) {
        edit = true;
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("compraId", ven.getId());

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("compraCadastro.xhtml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void excluir(Compra ven) {
        compraFacade.remover(ven);
    }

    public void adicionarItem() {
        if (itensCompra.getProdutoDerivacao() == null
                || itensCompra.getQuantidade() == null
                || itensCompra.getQuantidade() <= 0
                || itensCompra.getValorUnitario() == null) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Selecione o produto, informe quantidade e valor."));
            return;
        }

        ItensCompra itemExistente = null;
        for (ItensCompra it : compra.getItensCompra()) {
            if (it.getProdutoDerivacao().getId().equals(itensCompra.getProdutoDerivacao().getId())) {
                itemExistente = it;
                break;
            }
        }

        if (itemExistente == null) {
            itensCompra.setCompra(compra);
            compra.addItem(itensCompra);
        } else {
            if (!Objects.equals(itensCompra.getValorUnitario(), itemExistente.getValorUnitario())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erro", "O valor do produto precisa ser o mesmo do já cadastrado"));
                return;
            }
            itemExistente.setQuantidade(itemExistente.getQuantidade() + itensCompra.getQuantidade());

        }
        compra.setValorTotal(
                (compra.getValorTotal() == null ? 0d : compra.getValorTotal())
                + itensCompra.getSubTotal()
        );
        itensCompra = new ItensCompra();

    }

    public void removerItem(ItensCompra item) {
        if (item == null) {
            return;
        }

        compra.removeItem(item); // usa o helper da entidade
        compra.setValorTotal(
                (compra.getValorTotal() == null ? 0d : compra.getValorTotal()) - item.getSubTotal()
        );
        itensCompra = new ItensCompra();
        limpaCampos();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Item removido com sucesso!"));
    }

    public void atualizarPreco() {
        if (itensCompra.getProdutoDerivacao() != null) {
            Produto produto = itensCompra.getProdutoDerivacao().getProduto();
            if (produto != null) {
                itensCompra.setValorUnitario(produto.getValorUnitarioVenda());
            }
        }
    }

    public void fecharCompra(Compra com) {
        com = compraFacade.findWithItens(com.getId());

        try {
            compraFacade.fecharCompra(com);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compra fechada com sucesso!", null));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao fechar a Compra!", null));
        }
    }

    public void cancelarCompra(Compra com) {
        try {
            compraFacade.cancelarCompra(com);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compra cancelada e estoque removido com sucesso!", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao cancelar a compra!", null));
            e.printStackTrace();
        }
    }

    public void limpaCampos() {
        itensCompra.setQuantidade(null);
        itensCompra.setValorUnitario(null);
    }

    public void prepararVisualizacao(Compra com) {
        this.compraSelecionado = compraFacade.findWithItens(com.getId());
    }

    public List<Compra> getListaCompras() {
        return compraFacade.listaTodos();
    }

    public List<Compra> getListaComprasReais() {
        return compraFacade.listaTodasReais();
    }

    public List<Compra> getListaComprasCanceladas() {
        return compraFacade.listaComprasCanceladas();
    }

    public List<Produto> getListaProdutos() {
        return produtoFacade.listarProdutosAtivos();
    }

    public List<ProdutoDerivacao> getListaDerivacoes() {
        return produtoFacade.listarProdutosDerivacoesAtivas();
    }

    public List<PlanoPagamento> getPlanosPagamentos() {
        return planosPagamentos;
    }

    public List<Pessoa> getListaFornecedoresFiltrando(String filtro) {
        return pessoaFacade.listaFornecedorFiltrando(filtro, "nome", "cpfcnpj");
    }

    public List<Produto> getListaProdutosFiltrando(String filtro) {
        return produtoFacade.listaFiltrar(filtro, "categoria", "tipo");
    }

    //pdf
    public void exportarPDF(List<Compra> comprasParaExportar, String tituloRelatorio, Date dataIni, Date dataFim) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_compras.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
        DateFormat dfRelatorio = DateFormat.getDateInstance(DateFormat.SHORT, PT_BR); // 31/10/2025
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR); // 31/10/2025 10:30

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

            if (comprasParaExportar == null || comprasParaExportar.isEmpty()) {
                document.add(new Paragraph("Nenhum registro encontrado para o período.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {

                double valorTotalRelatorio = 0.0;

                for (Compra com : comprasParaExportar) {

                    PdfPTable compraTable = new PdfPTable(4);
                    compraTable.setWidthPercentage(100);
                    compraTable.setSpacingBefore(15);
                    compraTable.setWidths(new float[]{1.5f, 3f, 1.5f, 3f});

                    compraTable.addCell(createHeaderCell("Compra ID:"));
                    compraTable.addCell(createDataCell(s.apply(com.getId()), Element.ALIGN_LEFT));
                    compraTable.addCell(createHeaderCell("Fornecedor:"));
                    compraTable.addCell(createDataCell(s.apply(com.getFornecedor().getNome()), Element.ALIGN_LEFT));

                    compraTable.addCell(createHeaderCell("Data:"));
                    compraTable.addCell(createDataCell(sd.apply(com.getDataCompra()), Element.ALIGN_LEFT));
                    compraTable.addCell(createHeaderCell("Status:"));
                    compraTable.addCell(createDataCell(s.apply(com.getStatus()), Element.ALIGN_LEFT));

                    compraTable.addCell(createHeaderCell("Vencimento:")); // Adicionado vencimento
                    compraTable.addCell(createDataCell(sd.apply(com.getDataVencimento()), Element.ALIGN_LEFT));
                    compraTable.addCell(createHeaderCell("Valor Total Compra:"));
                    compraTable.addCell(createDataCell(sm.apply(com.getValorTotal()), Element.ALIGN_RIGHT));

                    document.add(compraTable);

                    if (com.getItensCompra() != null && !com.getItensCompra().isEmpty()) {
                        PdfPTable itemTable = new PdfPTable(4); // 4 colunas
                        itemTable.setWidthPercentage(100);
                        itemTable.setSpacingBefore(5);
                        itemTable.setWidths(new float[]{4f, 1f, 1.5f, 1.5f}); // Produto, Qtd, Vl. Unit., Subtotal

                        itemTable.addCell(createSubHeaderCell("Produto (Derivação)"));
                        itemTable.addCell(createSubHeaderCell("Qtd."));
                        itemTable.addCell(createSubHeaderCell("Vl. Unit."));
                        itemTable.addCell(createSubHeaderCell("Subtotal"));

                        for (ItensCompra item : com.getItensCompra()) {
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

    public List<MetodoPagamento> getMetodosParcelados() {
        List<MetodoPagamento> metodosParcelados = new ArrayList<>();
        metodosParcelados.add(MetodoPagamento.CARTAO_CREDITO);
        return metodosParcelados;
    }

    public void exportarPDFFiltrado() throws DocumentException, IOException {
        List<Compra> comprasParaExportar = compraFacade.buscarPorFiltros(fornecedorFiltro, produtoFiltro, dataInicio, dataFim);
        exportarPDF(comprasParaExportar, "Relatório de Compras Detalhado", dataInicio, dataFim);
    }

    public void aplicarFiltro() {
        listaComprasFiltradas = compraFacade.buscarPorFiltros(fornecedorFiltro, produtoFiltro, dataInicio, dataFim);
    }

    public void limparFiltros() {
        fornecedorFiltro = null;
        produtoFiltro = null;
        dataInicio = null;
        dataFim = null;
        listaComprasFiltradas = compraFacade.listaTodosComItens(); // ou vazio, como preferir
    }

    private PdfPCell createHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(0, 51, 102)); // Azul escuro
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alinhado à direita
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    // Helper - Célula de Sub-Cabeçalho (Cinza)
    private PdfPCell createSubHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.BLACK); // Texto preto
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(220, 220, 220)); // Cinza claro
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    // Helper para Célula de Dados (Branca)
    private PdfPCell createDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    //get e set
    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public ItensCompra getItensCompra() {
        return itensCompra;
    }

    public void setItensCompra(ItensCompra itensCompra) {
        this.itensCompra = itensCompra;
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

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Compra getCompraSelecionado() {
        return compraSelecionado;
    }

    public void setCompraSelecionado(Compra compraSelecionado) {
        this.compraSelecionado = compraSelecionado;
    }

    public Pessoa getFornecedorFiltro() {
        return fornecedorFiltro;
    }

    public void setFornecedorFiltro(Pessoa fornecedorFiltro) {
        this.fornecedorFiltro = fornecedorFiltro;
    }

    public Produto getProdutoFiltro() {
        return produtoFiltro;
    }

    public void setProdutoFiltro(Produto produtoFiltro) {
        this.produtoFiltro = produtoFiltro;
    }

    public ProdutoControle getProdutoControle() {
        return produtoControle;
    }

    public void setProdutoControle(ProdutoControle produtoControle) {
        this.produtoControle = produtoControle;
    }

}
