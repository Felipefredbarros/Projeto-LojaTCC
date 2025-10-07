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
import Entidades.Venda;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class CompraControle implements Serializable {

    private Compra compra = new Compra();
    private ItensCompra itensCompra;
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
    private List<PlanoPagamento> planosPagamentos;
    @ManagedProperty("#{produtoControle}")
    private ProdutoControle produtoControle;
    private boolean mostrarParcelas;
    private Date dataInicio;
    private Date dataFim;
    private Boolean edit = false;
    private List<Compra> listaComprasFiltradas = new ArrayList<>();
    private Compra compraSelecionado;

    public void filtrarPorPeriodo() {
        List<Compra> todasCompras = compraFacade.listaTodosComItens();
        listaComprasFiltradas = new ArrayList<>();

        for (Compra compra : todasCompras) {
            if ((dataInicio == null || !compra.getDataCompra().before(dataInicio))
                    && (dataFim == null || !compra.getDataCompra().after(dataFim))) {
                listaComprasFiltradas.add(compra);
            }
        }
    }

    public void removerFiltro() {
        dataInicio = null;
        dataFim = null;
        listaComprasFiltradas = compraFacade.listaTodosComItens();
    }

    public void prepararVisualizacao(Compra com) {
        this.compraSelecionado = compraFacade.findWithItens(com.getId());
    }

    public CompraControle() {
        this.planosPagamentos = PlanoPagamento.getPlanosPagamento();
        this.compra = new Compra();
    }

    public List<PlanoPagamento> getPlanosPagamentos() {
        return planosPagamentos;
    }

    public void setPlanosPagamentos(List<PlanoPagamento> planosPagamentos) {
        this.planosPagamentos = planosPagamentos;
    }

    public ItensCompra getItensCompra() {
        return itensCompra;
    }

    public void setItensCompra(ItensCompra itensCompra) {
        this.itensCompra = itensCompra;
    }

    public ProdutoControle getProdutoControle() {
        return produtoControle;
    }

    public void setProdutoControle(ProdutoControle produtoControle) {
        this.produtoControle = produtoControle;
    }

    public CompraFacade getCompraFacade() {
        return compraFacade;
    }

    public void setCompraFacade(CompraFacade compraFacade) {
        this.compraFacade = compraFacade;
    }

    public void atualizaPreco() {
        itensCompra.setValorUnitario(itensCompra.getProdutoDerivacao().getProduto().getValorUnitarioCompra());
    }

    public List<Pessoa> getListaFornecedoresFiltrando(String filtro) {
        return pessoaFacade.listaFornecedorFiltrando(filtro, "nome", "cpfcnpj");
    }

    public List<Produto> getListaProdutosFiltrando(String filtro) {
        return produtoFacade.listaFiltrar(filtro, "categoria", "tipo");
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
        if (produtoConverter == null) {
            produtoConverter = new ConverterGenerico(produtoDevFacade);
        }
        return produtoConverter;
    }

    public void setProdutoConverter(ConverterGenerico produtoConverter) {
        this.produtoConverter = produtoConverter;
    }

    public void setFornecedorConverter(ConverterGenerico estadoConverter) {
        this.pessoaConverter = estadoConverter;
    }

    public PessoaFacade getFornecedorFacade() {
        if (pessoaConverter == null) {
            pessoaConverter = new ConverterGenerico(pessoaFacade);
        }
        return pessoaFacade;
    }

    public void setFornecedorFacade(PessoaFacade estadoFacade) {
        this.pessoaFacade = estadoFacade;
    }

    public ProdutoFacade getProdutoFacade() {
        if (produtoConverter == null) {
            produtoConverter = new ConverterGenerico(produtoFacade);
        }
        return produtoFacade;
    }

    public void atualizarPreco() {
        if (itensCompra.getProdutoDerivacao() != null) {
            Produto produto = itensCompra.getProdutoDerivacao().getProduto();
            if (produto != null) {
                itensCompra.setValorUnitario(produto.getValorUnitarioVenda());
            }
        }
    }

    public void adicionarItem() {
        if (produtoControle.getListaProdutos() == null
                || produtoControle.getListaProdutos().isEmpty()
                || itensCompra.getQuantidade() == null || itensCompra.getValorUnitario() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Preencha os Campos para Adicionar um Item"));
            return;
        }

        ItensCompra itemTemp = null;
        for (ItensCompra it : compra.getItensCompra()) {
            if (it.getProdutoDerivacao().getId().equals(itensCompra.getProdutoDerivacao().getId())) {
                itemTemp = it;
            }
        }

        if (itemTemp == null) {
            itensCompra.setCompra(compra);
            compra.getItensCompra().add(itensCompra);
        } else {
            if (!Objects.equals(itensCompra.getValorUnitario(), itemTemp.getValorUnitario())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erro", "O valor do produto precisa ser o mesmo do já cadastrado"));
                return;
            }
            itemTemp.setQuantidade(itemTemp.getQuantidade() + itensCompra.getQuantidade());
        }
        itensCompra = new ItensCompra();

    }

    public void removerItem(ItensCompra item) {
        compra.getItensCompra().remove(item);
        compra.setValorTotal(compra.getValorTotal() - item.getSubTotal());
        itensCompra = new ItensCompra();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Item removido com sucesso!"));
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

    public List<MetodoPagamento> getMetodosParcelados() {
        List<MetodoPagamento> metodosParcelados = new ArrayList<>();
        metodosParcelados.add(MetodoPagamento.CARTAO_CREDITO);
        return metodosParcelados;
    }

    public List<Compra> getListaComprasFiltradas() {
        return listaComprasFiltradas != null && !listaComprasFiltradas.isEmpty()
                ? listaComprasFiltradas
                : compraFacade.listaTodosComItens();
    }

    public void exportarPDF() throws DocumentException, IOException {
        List<Compra> comprasParaExportar = getListaComprasFiltradas();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_compra.pdf");

        Document document = new Document(PageSize.A4, 20, 20, 20, 30); // Margens
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Título do relatório
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 0, 128)); // Azul escuro
        Paragraph title = new Paragraph("Relatório de Compras - Loja São Judas Tadeu", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20); // Espaçamento após o título
        document.add(title);

        // Fonte para os cabeçalhos
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, Color.BLACK);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        int vendaCounter = 1;

        // Para cada venda, criamos uma nova seção
        for (Compra com : comprasParaExportar) {
            // Seção de cabeçalho de cada venda
            Paragraph compraHeader = new Paragraph("Detalhes da Compra: " + vendaCounter, headerFont);
            compraHeader.setSpacingAfter(10);
            document.add(compraHeader);

            // Informações principais da venda
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
            compraTable.addCell(new PdfPCell(new Phrase("Valor Total:", headerFont)));
            compraTable.addCell(new PdfPCell(new Phrase(com.getValorTotal().toString(), contentFont)));

            document.add(compraTable);

            // Tabela de itens da compra
            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2f, 1f, 1f, 1f});
            itemTable.setSpacingBefore(10);

            // Cabeçalhos da tabela de itens
            String[] itemHeaders = {"Produto", "Quantidade", "Valor Unitário", "Subtotal"};
            for (String itemHeader : itemHeaders) {
                PdfPCell itemHeaderCell = new PdfPCell(new Phrase(itemHeader, headerFont));
                itemHeaderCell.setBackgroundColor(new Color(192, 192, 192)); // Cinza claro
                itemHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemHeaderCell.setPadding(5);
                itemTable.addCell(itemHeaderCell);
            }

            // Adiciona os detalhes dos itens da venda
            for (ItensCompra item : com.getItensCompra()) {
                itemTable.addCell(new PdfPCell(new Phrase(item.getProdutoDerivacao().getTexto(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getQuantidade().toString(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getValorUnitario().toString(), contentFont)));
                itemTable.addCell(new PdfPCell(new Phrase(item.getSubTotal().toString(), contentFont)));
            }

            // Adiciona a tabela de itens ao documento
            document.add(itemTable);

            // Linha de separação entre compras
            Paragraph separator = new Paragraph(" ", headerFont);
            separator.setSpacingBefore(10);
            separator.setSpacingAfter(20);
            document.add(separator);

            vendaCounter++;
        }

        document.close();
        facesContext.responseComplete();
    }

    public void novo() {
        edit = false;
        compra = new Compra();
        itensCompra = new ItensCompra();
    }

    public void excluir(Compra ven) {
        compraFacade.remover(ven);
    }

    public void editar(Compra ven) {
        edit = true;
        this.compra = compraFacade.findWithItens(ven.getId());
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
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

}
