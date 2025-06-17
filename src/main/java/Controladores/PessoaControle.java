/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import Converters.ConverterGenerico;
import Entidades.Cidade;
import Entidades.ContratoTrabalho;
import Entidades.Endereco;
import Entidades.Enums.TipoPessoa;
import javax.faces.application.FacesMessage;
import Entidades.Pessoa;
import Entidades.Telefone;
import Entidades.Enums.tipoTelefone;
import Facade.PessoaFacade;
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
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class PessoaControle implements Serializable {

    private Pessoa pessoa = new Pessoa();
    @EJB
    private PessoaFacade pessoaFacade;
    private ConverterGenerico pessoaConverter;
    private Endereco novoEndereco = new Endereco();
    private Telefone novoTelefone = new Telefone();
    private ContratoTrabalho contrato = new ContratoTrabalho();

    private List<Cidade> cidadesDisponiveis;
    private Boolean edit = false;
    private Pessoa pessoaSelecionado;

    //novo
    public void limparFormulario() {
        pessoa = new Pessoa(); // Pessoa constructor now defaults tipoPessoa to "FISICA"
        novoEndereco = new Endereco();
        novoTelefone = new Telefone();
    }

    public void prepararVisualizacao(Pessoa pes) {
        // Adicione logs para depuração
        if (pes == null || pes.getId() == null) {
            this.pessoaSelecionado = null;
            System.err.println("prepararVisualizacao - Pessoa ou ID nulo. pessoaSelecionado definido como null.");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível selecionar a pessoa para visualização."));
            return;
        }

        try {
            this.pessoaSelecionado = pessoaFacade.findWithAll(pes.getId());
            if (this.pessoaSelecionado != null) {
                System.out.println("prepararVisualizacao - Pessoa encontrada: " + this.pessoaSelecionado.getNome());
                System.out.println("prepararVisualizacao - Qtd Endereços: " + (this.pessoaSelecionado.getListaEnderecos() != null ? this.pessoaSelecionado.getListaEnderecos().size() : "null ou 0"));
                System.out.println("prepararVisualizacao - Qtd Telefones: " + (this.pessoaSelecionado.getListaTelefones() != null ? this.pessoaSelecionado.getListaTelefones().size() : "null ou 0"));
            } else {
                System.out.println("prepararVisualizacao - Nenhuma pessoa encontrada com o ID: " + pes.getId());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada."));
            }
        } catch (javax.persistence.NoResultException nre) {
            this.pessoaSelecionado = null; // Garante que fique nulo se não encontrar
            System.err.println("prepararVisualizacao - NoResultException ao buscar pessoa com ID " + pes.getId() + ": " + nre.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada com o ID fornecido."));
        } catch (Exception e) {
            this.pessoaSelecionado = null; // Garante que fique nulo em caso de outros erros
            System.err.println("prepararVisualizacao - Erro ao buscar Pessoa: " + e.getMessage());
            e.printStackTrace(); // Importante para ver a causa raiz do erro no console do servidor
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Ocorreu um erro inesperado ao carregar os detalhes da pessoa."));
        }
    }

    //novos
    public void onTipoPessoaChange() {
        //pessoa.setSalario(null);
        //pessoa.setCargo(null);
        //pessoa.setDiaPagamentos(null);
        pessoa.getContrato().setSalario(null);
        pessoa.getContrato().setCargo(null);
        pessoa.getContrato().setDiaPagamentos(null);
        pessoa.setRegiao(null);

        // Default the nature of the person to FISICA when the main type changes.
        // The Pessoa entity's tipoPessoa field stores "FISICA" or "JURIDICA".
        pessoa.setTipoPessoa("FISICA");

        pessoa.setCpfcnpj(null);

        System.out.println("Tipo de Pessoa (Enum Role) alterado para: " + pessoa.getTipo());
        System.out.println("Natureza da Pessoa (String FISICA/JURIDICA) definida para: " + pessoa.getTipoPessoa());
    }

    //novo
    public void onNaturezaPessoaChange() {
        pessoa.setCpfcnpj(null); // Clear CPF/CNPJ to reapply mask and avoid validation errors
        System.out.println("Natureza da Pessoa (FISICA/JURIDICA) alterada para: " + pessoa.getTipoPessoa());
        // The <p:ajax update="xxx"/> in JSF will handle UI updates.
    }

    public void onTipoTelefonePessoaChange() {
        novoTelefone.setNumero(null);
        System.out.println("Telefone da Pessoa (FISICA/JURIDICA) alterada para: " + novoTelefone.getTipoTelefone());
    }

    public String getMascaraTelefone() {
        if (novoTelefone.getTipoTelefone() == null) {
            return "(99) 99999-9999"; // padrão
        }

        if (novoTelefone.getTipoTelefone() == tipoTelefone.CELULAR) {
            return "(99) 99999-9999";
        }

        return "(99) 9999-9999";
    }

    //novo
    public void adicionarEndereco() {
        if (novoEndereco.getRua() == null || novoEndereco.getRua().trim().isEmpty()
                || novoEndereco.getNumero() == null || novoEndereco.getNumero().trim().isEmpty()
                || novoEndereco.getBairro() == null || novoEndereco.getBairro().trim().isEmpty()
                || novoEndereco.getCep() == null || novoEndereco.getCep().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Preencha todos os campos obrigatórios do endereço."));
            return;
        }
        novoEndereco.setPessoa(pessoa);
        pessoa.getListaEnderecos().add(novoEndereco);
        novoEndereco = new Endereco();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Endereço adicionado."));
    }

    //novo
    public void removerEndereco(Endereco enderecoParaRemover) {
        this.pessoa.getListaEnderecos().remove(enderecoParaRemover);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Endereço removido."));
    }

    //novo
    public void adicionarTelefone() {
        if (novoTelefone.getNumero() == null || novoTelefone.getNumero().trim().isEmpty()
                || novoTelefone.getTipoTelefone() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Número e Tipo são obrigatórios para o telefone."));
            return;
        }
        novoTelefone.setPessoa(this.pessoa);
        this.pessoa.getListaTelefones().add(novoTelefone);
        novoTelefone = new Telefone();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Telefone adicionado."));
    }

    public void removerTelefone(Telefone telefoneParaRemover) {
        this.pessoa.getListaTelefones().remove(telefoneParaRemover);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Telefone removido."));
        novoTelefone = new Telefone();
    }

    //novo
    public String salvarPessoa() {
        try {
            if (pessoa.getTipo() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "O tipo da pessoa é obrigatório."));
                return null;
            }
            if (pessoa.getTipoPessoa() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A natureza da pessoa (Física/Jurídica) é obrigatória."));
                return null;
            }

            pessoa.prepararParaSalvar();

            if (pessoa.getTipo() != TipoPessoa.FUNCIONARIO) {
                //pessoa.setSalario(null);
                //pessoa.setCargo(null);
                //pessoa.setDiaPagamentos(null);
                pessoa.getContrato().setSalario(null);
                pessoa.getContrato().setCargo(null);
                pessoa.getContrato().setDiaPagamentos(null);
            } else {
                // garante vínculo contrato ↔ pessoa
                if (pessoa.getContrato() != null) {
                    pessoa.getContrato().setFuncionario(pessoa);
                }
            }
            if (pessoa.getTipo() != TipoPessoa.FORNECEDOR) {
                pessoa.setRegiao(null);
            }
            pessoaFacade.salvar(pessoa);
            pessoa = new Pessoa();
            novoEndereco = new Endereco();
            novoTelefone = new Telefone();
            contrato = new ContratoTrabalho();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa salva com sucesso! (Simulado)"));
            limparFormulario();
            return "listaPessoas.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao Salvar", e.getMessage()));
            e.printStackTrace();
            return null;
        }
    }

    public List<TipoPessoa> getTiposPessoaDisponiveis() {
        return Arrays.asList(TipoPessoa.values());
    }

    public List<tipoTelefone> getTiposTelefoneDisponiveis() {
        return Arrays.asList(tipoTelefone.values());
    }

    public PessoaFacade getPessoaFacade() {
        return pessoaFacade;
    }

    public void setPessoaFacade(PessoaFacade pessoaFacade) {
        this.pessoaFacade = pessoaFacade;
    }

    public ConverterGenerico getPessoaConverter() {
        return pessoaConverter;
    }

    public void setPessoaConverter(ConverterGenerico pessoaConverter) {
        this.pessoaConverter = pessoaConverter;
    }

    public Pessoa getPessoaSelecionado() {
        return pessoaSelecionado;
    }

    public void setPessoaSelecionado(Pessoa pessoaSelecionado) {
        this.pessoaSelecionado = pessoaSelecionado;
    }

    public void salvarCliente() {
        pessoa.setTipo(TipoPessoa.CLIENTE);
        pessoa.setTipoPessoa("FISICA");
        pessoa.limparDadosPessoais();
        pessoaFacade.salvar(pessoa);
        pessoa = new Pessoa();
    }

    public void salvarFunc() {
        pessoa.setTipoPessoa("FISICA");
        pessoa.setTipo(TipoPessoa.FUNCIONARIO);
        pessoa.limparDadosPessoais();
        pessoaFacade.salvar(pessoa);
        pessoa = new Pessoa();
    }

    public void salvarForn() {
        pessoa.setTipo(TipoPessoa.FORNECEDOR);
        pessoa.limparDadosPessoais();
        pessoaFacade.salvar(pessoa);
        pessoa = new Pessoa();
    }

    public void novo() {
        edit = false;
        pessoa = new Pessoa();
    }

    public void excluir(Pessoa pessoa) {
        boolean temVinculos = pessoaFacade.pessoaTemVinculos(pessoa);

        if (temVinculos && pessoa.getAtivo()) {
            pessoa.setAtivo(false);
            pessoaFacade.salvar(pessoa);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Pessoa inativada",
                            "A pessoa foi inativada com sucesso e não pode mais ser usada no Sistema."));
            return;
        }

        if (!pessoa.getAtivo()) {
            if (temVinculos) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Erro ao Excluir",
                                "Pessoa inativada, mas há vendas/movimentaçoes relacionadas a ela, exclua os vinculos antes de tentar excluir definitivamente"));
                return;
            } else {
                pessoaFacade.remover(pessoa);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Sucesso",
                                "Pessoa inativada excluída com sucesso!"));
                return;
            }
        }

        pessoaFacade.remover(pessoa);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso",
                        "Pessoa excluída com sucesso!"));
    }

    public void editar(Pessoa pes) {
        edit = true;
        this.pessoa = pessoaFacade.findWithAll(pes.getId());

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("pessoaCadastro.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Endereco getNovoEndereco() {
        return novoEndereco;
    }

    public void setNovoEndereco(Endereco novoEndereco) {
        this.novoEndereco = novoEndereco;
    }

    public Telefone getNovoTelefone() {
        return novoTelefone;
    }

    public void setNovoTelefone(Telefone novoTelefone) {
        this.novoTelefone = novoTelefone;
    }

    public List<Cidade> getCidadesDisponiveis() {
        return cidadesDisponiveis;
    }

    public void setCidadesDisponiveis(List<Cidade> cidadesDisponiveis) {
        this.cidadesDisponiveis = cidadesDisponiveis;
    }

    public List<Pessoa> getListaClientes() {
        return pessoaFacade.listaClienteAtivo();
    }

    public List<Pessoa> getListaFornecedor() {
        return pessoaFacade.listaFornecedorAtivo();
    }

    public List<Pessoa> getListaFuncionario() {
        return pessoaFacade.listaFuncionarioAtivo();
    }

    public List<Pessoa> getListaClientesInativos() {
        return pessoaFacade.listaClienteInativo();
    }

    public List<Pessoa> getListaFornecedorInativos() {
        return pessoaFacade.listaFornecedorInativo();
    }

    public List<Pessoa> getListaFuncionarioInativos() {
        return pessoaFacade.listaFuncionarioInativo();
    }

    public List<Pessoa> getListaPessoaAtiva() {
        return pessoaFacade.listaPessoaAtivo();
    }

    public List<Pessoa> getListaPessoaInativa() {
        return pessoaFacade.listaPessoaInativo();
    }

    public ContratoTrabalho getContrato() {
        return contrato;
    }

    public void setContrato(ContratoTrabalho contrato) {
        this.contrato = contrato;
    }

    public void exportarRelatorioClientes() throws DocumentException, IOException {
        exportarPDF(pessoaFacade.listaCliAtivo(), "relatorio_clientes.pdf");
    }

    public void exportarRelatorioFuncionarios() throws DocumentException, IOException {
        exportarPDF(pessoaFacade.listaFuncAtivo(), "relatorio_funcionarios.pdf");
    }

    public void exportarRelatorioFornecedores() throws DocumentException, IOException {
        exportarPDF(pessoaFacade.listaFornAtivo(), "relatorio_fornecedores.pdf");
    }

    private void exportarPDF(List<Pessoa> pessoas, String reportTitle) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        String fileName = reportTitle.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, new Color(0, 51, 102));
        Paragraph title = new Paragraph(reportTitle + " - Loja São Judas Tadeu", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable mainTable = new PdfPTable(8);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1f, 1f, 2.5f, 3f, 1.5f, 1f, 4f, 2f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
        String[] headers = {"Tipo Pessoa", "Natureza", "Nome", "Email", "CPF/CNPJ", "Status", "Endereços", "Telefones"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(new Color(102, 102, 102));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            headerCell.setBorderColor(new Color(200, 200, 200));
            mainTable.addCell(headerCell);
        }

        // --- Table Body ---
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font nestedHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.DARK_GRAY);
        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        for (Pessoa p : pessoas) {
            // Add basic person data directly to the main table
            mainTable.addCell(new PdfPCell(new Phrase(p.getTipo().getLabel(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(p.getTipoPessoa(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(p.getNome(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(p.getEmail(), contentFont)));
            mainTable.addCell(new PdfPCell(new Phrase(p.getCpfcnpjFormatado(), contentFont)));

            // Add status with centered alignment
            PdfPCell statusCell = new PdfPCell(new Phrase(p.getStatus(), contentFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            mainTable.addCell(statusCell);

            // --- Create a nested table for addresses and add it to a single cell ---
            PdfPCell addressesCell = new PdfPCell();
            addressesCell.setPadding(2);

            // This check is safe because of the JOIN FETCH in your facade
            if (p.getListaEnderecos() != null && !p.getListaEnderecos().isEmpty()) {
                PdfPTable addressTable = new PdfPTable(4);
                addressTable.setWidthPercentage(100);
                addressTable.setWidths(new float[]{4.5f, 1.8f, 2.5f, 3.2f});

                // Headers for the nested address table
                addressTable.addCell(new PdfPCell(new Phrase("Rua", nestedHeaderFont)));
                addressTable.addCell(new PdfPCell(new Phrase("Nº", nestedHeaderFont)));
                addressTable.addCell(new PdfPCell(new Phrase("Bairro", nestedHeaderFont)));
                addressTable.addCell(new PdfPCell(new Phrase("CEP", nestedHeaderFont)));

                // Add address rows
                for (Endereco end : p.getListaEnderecos()) {
                    addressTable.addCell(new PdfPCell(new Phrase(end.getRua(), nestedContentFont)));
                    addressTable.addCell(new PdfPCell(new Phrase(end.getNumero(), nestedContentFont)));
                    addressTable.addCell(new PdfPCell(new Phrase(end.getBairro(), nestedContentFont)));
                    addressTable.addCell(new PdfPCell(new Phrase(end.getCep(), nestedContentFont)));
                }
                addressesCell.addElement(addressTable);
            } else {
                addressesCell.addElement(new Phrase("Nenhum endereço cadastrado", nestedContentFont));
            }
            mainTable.addCell(addressesCell);

            // --- Create another nested table for phones ---
            PdfPCell phonesCell = new PdfPCell();
            phonesCell.setPadding(2);
            if (p.getListaTelefones() != null && !p.getListaTelefones().isEmpty()) {
                PdfPTable phoneTable = new PdfPTable(2);
                phoneTable.setWidthPercentage(100);

                phoneTable.addCell(new PdfPCell(new Phrase("Tipo", nestedHeaderFont)));
                phoneTable.addCell(new PdfPCell(new Phrase("Número", nestedHeaderFont)));

                for (Telefone tel : p.getListaTelefones()) {
                    phoneTable.addCell(new PdfPCell(new Phrase(tel.getTipoTelefone().toString(), nestedContentFont)));
                    phoneTable.addCell(new PdfPCell(new Phrase(tel.getNumero(), nestedContentFont)));
                }
                phonesCell.addElement(phoneTable);
            } else {
                phonesCell.addElement(new Phrase("Nenhum telefone cadastrado", nestedContentFont));
            }
            mainTable.addCell(phonesCell);
        }

        document.add(mainTable);
        document.close();
        facesContext.responseComplete();
    }
}
