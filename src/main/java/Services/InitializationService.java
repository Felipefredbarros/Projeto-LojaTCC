package Services; // Ou o pacote onde você quer colocar este serviço

import Entidades.Usuario;
import Facade.UsuarioFacade;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton 
@Startup   
public class InitializationService {

    private static final Logger LOGGER = Logger.getLogger(InitializationService.class.getName());

    private static final String DEFAULT_ADMIN_LOGIN = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "lojaadmin@123"; 

    @EJB
    private UsuarioFacade usuarioFacade;

    @PostConstruct
    public void initialize() {
        LOGGER.info("Verificando a existência de usuários no sistema...");

        try {
            long userCount = usuarioFacade.count(); 

            if (userCount == 0) {
                LOGGER.info("Nenhum usuário encontrado. Criando usuário administrador padrão...");
                criarAdminPadrao();
            } else {
                LOGGER.log(Level.INFO, "{0} usuário(s) já existem no sistema. Nenhuma ação necessária.", userCount);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar/criar usuário padrão durante a inicialização.", e);
        }
    }

    private void criarAdminPadrao() {
        Usuario adminUser = new Usuario();
        adminUser.setLogin(DEFAULT_ADMIN_LOGIN);

        // IMPORTANTE: Usar o método setSenha que já faz o HASH com BCrypt!
        adminUser.setSenha(DEFAULT_ADMIN_PASSWORD);


        try {
            usuarioFacade.salvar(adminUser);
            LOGGER.log(Level.INFO, "Usuário administrador padrão (''{0}'') criado com SUCESSO.", DEFAULT_ADMIN_LOGIN);
            LOGGER.log(Level.WARNING, "*** ATENÇÃO: A senha padrão ''{0}'' é INSEGURA. Altere-a imediatamente após o primeiro login! ***", DEFAULT_ADMIN_PASSWORD);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao persistir o usuário administrador padrão.", e);
            // Lançar exceção pode impedir o deploy se a criação do admin for crítica
            // throw new RuntimeException("Falha ao persistir admin padrão", e);
        }
    }

}