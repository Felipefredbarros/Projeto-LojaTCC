package Controladores;

import Entidades.Produto;
import Entidades.ProdutoDerivacao;
import Facade.CompraFacade;
import Facade.PessoaFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.DashboardModel;

/**
 *
 * @author felip
 */
@ManagedBean
@SessionScoped
public class dashBoardControle implements Serializable {

    @EJB
    private PessoaFacade pessoaFacade;
    @EJB
    private ProdutoFacade produtoFacade;
    @EJB
    private CompraFacade compraFacade;
    @EJB
    private VendaFacade vendaFacade;

    private DashboardModel dashboardModel;

    public Long getTotalProdutos() {
        return produtoFacade.totalProdutosCadastradosAtivos();
    }

    public List<String> getProdutosEstoqueBaixo() {
        List<ProdutoDerivacao> produtosBaixoEstoque = produtoFacade.listarProdutosEstoqueBaixo();
        List<String> descricoes = new ArrayList<>();
        for (ProdutoDerivacao derivacao : produtosBaixoEstoque) {
            descricoes.add(derivacao.getTexto());
        }
        return descricoes;
    }

    public Double getTotalEstoque() {
        return produtoFacade.totalEstoqueGeral();
    }

    public Long getTotalVendas() {
        return vendaFacade.totalVendasCadastradas();
    }

    public Double getValorTotalVendido() {
        return vendaFacade.valorVendaVendido();
    }

    public Long getTotalCompras() {
        return compraFacade.totalComprasCadastradas();
    }

    public Double getValorTotalComprado() {
        return compraFacade.valorComprasVendido();
    }

    public Double getLucroLiquido() {
        return vendaFacade.valorVendaVendido() - compraFacade.valorComprasVendido();
    }

}
