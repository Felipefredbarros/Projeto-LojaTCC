
import Controladores.PessoaControle;
import Converters.ConverterGenerico;
import Entidades.MovimentacaoMensalFuncionario;
import Entidades.Pessoa;
import Entidades.Enums.TipoBonus;
import Facade.MovimentacaoMensalFacade;
import Facade.PessoaFacade;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class MovimentacaoMensalControle implements Serializable {

    @EJB
    private PessoaFacade funcionarioFacade;
    @ManagedProperty("#{pessoaControle}")
    private PessoaControle pessoaControle;
    @EJB
    private MovimentacaoMensalFacade movimentacaoFacade;

    private MovimentacaoMensalFuncionario movimentacao = new MovimentacaoMensalFuncionario();
    private List<Pessoa> listaFuncionarios;
    private ConverterGenerico funcionarioConverter;
    private List<MovimentacaoMensalFuncionario> movimentacoesFuncionario;
    private MovimentacaoMensalFuncionario movSelecionado;

    @PostConstruct
    public void init() {
        listaFuncionarios = funcionarioFacade.listaFuncAtivo();
    }

    public ConverterGenerico getFuncionarioConverter() {
        if (funcionarioConverter == null) {
            funcionarioConverter = new ConverterGenerico(funcionarioFacade);
        }
        return funcionarioConverter;
    }

    public List<TipoBonus> getTiposBonusDisponiveis() {
        return Arrays.asList(TipoBonus.values());
    }

    public void prepararVisualizacao(MovimentacaoMensalFuncionario mov) {
        this.movSelecionado = movimentacaoFacade.findWithVendaAndItens(mov.getId());
    }

    public MovimentacaoMensalFuncionario getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(MovimentacaoMensalFuncionario movimentacao) {
        this.movimentacao = movimentacao;
    }

    public List<Pessoa> getListaFuncionarios() {
        return listaFuncionarios;
    }

    public void setListaFuncionarios(List<Pessoa> listaFuncionarios) {
        this.listaFuncionarios = listaFuncionarios;
    }

    public List<MovimentacaoMensalFuncionario> getMovimentacoesFuncionario() {
        return movimentacaoFacade.listaTodos();
    }

    public void setMovimentacoesFuncionario(List<MovimentacaoMensalFuncionario> movimentacoesFuncionario) {
        this.movimentacoesFuncionario = movimentacoesFuncionario;
    }

    public PessoaControle getPessoaControle() {
        return pessoaControle;
    }

    public void setPessoaControle(PessoaControle pessoaControle) {
        this.pessoaControle = pessoaControle;
    }

    public MovimentacaoMensalFuncionario getMovSelecionado() {
        return movSelecionado;
    }

    public void setMovSelecionado(MovimentacaoMensalFuncionario movSelecionado) {
        this.movSelecionado = movSelecionado;
    }

}
