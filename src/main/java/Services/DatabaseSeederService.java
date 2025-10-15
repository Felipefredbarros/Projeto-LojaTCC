/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import Entidades.Cidade;
import Entidades.Estado;
import Facade.CidadeFacade;
import Facade.EstadoFacade;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author felip
 */
@Singleton 
@Startup   

public class DatabaseSeederService {

    @Inject
    private IbgeService ibgeService; 

    @EJB
    private EstadoFacade estadoFacade;

    @EJB
    private CidadeFacade cidadeFacade;

    @PostConstruct
    public void onStartup() {
        if (estadoFacade.count() == 0) {
            popularEstadosECidades();
        } else {
            System.out.println("Banco de dados já populado. Ignorando seeder.");
        }
    }

    public void popularEstadosECidades() {
        try {
            System.out.println("Iniciando a população do banco de dados de estados e cidades...");

            List<Estado> estadosDaApi = ibgeService.buscarEstados();

            for (Estado estadoApi : estadosDaApi) {
                Estado novoEstado = new Estado();
                novoEstado.setId(estadoApi.getId()); 
                novoEstado.setNome(estadoApi.getNome());
                novoEstado.setSigla(estadoApi.getSigla());

                novoEstado = estadoFacade.salvarCerto(novoEstado);

                List<Cidade> cidadesDaApi = ibgeService.buscarCidadesPorEstado(estadoApi.getId());

                for (Cidade cidadeApi : cidadesDaApi) {
                    Cidade novaCidade = new Cidade();
                    novaCidade.setId(cidadeApi.getId()); 
                    novaCidade.setNome(cidadeApi.getNome());
                    novaCidade.setEstado(novoEstado); 

                    cidadeFacade.salvar(novaCidade); 
                }
                System.out.println("Estado " + novoEstado.getSigla() + " e suas cidades foram salvos.");
            }

            System.out.println("População do banco concluída com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
