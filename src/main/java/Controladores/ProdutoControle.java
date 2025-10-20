package Controladores;

import Converters.ConverterGenerico;
import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Facade.CategoriaFacade;
import Facade.MarcaFacade;
import Facade.ProdutoDerivacaoFacade;
import Facade.ProdutoFacade;
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
import java.text.NumberFormat;
import java.util.ArrayList;
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

    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private CategoriaFacade categoriaFacade;
    @EJB
    private MarcaFacade marcaFacade;
    @EJB
    private ProdutoDerivacaoFacade produtoDevFacade;

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

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Cabeçalho
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
        Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
        Paragraph subtitle = new Paragraph("Relatório de Produtos e Variações", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Tabela principal
        PdfPTable mainTable = new PdfPTable(6);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.6f, 1.6f, 1.1f, 1.2f, 1.2f, 3.3f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
        String[] headers = {"Categoria", "Marca", "NCM", "Valor Compra", "Valor Venda", "Variações (Tamanho, Cor, Estoque)"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(new Color(102, 102, 102));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            mainTable.addCell(headerCell);
        }

        List<Produto> produtos = somenteVisivel ? getProdutosVisiveisNaTabela() : produtoFacade.listarProdutosAtivos();

        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font nestedHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.DARK_GRAY);
        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        for (Produto p : (produtos != null ? produtos : new ArrayList<Produto>())) {
            // Colunas simples com null-safety
            mainTable.addCell(cell(texto(p != null && p.getCategoria() != null ? p.getCategoria().getCategoria() : "-"), contentFont));
            mainTable.addCell(cell(texto(p != null && p.getMarca() != null ? p.getMarca().getMarca() : "-"), contentFont));
            mainTable.addCell(cell(texto(p != null ? p.getNcm() : "-"), contentFont));
            mainTable.addCell(cell(moeda(p != null ? p.getValorUnitarioCompra() : null), contentFont));
            mainTable.addCell(cell(moeda(p != null ? p.getValorUnitarioVenda() : null), contentFont));

            // Coluna de variações (tabela aninhada)
            PdfPCell derivationsCell = new PdfPCell();
            derivationsCell.setPadding(2);

            List<ProdutoDerivacao> variacoes = (p != null ? p.getVariacoes() : null);
            if (variacoes != null && !variacoes.isEmpty()) {
                PdfPTable derivationTable = new PdfPTable(3);
                derivationTable.setWidthPercentage(100);

                derivationTable.addCell(cell("Tamanho/Num.", nestedHeaderFont));
                derivationTable.addCell(cell("Cor", nestedHeaderFont));
                derivationTable.addCell(cell("Qtd. Estoque", nestedHeaderFont, Element.ALIGN_CENTER));

                for (ProdutoDerivacao d : variacoes) {
                    derivationTable.addCell(cell(texto(d != null ? d.getTamanho() : "-"), nestedContentFont));
                    derivationTable.addCell(cell(texto(d != null ? d.getCor() : "-"), nestedContentFont));
                    derivationTable.addCell(cell(texto(d != null && d.getQuantidade() != null ? d.getQuantidade().toString() : "0"),
                            nestedContentFont, Element.ALIGN_CENTER));
                }
                derivationsCell.addElement(derivationTable);
            } else {
                derivationsCell.addElement(new Phrase("Sem variações cadastradas", nestedContentFont));
            }
            mainTable.addCell(derivationsCell);
        }

        document.add(mainTable);
        document.close();
        facesContext.responseComplete();
    }

    private PdfPCell cell(String value, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(value, font));
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell cell(String value, Font font, int hAlign) {
        PdfPCell c = cell(value, font);
        c.setHorizontalAlignment(hAlign);
        return c;
    }

    private List<Produto> getProdutosVisiveisNaTabela() {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("formListagemProdutos:tabelaProdutos");

        List<Produto> listaFiltrada = (List<Produto>) dataTable.getFilteredValue();

        if (listaFiltrada == null) {
            listaFiltrada = (List<Produto>) dataTable.getValue();
        }

        return listaFiltrada;
    }

    //helpers
    private String texto(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private String moeda(Double v) {
        if (v == null) {
            return CURRENCY.format(0);
        }
        return CURRENCY.format(v);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
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

}
