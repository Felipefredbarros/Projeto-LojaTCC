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
import Entidades.Estado;
import Facade.CidadeFacade;
import Facade.EstadoFacade;
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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author felip
 */
@Named("pessoaControle")
@ViewScoped
public class PessoaControle implements Serializable {

    private Pessoa pessoa = new Pessoa();
    @EJB
    private PessoaFacade pessoaFacade;
    @EJB
    private CidadeFacade cidadeFacade;
    private ConverterGenerico pessoaConverter;
    private Endereco novoEndereco = new Endereco();
    private Telefone novoTelefone = new Telefone();
    private ContratoTrabalho contrato = new ContratoTrabalho();
    private Boolean edit = false;
    private Pessoa pessoaSelecionado;
    private Long estadoSelecionadoId;
    private Long cidadeSelecionadaId;
    private List<Estado> listaEstados;
    private List<Cidade> listaCidades;
    @EJB
    private EstadoFacade estadoFacade;

    @PostConstruct
    public void init() {

        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        pessoa = new Pessoa();
        edit = false;
        novoEndereco = new Endereco();
        listaCidades = new ArrayList<>();
        carregarEstados();

        Object id = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getFlash()
                .get("pessoaId");
        if (id != null) {
            Long pid = (id instanceof Long) ? (Long) id : Long.valueOf(id.toString());
            this.pessoa = pessoaFacade.findWithAll(pid);
        }
        if (pessoa.getContrato() == null) {
            pessoa.setContrato(new ContratoTrabalho());
        }
    }

    public void limparFormulario() {
        pessoa = new Pessoa();
        novoEndereco = new Endereco();
        novoTelefone = new Telefone();
    }

    private void carregarEstados() {
        listaEstados = estadoFacade.listaTodos();
    }

    public void onEstadoSelect() {
        if (estadoSelecionadoId != null) {
            listaCidades = cidadeFacade.buscarPorEstadoId(estadoSelecionadoId); // Você precisará criar este método no seu CidadeFacade

        } else {
            listaCidades = new ArrayList<>();
        }
    }

    public void prepararVisualizacao(Pessoa pes) {
        if (pes == null || pes.getId() == null) {
            this.pessoaSelecionado = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível selecionar a pessoa para visualização."));
            return;
        }

        try {
            this.pessoaSelecionado = pessoaFacade.findWithAll(pes.getId());
        } catch (javax.persistence.NoResultException nre) {
            this.pessoaSelecionado = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pessoa não encontrada com o ID fornecido."));
        } catch (Exception e) {
            this.pessoaSelecionado = null;
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Ocorreu um erro inesperado ao carregar os detalhes da pessoa."));
        }
    }

    public void onTipoPessoaChange() {
        if (pessoa.getContrato() == null) {
            pessoa.setContrato(new ContratoTrabalho());
        }
        pessoa.getContrato().setSalario(null);
        pessoa.getContrato().setCargo(null);
        pessoa.getContrato().setDiaPagamentos(null);

        pessoa.setRegiao(null);
        pessoa.setTipoPessoa("FISICA");
    }

    public void onNaturezaPessoaChange() {
        pessoa.setCpfcnpj(null);
    }

    public void onTipoTelefonePessoaChange() {
        novoTelefone.setNumero(null);
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
        if (estadoSelecionadoId == null || cidadeSelecionadaId == null
                || novoEndereco.getRua() == null || novoEndereco.getRua().trim().isEmpty()
                || novoEndereco.getNumero() == null || novoEndereco.getNumero().trim().isEmpty()
                || novoEndereco.getBairro() == null || novoEndereco.getBairro().trim().isEmpty()
                || novoEndereco.getCep() == null || novoEndereco.getCep().trim().isEmpty()) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Preencha todos os campos do endereço (Estado, Cidade, Rua, etc.)."));
            return;
        }

        try {
            // achar o estado
            Estado estadoSelecionado = listaEstados.stream()
                    .filter(e -> e.getId().equals(estadoSelecionadoId))
                    .findFirst()
                    .orElse(null);

            // achar o cidade
            Cidade cidadeSelecionada = listaCidades.stream()
                    .filter(c -> c.getId().equals(cidadeSelecionadaId))
                    .findFirst()
                    .orElse(null);

            // se achou o objeto
            if (cidadeSelecionada != null && estadoSelecionado != null) {
                // associa o estado à cidade 
                cidadeSelecionada.setEstado(estadoSelecionado);

                // associa a cidade ao endereço 
                novoEndereco.setCidade(cidadeSelecionada);

                novoEndereco.setPessoa(pessoa);
                pessoa.getListaEnderecos().add(novoEndereco);

                novoEndereco = new Endereco();
                estadoSelecionadoId = null;
                cidadeSelecionadaId = null;
                listaCidades = new ArrayList<>();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Endereço adicionado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Estado ou Cidade selecionada não foi encontrada. Tente novamente."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Erro Crítico", "Ocorreu um erro inesperado ao adicionar o endereço."));
        }
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
                pessoa.getContrato().setSalario(null);
                pessoa.getContrato().setCargo(null);
                pessoa.getContrato().setDiaPagamentos(null);
                pessoa.getContrato().setDataInicio(null);
                pessoa.getContrato().setJornadaDiariaHoras(null);
            } else {
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
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa salva com sucesso!"));
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("pessoaId");

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

    public String novo() {
        edit = false;
        pessoa = new Pessoa();
        novoEndereco = new Endereco();
        novoTelefone = new Telefone();
        contrato = new ContratoTrabalho();
        pessoa.setContrato(new ContratoTrabalho());

        FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("pessoaId");
        return "pessoaCadastro.xhtml?faces-redirect=true";
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
    
    public void ativar(Pessoa pes){
        pes.setAtivo(true);
        pessoaFacade.salvar(pes);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Sucesso",
                        "Pessoa ativado com sucesso!"));
    }

    public void editar(Pessoa pes) {
        if (pessoaFacade.pessoaTemVendas(pes.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Esta pessoa possui vendas relacionadas e não pode ser editado"));
            return;
        }

        if (pessoaFacade.pessoaTemCompras(pes.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Esta pessoa possui compras relacionadas e não pode ser editado"));
            return;
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("pessoaId", pes.getId());

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("pessoaCadastro.xhtml");
        } catch (Exception e) {
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
        exportarPDF(pessoaFacade.listaCliAtivo(), "Relatório de Clientes Ativos");
    }

    public void exportarRelatorioFuncionarios() throws DocumentException, IOException {
        exportarPDF(pessoaFacade.listaFuncAtivo(), "Relatório de Funcionários Ativos");
    }

    public void exportarRelatorioFornecedores() throws DocumentException, IOException {
        exportarPDF(pessoaFacade.listaFornAtivo(), "Relatório de Fornecedores Ativos");
    }

    private void exportarPDF(List<Pessoa> pessoas, String tituloRelatorio) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        String fileName = tituloRelatorio.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        Locale ptBr = new Locale("pt", "BR");
        DateFormat dfCompleto = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr);

        java.util.function.Function<Object, String> s = obj -> (obj == null || obj.toString().trim().isEmpty()) ? "-" : obj.toString();

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
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            if (pessoas == null || pessoas.isEmpty()) {
                document.add(new Paragraph("Nenhuma pessoa encontrada para este relatório.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            } else {

                PdfPTable mainTable = new PdfPTable(8);
                mainTable.setWidthPercentage(100);
                mainTable.setWidths(new float[]{1.2f, 1f, 3f, 3f, 1.5f, 0.8f, 4.5f, 2.5f});

                mainTable.addCell(createHeaderCell("Tipo Pessoa"));
                mainTable.addCell(createHeaderCell("Natureza"));
                mainTable.addCell(createHeaderCell("Nome"));
                mainTable.addCell(createHeaderCell("Email"));
                mainTable.addCell(createHeaderCell("CPF/CNPJ"));
                mainTable.addCell(createHeaderCell("Status"));
                mainTable.addCell(createHeaderCell("Endereços"));
                mainTable.addCell(createHeaderCell("Telefones"));

                for (Pessoa p : pessoas) {
                    mainTable.addCell(createDataCell(s.apply(p.getTipo().getLabel()), Element.ALIGN_LEFT));
                    mainTable.addCell(createDataCell(s.apply(p.getTipoPessoa()), Element.ALIGN_CENTER));
                    mainTable.addCell(createDataCell(s.apply(p.getNome()), Element.ALIGN_LEFT));
                    mainTable.addCell(createDataCell(s.apply(p.getEmail()), Element.ALIGN_LEFT));
                    mainTable.addCell(createDataCell(s.apply(p.getCpfcnpjFormatado()), Element.ALIGN_CENTER));
                    mainTable.addCell(createDataCell(p.getAtivo() ? "Ativo" : "Inativo", Element.ALIGN_CENTER));

                    PdfPCell addressesCell = new PdfPCell();
                    addressesCell.setPadding(2);
                    if (p.getListaEnderecos() != null && !p.getListaEnderecos().isEmpty()) {
                        PdfPTable addressTable = new PdfPTable(4);
                        addressTable.setWidthPercentage(100);
                        addressTable.setWidths(new float[]{3f, 1f, 2.5f, 3.5f});

                        addressTable.addCell(createNestedHeaderCell("Rua"));
                        addressTable.addCell(createNestedHeaderCell("Nº"));
                        addressTable.addCell(createNestedHeaderCell("Bairro"));
                        addressTable.addCell(createNestedHeaderCell("Cidade/UF/CEP"));

                        for (Endereco end : p.getListaEnderecos()) {
                            addressTable.addCell(createNestedDataCell(s.apply(end.getRua()), Element.ALIGN_LEFT));
                            addressTable.addCell(createNestedDataCell(s.apply(end.getNumero()), Element.ALIGN_CENTER));
                            addressTable.addCell(createNestedDataCell(s.apply(end.getBairro()), Element.ALIGN_LEFT));
                            String cidadeUfCep = String.format("%s/%s - %s",
                                    s.apply(end.getCidade().getNome()),
                                    s.apply(end.getCidade().getEstado().getSigla()),
                                    s.apply(end.getCep()));
                            addressTable.addCell(createNestedDataCell(cidadeUfCep, Element.ALIGN_LEFT));
                        }
                        addressesCell.addElement(addressTable);
                    } else {
                        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLACK);
                        Phrase emptyAddressPhrase = new Phrase("Nenhum endereço", nestedContentFont);
                        addressesCell.addElement(emptyAddressPhrase);
                        addressesCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        addressesCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    }
                    mainTable.addCell(addressesCell);

                    PdfPCell phonesCell = new PdfPCell();
                    phonesCell.setPadding(2);
                    if (p.getListaTelefones() != null && !p.getListaTelefones().isEmpty()) {
                        PdfPTable phoneTable = new PdfPTable(2);
                        phoneTable.setWidthPercentage(100);
                        phoneTable.setWidths(new float[]{1f, 2f});

                        phoneTable.addCell(createNestedHeaderCell("Tipo"));
                        phoneTable.addCell(createNestedHeaderCell("Número"));

                        for (Telefone tel : p.getListaTelefones()) {
                            phoneTable.addCell(createNestedDataCell(s.apply(tel.getTipoTelefone().toString()), Element.ALIGN_LEFT));
                            phoneTable.addCell(createNestedDataCell(s.apply(tel.getNumero()), Element.ALIGN_LEFT));
                        }
                        phonesCell.addElement(phoneTable);
                    } else {
                        Font nestedContentFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLACK);
                        Phrase emptyPhonePhrase = new Phrase("Nenhum telefone", nestedContentFont);
                        phonesCell.addElement(emptyPhonePhrase);
                        phonesCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        phonesCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    }

                    mainTable.addCell(phonesCell);
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

    public Long getEstadoSelecionadoId() {
        return estadoSelecionadoId;
    }

    public void setEstadoSelecionadoId(Long estadoSelecionadoId) {
        this.estadoSelecionadoId = estadoSelecionadoId;
    }

    public List<Estado> getListaEstados() {
        return listaEstados;
    }

    public List<Cidade> getListaCidades() {
        return listaCidades;
    }

    public Long getCidadeSelecionadaId() {
        return cidadeSelecionadaId;
    }

    public void setCidadeSelecionadaId(Long cidadeSelecionadaId) {
        this.cidadeSelecionadaId = cidadeSelecionadaId;
    }

}
