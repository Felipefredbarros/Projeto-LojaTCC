/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Converters.ConverterGenerico;
import Entidades.Enums.TipoBonus;
import Entidades.FolhaPagamento;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Pessoa;
import Facade.FolhaFacade;
import Facade.MovimentacaoMensalFacade;
import Facade.PessoaFacade;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class FolhaControle implements Serializable {

    private FolhaPagamento folha = new FolhaPagamento();
    @EJB
    private FolhaFacade folhaFacade;
    @EJB
    private MovimentacaoMensalFacade movimentacaoMensalFacade;
    @EJB
    private PessoaFacade pessoaFacade;
    private ConverterGenerico pessoaConverter;

    public void gerarFolha() {
        try {
            folha.setDataGeracao(new Date());
            Pessoa funcionario = folha.getFuncionario();
            Date competencia = folha.getCompetencia();

            if (funcionario == null || competencia == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione o funcionário e a competência."));
                return;
            }
            double salarioBase = funcionario.getContrato().getSalario();
            List<MovimentacaoMensalFuncionario> movimentacoes = movimentacaoMensalFacade.buscarPorFuncionarioECompetencia(funcionario, competencia);

            double totalHorasExtras = movimentacoes.stream()
                    .filter(m -> m.getTipoBonus() == TipoBonus.HORA_EXTRA)
                    .mapToDouble(m -> m.getBonus() != null ? m.getBonus() : 0.0)
                    .sum();

            double totalComissoes = movimentacoes.stream()
                    .filter(m -> m.getTipoBonus() == TipoBonus.COMISSAO)
                    .mapToDouble(m -> m.getBonus() != null ? m.getBonus() : 0.0)
                    .sum();
            System.out.println("\ntotalHorasExtras: \n" + totalHorasExtras);
            System.out.println("totalComissoes: " + totalComissoes);

            folha.setSalarioBase(salarioBase);
            folha.setAdicional(totalHorasExtras);
            folha.setComissao(totalComissoes);

            double salarioBruto = salarioBase + totalHorasExtras + totalComissoes;
            double inss = folha.calcularINSS(salarioBruto);
            double irrf = folha.calcularIRRF(salarioBruto, inss);
            double fgts = folha.calcularFGTS(salarioBruto);
            double salarioLiquido = salarioBruto - inss - irrf;

            folha.setInss(inss);
            folha.setIrrf(irrf);
            folha.setFgts(fgts);
            folha.setSalarioLiquido(salarioLiquido);

            folhaFacade.salvar(folha);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Folha gerada e salva com sucesso!"));

            folha = new FolhaPagamento();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao gerar a folha."));
        }
    }

    public String exportarPDFAction(FolhaPagamento folha) {
        try {
            exportarPDF(folha); // chama seu método atual
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao gerar PDF: " + e.getMessage()));
        }
        return null; // necessário para não redirecionar
    }

    public void exportarPDF(FolhaPagamento folha) throws IOException, DocumentException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        String nomeArquivo = "folha_pagamento_"
                + folha.getFuncionario().getNome().toLowerCase().replace(" ", "_") + "_"
                + new SimpleDateFormat("MM_yyyy").format(folha.getCompetencia()) + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + nomeArquivo);

        Document document = new Document(PageSize.A4, 40, 40, 50, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fieldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfMesAno = new SimpleDateFormat("MM/yyyy");

        // Título centralizado
        Paragraph titulo = new Paragraph("FOLHA DE PAGAMENTO", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(15);
        document.add(titulo);

        // Informações Empresa + Recibo
        PdfPTable empresaTable = new PdfPTable(2);
        empresaTable.setWidthPercentage(100);
        empresaTable.setWidths(new float[]{4f, 2.5f});

        PdfPCell empresaCell = new PdfPCell();
        empresaCell.setBorder(Rectangle.BOX);
        empresaCell.setPadding(5);
        empresaCell.addElement(new Phrase("Loja São Judas Tadeu LTDA", fieldFont));
        empresaCell.addElement(new Phrase("Endereço: Rua Barão do Rio Branco 713, Terra Rica - Paraná (PR), CEP: 87890-000", valueFont));
        empresaCell.addElement(new Phrase("CNPJ: 29.832.971/0006-94", valueFont));

        PdfPCell reciboCell = new PdfPCell();
        reciboCell.setBorder(Rectangle.BOX);
        reciboCell.setPadding(5);
        reciboCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        reciboCell.addElement(new Phrase("Recibo de Pagamento e Salário", headerFont));
        reciboCell.addElement(new Phrase("Competência: " + sdfMesAno.format(folha.getCompetencia()), valueFont));

        empresaTable.addCell(empresaCell);
        empresaTable.addCell(reciboCell);

        document.add(empresaTable);
        document.add(new Paragraph(" "));

        // Dados Funcionário
        PdfPTable dadosFunc = new PdfPTable(2);
        dadosFunc.setWidthPercentage(100);
        dadosFunc.setWidths(new float[]{1.5f, 3.5f});

        dadosFunc.addCell(new Phrase("Funcionário:", fieldFont));
        dadosFunc.addCell(new Phrase(folha.getFuncionario().getNome(), valueFont));
        dadosFunc.addCell(new Phrase("CPF/CNPJ:", fieldFont));
        dadosFunc.addCell(new Phrase(folha.getFuncionario().getCpfcnpjFormatado(), valueFont));
        dadosFunc.addCell(new Phrase("Data de Geração:", fieldFont));
        dadosFunc.addCell(new Phrase(sdf.format(folha.getDataGeracao()), valueFont));

        document.add(dadosFunc);
        document.add(new Paragraph(" "));

        // Tabela de Valores
        PdfPTable valores = new PdfPTable(3);
        valores.setWidthPercentage(100);
        valores.setWidths(new float[]{4.5f, 2f, 2f});
        valores.addCell(new Phrase("Descrição", fieldFont));
        valores.addCell(new Phrase("Proventos", fieldFont));
        valores.addCell(new Phrase("Descontos", fieldFont));

        // Linhas
        valores.addCell(new Phrase("Salário Base", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getSalarioBase()), valueFont));
        valores.addCell(new Phrase("-", valueFont));

        valores.addCell(new Phrase("Adicional (Horas Extras)", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getAdicional()), valueFont));
        valores.addCell(new Phrase("-", valueFont));

        valores.addCell(new Phrase("Comissões", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getComissao()), valueFont));
        valores.addCell(new Phrase("-", valueFont));

        valores.addCell(new Phrase("INSS", valueFont));
        valores.addCell(new Phrase("-", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getInss()), valueFont));

        valores.addCell(new Phrase("IRRF", valueFont));
        valores.addCell(new Phrase("-", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getIrrf()), valueFont));

        valores.addCell(new Phrase("FGTS", valueFont));
        valores.addCell(new Phrase(String.format("R$ %.2f", folha.getFgts()), valueFont));
        valores.addCell(new Phrase("-", valueFont));

        // Salário líquido em destaque
        PdfPCell liquidoLabel = new PdfPCell(new Phrase("Salário Líquido", fieldFont));
        liquidoLabel.setColspan(2);
        PdfPCell liquidoValor = new PdfPCell(new Phrase(String.format("R$ %.2f", folha.getSalarioLiquido()), valueFont));
        valores.addCell(liquidoLabel);
        valores.addCell(liquidoValor);

        document.add(valores);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Linha para assinatura
        Paragraph assinatura = new Paragraph("Assinatura do Funcionário: _______________________________________", valueFont);
        assinatura.setSpacingBefore(30);
        document.add(assinatura);

        document.close();
        facesContext.responseComplete();
    }

    public void novo() {
        folha = new FolhaPagamento();
    }

    public void excluir(FolhaPagamento fol) {
        folhaFacade.remover(fol);
    }

    public List<FolhaPagamento> getlistaFolhas() {
        return folhaFacade.listaTodos();
    }

    public FolhaPagamento getFolha() {
        return folha;
    }

    public void setFolha(FolhaPagamento folha) {
        this.folha = folha;
    }

    public FolhaFacade getFolhaFacade() {
        return folhaFacade;
    }

    public void setFolhaFacade(FolhaFacade folhaFacade) {
        this.folhaFacade = folhaFacade;
    }

    public ConverterGenerico getPessoaConverter() {
        if (pessoaConverter == null) {
            pessoaConverter = new ConverterGenerico(pessoaFacade);
        }
        return pessoaConverter;
    }

    public List<Pessoa> getListaFuncionariosFiltrando(String filtro) {
        return pessoaFacade.listaFuncionarioFiltrando(filtro, "nome", "cpfcnpj");
    }

}
