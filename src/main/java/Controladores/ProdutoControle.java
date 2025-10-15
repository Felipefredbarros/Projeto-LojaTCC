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
import javax.faces.bean.SessionScoped;
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
import dto.ProdutoRankingDTO;
import java.awt.Color;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class ProdutoControle implements Serializable {

    private Produto produto = new Produto();
    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private CategoriaFacade categoriaFacade;
    private ConverterGenerico categoriaConverter;
    @ManagedProperty("#{categoriaControle}")
    private CategoriaControle categoriaControle;
    @EJB
    private MarcaFacade marcaFacade;
    private ConverterGenerico marcaConverter;
    @ManagedProperty("#{marcaControle}")
    private MarcaControle marcaControle;
    private ProdutoDerivacao prodDev = new ProdutoDerivacao();
    @EJB
    private ProdutoDerivacaoFacade produtoDevFacade;
    private Produto produtoSelecionado;


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
        produto.getVariacoes().remove(dev);
        prodDev = new ProdutoDerivacao();
        limpaCampos();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Derivação removido com sucesso!"));
    }

    public void limpaCampos() {
        prodDev.setCor("");
        prodDev.setQuantidade(null);
        prodDev.setTamanho("");
    }

    public void prepararVisualizacao(Produto prod) {
        if (prod == null || prod.getId() == null) {
            this.produtoSelecionado = null;
            System.err.println("prepararVisualizacao - Pessoa ou ID nulo. pessoaSelecionado definido como null.");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível selecionar a pessoa para visualização."));
            return;
        }

        try {
            this.produtoSelecionado = produtoFacade.findWithDerivacoes(prod.getId());
            if (this.produtoSelecionado != null) {
                System.out.println("prepararVisualizacao - Produto encontrada: " + this.produtoSelecionado.getTexto());
            } else {
                System.out.println("prepararVisualizacao - Nenhuma pessoa encontrada com o ID: " + prod.getId());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada."));
            }
        } catch (javax.persistence.NoResultException nre) {
            this.produtoSelecionado = null; // Garante que fique nulo se não encontrar
            System.err.println("prepararVisualizacao - NoResultException ao buscar pessoa com ID " + prod.getId() + ": " + nre.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada com o ID fornecido."));
        } catch (Exception e) {
            this.produtoSelecionado = null; // Garante que fique nulo em caso de outros erros
            System.err.println("prepararVisualizacao - Erro ao buscar Pessoa: " + e.getMessage());
            e.printStackTrace(); // Importante para ver a causa raiz do erro no console do servidor
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Ocorreu um erro inesperado ao carregar os detalhes da pessoa."));
        }
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
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso", "Produto salvo com sucesso!"));

        try {

            FacesContext.getCurrentInstance().getExternalContext().redirect("listaProduto.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        produto = new Produto();
        prodDev = new ProdutoDerivacao();
    }

    public void novo() {
        produto = new Produto();
    }

    public void excluir(Produto prod) {
        if ((produtoFacade.produtoTemVendas(prod.getId()) || produtoFacade.produtoTemCompras(prod.getId())) && prod.getAtivo() == true) {
            // Se o produto estiver associado, o marcar como inativo
            prod.setAtivo(false); // Marcar o produto como inativo
            produtoFacade.salvar(prod); // Salva as alterações no banco de dados

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Produto inativado", "O produto foi inativado com sucesso e não pode mais ser usado nas vendas/compras."));
            return; // Sai do método após inativar o produto
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

        // Se não estiver associado, remove o produto da base de dados
        produtoFacade.remover(prod);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso", "Produto excluído com sucesso!"));
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

        this.produto = produtoFacade.findWithDerivacoes(prod.getId());
        prodDev.setProduto(produto);

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("produtoCadastro.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public List<Produto> getListaProdutos() {
        return produtoFacade.listarProdutosAtivos();
    }

    public List<Produto> getListaProdutosInativos() {
        return produtoFacade.listarProdutosInativos();
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

    public void exportarPDF(boolean somenteVisivel) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        // Configure the HTTP response for PDF download
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_produtos.pdf");

        // Initialize the PDF document with landscape orientation for more space
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

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

        // --- Main Product Table ---
        // 6 columns: Category, Brand, NCM, Purchase Price, Sale Price, and a wide column for Derivations
        PdfPTable mainTable = new PdfPTable(6);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.5f, 1.5f, 1f, 1f, 1f, 3f});

        // --- Main Table Headers ---
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

        // --- Get Product List ---
        // This logic correctly fetches either the filtered list from the UI or all active products.
        // IMPORTANT: The `produtoFacade.listarProdutosAtivos()` method MUST use a JOIN FETCH
        // to eagerly load the `listaDerivacoes` to prevent LazyInitializationException.
        List<Produto> produtos = somenteVisivel ? getProdutosVisiveisNaTabela() : produtoFacade.listarProdutosAtivos();

        // --- Table Body ---
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font nestedHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.DARK_GRAY);
        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        for (Produto prod : produtos) {
            // Add general product info to the main table cells
            mainTable.addCell(new PdfPCell(new Phrase(prod.getCategoria().getCategoria(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(prod.getMarca().getMarca(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(prod.getNcm(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(String.format("R$ %.2f", prod.getValorUnitarioCompra()), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(String.format("R$ %.2f", prod.getValorUnitarioVenda()), contentFont)));

            // --- Create a nested table for derivations and add it to a single cell ---
            PdfPCell derivationsCell = new PdfPCell();
            derivationsCell.setPadding(2);

            // This check is safe ONLY IF you eagerly fetched the list
            if (prod.getVariacoes() != null && !prod.getVariacoes().isEmpty()) {
                PdfPTable derivationTable = new PdfPTable(3); // 3 columns for derivation details
                derivationTable.setWidthPercentage(100);

                // Headers for the nested derivation table
                derivationTable.addCell(new PdfPCell(new Phrase("Tamanho/Num.", nestedHeaderFont)));
                derivationTable.addCell(new PdfPCell(new Phrase("Cor", nestedHeaderFont)));
                derivationTable.addCell(new PdfPCell(new Phrase("Qtd. Estoque", nestedHeaderFont)));

                for (ProdutoDerivacao deriv : prod.getVariacoes()) {
                    derivationTable.addCell(new PdfPCell(new Phrase(deriv.getTamanho(), nestedContentFont)));
                    derivationTable.addCell(new PdfPCell(new Phrase(deriv.getCor(), nestedContentFont)));

                    PdfPCell qtyCell = new PdfPCell(new Phrase(deriv.getQuantidade().toString(), nestedContentFont));
                    qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    derivationTable.addCell(qtyCell);
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

    private List<Produto> getProdutosVisiveisNaTabela() {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("formListagemProdutos:tabelaProdutos");

        List<Produto> listaFiltrada = (List<Produto>) dataTable.getFilteredValue();

        if (listaFiltrada == null) {
            listaFiltrada = (List<Produto>) dataTable.getValue();
        }

        return listaFiltrada;
    }

}
