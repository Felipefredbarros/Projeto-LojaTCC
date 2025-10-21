package Controladores;

import Converters.ConverterGenerico;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Facade.CategoriaFacade;
import Facade.CompraFacade;
import Facade.MarcaFacade;
import Facade.ProdutoDerivacaoFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
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
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author felip
 */
@ManagedBean
@ViewScoped
public class ProdutoControle implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(PT_BR);

    private Produto produto = new Produto();
    private ProdutoDerivacao prodDev = new ProdutoDerivacao();
    private Produto produtoSelecionado;
    private Date dataInicioAnalise;
    private Date dataFimAnalise;

    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private CategoriaFacade categoriaFacade;
    @EJB
    private MarcaFacade marcaFacade;
    @EJB
    private ProdutoDerivacaoFacade produtoDevFacade;
    @EJB
    private VendaFacade vendaFacade;
    @EJB
    private CompraFacade compraFacade;

    @ManagedProperty("#{categoriaControle}")
    private CategoriaControle categoriaControle;

    @ManagedProperty("#{marcaControle}")
    private MarcaControle marcaControle;

    private ConverterGenerico categoriaConverter;
    private ConverterGenerico marcaConverter;

    @PostConstruct
    public void init() {
        produto = new Produto();
        prodDev = new ProdutoDerivacao();

        Object id = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getFlash()
                .get("produtoId");
        if (id != null) {
            Long pid = (id instanceof Long) ? (Long) id : Long.valueOf(id.toString());
            this.produto = produtoFacade.findWithDerivacoes(pid);
            this.prodDev.setProduto(produto);
        }
    }

    public ConverterGenerico getCategoriaConverter() {
        if (categoriaConverter == null) {
            categoriaConverter = new ConverterGenerico(categoriaFacade);
        }
        return categoriaConverter;
    }

    public ConverterGenerico getMarcaConverter() {
        if (marcaConverter == null) {
            marcaConverter = new ConverterGenerico(marcaFacade);
        }
        return marcaConverter;
    }

    public String novo() {
        this.produto = new Produto();
        this.prodDev = new ProdutoDerivacao();

        FacesContext.getCurrentInstance()
                .getExternalContext()
                .getFlash()
                .remove("produtoId");

        return "produtoCadastro.xhtml?faces-redirect=true";
    }

    public void salvar() {

        if (categoriaControle.getListaCategorias() == null || categoriaControle.getListaCategorias().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cadastre pelo menos uma categoria antes de salvar um produto.", ""));
            return;
        }
        if (marcaControle.getListaMarcas() == null || marcaControle.getListaMarcas().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cadastre pelo menos uma marca antes de salvar um produto.", ""));
            return;
        }

        produtoFacade.salvar(produto);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Produto salvo com sucesso!"));
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

        try {

            FacesContext.getCurrentInstance().getExternalContext().redirect("listaProduto.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        produto = new Produto();
        prodDev = new ProdutoDerivacao();
    }

    public void editar(Produto prod) {
        if (produtoFacade.produtoTemVendas(prod.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Este produto possui vendas relacionadas e não pode ser editado"));
            return;
        }

        if (produtoFacade.produtoTemCompras(prod.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Este produto possui compras relacionadas e não pode ser editado"));
            return;
        }

        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("produtoId", prod.getId());
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("produtoCadastro.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void excluir(Produto prod) {
        if ((produtoFacade.produtoTemVendas(prod.getId()) || produtoFacade.produtoTemCompras(prod.getId())) && prod.getAtivo() == true) {
            prod.setAtivo(false);
            produtoFacade.salvar(prod);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Produto inativado", "O produto foi inativado com sucesso e não pode mais ser usado nas vendas/compras."));
            return;
        }
        if (prod.getAtivo() == false) {
            if (produtoFacade.produtoTemVendas(prod.getId()) || produtoFacade.produtoTemCompras(prod.getId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Erro ao Excluir", "Produto inativado, para excluir exclua as vendas/compras relacionadas a ele"));
                return;
            } else {
                produtoFacade.remover(prod);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Sucesso", "Produto inativado excluído com sucesso!"));
                return;
            }
        }

        produtoFacade.remover(prod);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso", "Produto excluído com sucesso!"));
    }
    
    public void ativar(Produto prod){
        prod.setAtivo(true);
        produtoFacade.salvar(prod);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso",
                        "Produto ativado com sucesso!"));
    }

    public void adicionarDerivacao() {
        StringJoiner sj = new StringJoiner(", ", "Preencha os campos obrigatórios: ", "");

        if (prodDev.getTamanho() == null || prodDev.getTamanho().trim().isEmpty()) {
            sj.add("Tamanho");
        }
        if (prodDev.getDescricao() == null || prodDev.getDescricao().trim().isEmpty()) {
            sj.add("Descrição");
        }
        if (prodDev.getCor() == null || prodDev.getCor().trim().isEmpty()) {
            sj.add("Cor");
        }
        if (prodDev.getQuantidade() == null) {
            sj.add("Quantidade");
        }

        if (sj.length() > "Preencha os campos obrigatórios: ".length()) {
            FacesContext.getCurrentInstance().addMessage(
                    "form:adicionarBtn",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, sj.toString(), null)
            );
            return;
        }

        prodDev.setProduto(produto);
        produto.getVariacoes().add(prodDev);
        prodDev = new ProdutoDerivacao();

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Derivação adicionada!")
        );
    }

    public void removerDerivacao(ProdutoDerivacao dev) {
        if (dev == null) {
            return;
        }
        if (produto != null && produto.getVariacoes() != null) {
            produto.getVariacoes().remove(dev);
        }
        prodDev = new ProdutoDerivacao();
        limpaCampos();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Derivação removido com sucesso!"));
    }

    public void limpaCampos() {
        prodDev.setCor("");
        prodDev.setQuantidade(null);
        prodDev.setTamanho("");
        prodDev.setDescricao(null);
    }

    public void prepararVisualizacao(Produto prod) {
        if (prod == null || prod.getId() == null) {
            this.produtoSelecionado = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível selecionar a pessoa para visualização."));
            return;
        }

        try {
            this.produtoSelecionado = produtoFacade.findWithDerivacoes(prod.getId());

        } catch (javax.persistence.NoResultException nre) {
            this.produtoSelecionado = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada com o ID fornecido."));
        } catch (Exception e) {
            this.produtoSelecionado = null;
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Ocorreu um erro inesperado ao carregar os detalhes da pessoa."));
        }
    }

    public List<Produto> getListaProdutos() {
        return produtoFacade.listarProdutosAtivos();
    }

    public List<Produto> getListaProdutosInativos() {
        return produtoFacade.listarProdutosInativos();
    }

    // pdf
    public void exportarPDF(boolean somenteVisivel) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_produtos.pdf");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR); // Para o rodapé

        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();
        java.util.function.Function<Number, String> sm = num -> (num == null) ? moeda.format(0) : moeda.format(num);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Relatório de Produtos e Variações", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            List<Produto> produtosParaExportar = somenteVisivel ? getProdutosVisiveisNaTabela() : produtoFacade.listarProdutosAtivos();

            if (produtosParaExportar == null || produtosParaExportar.isEmpty()) {
                document.add(new Paragraph("Nenhum produto encontrado para este relatório.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {

                PdfPTable mainTable = new PdfPTable(6);
                mainTable.setWidthPercentage(100);
                mainTable.setWidths(new float[]{1.8f, 1.8f, 1.2f, 1.2f, 1.2f, 4.8f});

                mainTable.addCell(createHeaderCell("Categoria"));
                mainTable.addCell(createHeaderCell("Marca"));
                mainTable.addCell(createHeaderCell("NCM"));
                mainTable.addCell(createHeaderCell("Vl. Compra"));
                mainTable.addCell(createHeaderCell("Vl. Venda"));
                mainTable.addCell(createHeaderCell("Variações (Tamanho, Cor, Estoque, Descrição)"));

                for (Produto p : produtosParaExportar) {
                    mainTable.addCell(createDataCell(s.apply(p.getCategoria() != null ? p.getCategoria().getCategoria() : null), Element.ALIGN_LEFT));
                    mainTable.addCell(createDataCell(s.apply(p.getMarca() != null ? p.getMarca().getMarca() : null), Element.ALIGN_LEFT));
                    mainTable.addCell(createDataCell(s.apply(p.getNcm()), Element.ALIGN_CENTER));
                    mainTable.addCell(createDataCell(sm.apply(p.getValorUnitarioCompra()), Element.ALIGN_RIGHT));
                    mainTable.addCell(createDataCell(sm.apply(p.getValorUnitarioVenda()), Element.ALIGN_RIGHT));

                    PdfPCell derivationsCell = new PdfPCell();
                    derivationsCell.setPadding(2);
                    List<ProdutoDerivacao> variacoes = p.getVariacoes();
                    if (variacoes != null && !variacoes.isEmpty()) {
                        PdfPTable derivationTable = new PdfPTable(4);
                        derivationTable.setWidthPercentage(100);
                        derivationTable.setWidths(new float[]{1.5f, 1.5f, 1f, 3f});

                        derivationTable.addCell(createNestedHeaderCell("Tamanho"));
                        derivationTable.addCell(createNestedHeaderCell("Cor"));
                        derivationTable.addCell(createNestedHeaderCell("Estoque"));
                        derivationTable.addCell(createNestedHeaderCell("Descrição"));

                        for (ProdutoDerivacao d : variacoes) {
                            derivationTable.addCell(createNestedDataCell(s.apply(d.getTamanho()), Element.ALIGN_LEFT));
                            derivationTable.addCell(createNestedDataCell(s.apply(d.getCor()), Element.ALIGN_LEFT));
                            String quantidadeStr = (d.getQuantidade() != null) ? d.getQuantidade().toString() : "0";
                            derivationTable.addCell(createNestedDataCell(quantidadeStr, Element.ALIGN_CENTER));
                            derivationTable.addCell(createNestedDataCell(s.apply(d.getDescricao()), Element.ALIGN_LEFT));
                        }
                        derivationsCell.addElement(derivationTable);
                    } else {
                        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLACK);
                        Phrase emptyVariationsPhrase = new Phrase("Sem variações", nestedContentFont);
                        derivationsCell.addElement(emptyVariationsPhrase);
                        derivationsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        derivationsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    }
                    mainTable.addCell(derivationsCell);
                }

                document.add(mainTable);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(currentY < 50 ? 10 : currentY - 30);
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    public void exportarPDFEstoqueTotal() throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_estoque_total.pdf");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR);
        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Relatório de Estoque Total", subtitleFont); 
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            List<ProdutoDerivacao> derivacoes = produtoDevFacade.listarTodasOrdenadasComProduto();

            if (derivacoes == null || derivacoes.isEmpty()) {
                document.add(new Paragraph("Nenhuma derivação de produto encontrada.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 1.5f, 1.5f, 3f, 1f});

                table.addCell(createHeaderCell("Produto"));
                table.addCell(createHeaderCell("Tamanho"));
                table.addCell(createHeaderCell("Cor"));
                table.addCell(createHeaderCell("Descrição Específica"));
                table.addCell(createHeaderCell("Qtd. Estoque"));

                for (ProdutoDerivacao pd : derivacoes) {
                    table.addCell(createDataCell(s.apply(pd.getProduto() != null ? pd.getProduto().getCategoria().getCategoria() : "Produto Inválido"), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(pd.getTamanho()), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(pd.getCor()), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(pd.getDescricao()), Element.ALIGN_LEFT));
                    String quantidadeStr = (pd.getQuantidade() != null) ? pd.getQuantidade().toString() : "0";
                    table.addCell(createDataCell(quantidadeStr, Element.ALIGN_CENTER));
                }
                document.add(table);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(Math.max(15, currentY - document.bottomMargin() - footer.getTotalLeading()));
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    public void exportarPDFMaisVendidos() throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_mais_vendidos.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 30, 20); // Retrato
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        DateFormat dfRelatorio = DateFormat.getDateInstance(DateFormat.SHORT, PT_BR);
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR);
        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();
        java.util.function.Function<Date, String> sd = dt -> (dt == null) ? "__/__/____" : dfRelatorio.format(dt);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Relatório de Produtos Mais Vendidos", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            String periodoStr = "Período: ";
            periodoStr += (dataInicioAnalise == null && dataFimAnalise == null) ? "Todos os registros" : sd.apply(dataInicioAnalise) + " até " + sd.apply(dataFimAnalise);
            Paragraph periodo = new Paragraph(periodoStr, periodFont);
            periodo.setAlignment(Element.ALIGN_CENTER);
            periodo.setSpacingAfter(10);
            document.add(periodo);

            List<Object[]> resultados = vendaFacade.buscarProdutosMaisVendidos(dataInicioAnalise, dataFimAnalise);

            if (resultados == null || resultados.isEmpty()) {
                document.add(new Paragraph("Nenhum produto vendido encontrado no período selecionado.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f, 6f, 2f});

                table.addCell(createHeaderCell("Rank"));
                table.addCell(createHeaderCell("Produto (Derivação)"));
                table.addCell(createHeaderCell("Quantidade Vendida"));

                int rank = 1;
                for (Object[] resultado : resultados) {
                    Long derivacaoId = (Long) resultado[0];
                    Number totalVendidoNumber = (Number) resultado[1];
                    Double totalVendido = totalVendidoNumber.doubleValue();

                    ProdutoDerivacao pd = produtoDevFacade.buscar(derivacaoId);

                    table.addCell(createDataCell(String.valueOf(rank++), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(s.apply(pd != null ? pd.getTexto() : "ID: " + derivacaoId + " (Não encontrado)"), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(totalVendido), Element.ALIGN_CENTER));
                }
                document.add(table);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(Math.max(15, currentY - document.bottomMargin() - footer.getTotalLeading()));
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    public void exportarPDFMaisComprados() throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_mais_comprados.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        DateFormat dfRelatorio = DateFormat.getDateInstance(DateFormat.SHORT, PT_BR);
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR);
        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();
        java.util.function.Function<Date, String> sd = dt -> (dt == null) ? "__/__/____" : dfRelatorio.format(dt);

        try {
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Relatório de Produtos Mais Comprados", subtitleFont); // Título específico
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            String periodoStr = "Período: ";
            periodoStr += (dataInicioAnalise == null && dataFimAnalise == null) ? "Todos os registros" : sd.apply(dataInicioAnalise) + " até " + sd.apply(dataFimAnalise);
            Paragraph periodo = new Paragraph(periodoStr, periodFont);
            periodo.setAlignment(Element.ALIGN_CENTER);
            periodo.setSpacingAfter(10);
            document.add(periodo);

            List<Object[]> resultados = compraFacade.buscarProdutosMaisComprados(dataInicioAnalise, dataFimAnalise);

            if (resultados == null || resultados.isEmpty()) {
                document.add(new Paragraph("Nenhum produto comprado encontrado no período selecionado.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f, 6f, 2f});

                table.addCell(createHeaderCell("Rank"));
                table.addCell(createHeaderCell("Produto (Derivação)"));
                table.addCell(createHeaderCell("Quantidade Comprada"));

                int rank = 1;
                for (Object[] resultado : resultados) {
                    Long derivacaoId = (Long) resultado[0];
                    Number totalCompradoNumber = (Number) resultado[1];
                    Double totalComprado = totalCompradoNumber.doubleValue();

                    ProdutoDerivacao pd = produtoDevFacade.buscar(derivacaoId); 

                    table.addCell(createDataCell(String.valueOf(rank++), Element.ALIGN_CENTER));
                    table.addCell(createDataCell(s.apply(pd != null ? pd.getTexto() : "ID: " + derivacaoId + " (Não encontrado)"), Element.ALIGN_LEFT));
                    table.addCell(createDataCell(s.apply(totalComprado), Element.ALIGN_CENTER));
                }
                document.add(table);
            }

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em: " + dfCompleto.format(new Date()), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            float currentY = writer.getVerticalPosition(true);
            footer.setSpacingBefore(Math.max(15, currentY - document.bottomMargin() - footer.getTotalLeading()));
            document.add(footer);

        } finally {
            document.close();
            facesContext.responseComplete();
        }
    }

    private PdfPCell createHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(0, 51, 102));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createNestedHeaderCell(String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.BOLD, Color.DARK_GRAY);
        PdfPCell cell = new PdfPCell(new Phrase(content, headerFont));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(Color.GRAY);
        return cell;
    }

    private PdfPCell createNestedDataCell(String content, int alignment) {
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(content, contentFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(Color.GRAY);
        return cell;
    }

    private List<Produto> getProdutosVisiveisNaTabela() {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("formListagemProdutos:tabelaProdutos");

        if (dataTable == null) {
            System.err.println("WARN: DataTable 'formListagemProdutos:tabelaProdutos' não encontrada. Exportando todos os produtos ativos.");
            return produtoFacade.listarProdutosAtivos();
        }

        List<Produto> listaFiltrada = (List<Produto>) dataTable.getFilteredValue();

        if (listaFiltrada == null) {
            Object tableValue = dataTable.getValue();
            if (tableValue instanceof List) {
                List<?> rawList = (List<?>) tableValue;
                if (!rawList.isEmpty() && rawList.get(0) instanceof Produto) {
                    listaFiltrada = (List<Produto>) rawList;
                } else if (rawList.isEmpty()) {
                    listaFiltrada = new ArrayList<>();
                } else {
                    System.err.println("WARN: O valor da DataTable não é uma lista de Produtos. Exportando todos os produtos ativos.");
                    return produtoFacade.listarProdutosAtivos();
                }
            } else {
                System.err.println("WARN: O valor da DataTable não é uma lista. Exportando todos os produtos ativos.");
                return produtoFacade.listarProdutosAtivos();
            }
        }

        return listaFiltrada != null ? listaFiltrada : new ArrayList<>();
    }

    private String moeda(Double v) {
        if (v == null) {
            return CURRENCY.format(0);
        }
        return CURRENCY.format(v);
    }

    //get e set
    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public void setCategoriaControle(CategoriaControle categoriaControle) {
        this.categoriaControle = categoriaControle;
    }

    public void setMarcaControle(MarcaControle marcaControle) {
        this.marcaControle = marcaControle;
    }

    public ProdutoDerivacao getProdDev() {
        return prodDev;
    }

    public void setProdDev(ProdutoDerivacao prodDev) {
        this.prodDev = prodDev;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public Date getDataInicioAnalise() {
        return dataInicioAnalise;
    }

    public void setDataInicioAnalise(Date dataInicioAnalise) {
        this.dataInicioAnalise = dataInicioAnalise;
    }

    public Date getDataFimAnalise() {
        return dataFimAnalise;
    }

    public void setDataFimAnalise(Date dataFimAnalise) {
        this.dataFimAnalise = dataFimAnalise;
    }

}
