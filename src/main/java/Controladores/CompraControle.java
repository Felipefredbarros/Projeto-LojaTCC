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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
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
    public void exportarPDF(List<Compra> comprasParaExportar) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_compra.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 20, 30); // Margens
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
        Paragraph title = new Paragraph("Loja São Judas Tadeu", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
        Paragraph subtitle = new Paragraph("Relatório de Compras", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, Color.BLACK);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        int vendaCounter = 1;

        for (Compra com : comprasParaExportar) {
            Paragraph compraHeader = new Paragraph("Detalhes da Compra: " + vendaCounter, headerFont);
            compraHeader.setSpacingAfter(10);
            document.add(compraHeader);

            PdfPTable compraTable = new PdfPTable(2);
            compraTable.setWidthPercentage(100);
            compraTable.setSpacingBefore(5);
            compraTable.setSpacingAfter(10);
            compraTable.addCell(new PdfPCell(new Phrase("Data da Compra:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getDataCompra().toString(), contentFont)));
            compraTable.addCell(new PdfPCell(new Phrase("Fornecedor:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getFornecedor().getNome(), contentFont)));
            compraTable.addCell(new PdfPCell(new Phrase("Método de Pagamento:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getMetodoPagamento().toString(), contentFont)));
            compraTable.addCell(new PdfPCell(new Phrase("Parcelas:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getParcelas().toString(), contentFont)));
            compraTable.addCell(new PdfPCell(new Phrase("Valor Total:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getValorTotal().toString(), contentFont)));

            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String textoParcela;
            if (com.getParcelasCompra() != null && !com.getParcelasCompra().isEmpty()) {
                double vParc = com.getParcelasCompra().get(0).getValorParcela(); // assume todas iguais
                int qtd = com.getParcelasCompra().size();
                textoParcela = qtd + "x de " + fmt.format(vParc);
            } else {
                textoParcela = "-";
            }
            compraTable.addCell(new PdfPCell(new Phrase("Valor Parcelas:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(textoParcela, contentFont)));

            document.add(compraTable);

            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2f, 1f, 1f, 1f});
            itemTable.setSpacingBefore(10);

            String[] itemHeaders = {"Produto", "Quantidade", "Valor Unitário", "Subtotal"};
            for (String itemHeader : itemHeaders) {
                PdfPCell itemHeaderCell = new PdfPCell(new Phrase(itemHeader, headerFont));
                itemHeaderCell.setBackgroundColor(new Color(192, 192, 192)); // Cinza claro
                itemHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemHeaderCell.setPadding(5);
                itemTable.addCell(itemHeaderCell);
            }

            for (ItensCompra item : com.getItensCompra()) {
                itemTable.addCell(new PdfPCell(new Phrase(item.getProdutoDerivacao().getTexto(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getQuantidade().toString(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getValorUnitario().toString(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getSubTotal().toString(), contentFont)));
            }

            document.add(itemTable);

            Paragraph separator = new Paragraph(" ", headerFont);
            separator.setSpacingBefore(10);
            separator.setSpacingAfter(20);
            document.add(separator);

            vendaCounter++;
        }

        document.close();
        facesContext.responseComplete();
    }

    public List<MetodoPagamento> getMetodosParcelados() {
        List<MetodoPagamento> metodosParcelados = new ArrayList<>();
        metodosParcelados.add(MetodoPagamento.CARTAO_CREDITO);
        return metodosParcelados;
    }

    public void exportarPDFFiltrado() throws DocumentException, IOException {
        List<Compra> comprasParaExportar = compraFacade.buscarPorFiltros(fornecedorFiltro, produtoFiltro, dataInicio, dataFim);
        exportarPDF(comprasParaExportar);
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
