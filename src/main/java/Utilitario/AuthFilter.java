
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Importe HttpSession
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter { // <- Implementa a interface Filter

    // MÉTODO OBRIGATÓRIO: init
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Pode deixar vazio se não precisar de inicialização
        // Filter.super.init(filterConfig); // Se estiver usando Java 8+, pode usar o default, senão, deixe vazio ou com sua lógica
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath(); // Pega /projeto-teste

        // Verifica se a URI contém recursos estáticos do JSF
        boolean recursoEstatico = uri.contains(contextPath + "/javax.faces.resource/"); // Mais preciso

        // Verifica se a URI termina EXATAMENTE com /faces/login.xhtml
        boolean paginaLogin = uri.endsWith("/faces/login.xhtml");

        // Verifica se existe um usuário na sessão
        boolean usuarioLogado = (session != null && session.getAttribute("usuarioLogado") != null);

        if (usuarioLogado || paginaLogin || recursoEstatico) {
            chain.doFilter(req, res); 
        } else {
            response.sendRedirect(contextPath + "/faces/login.xhtml");
        }
    }

    @Override
    public void destroy() {
        // Pode deixar vazio se não precisar de limpeza ao descarregar
        // Filter.super.destroy(); // Se estiver usando Java 8+, pode usar o default, senão, deixe vazio ou com sua lógica
    }
}
