/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import Entidades.Cidade;
import Entidades.Estado;
import javax.enterprise.context.ApplicationScoped;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;



/**
 *
 * @author felip
 */
@ApplicationScoped
public class IbgeService {

    private final Gson gson = new Gson();

    // URL para buscar todos os estados
    private static final String URL_ESTADOS = "https://servicodados.ibge.gov.br/api/v1/localidades/estados";

    // URL base para buscar cidades de um estado (precisa do ID do estado)
    private static final String URL_CIDADES_POR_ESTADO = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/%d/municipios";

    public List<Estado> buscarEstados() throws IOException {
        // Cria a URL para buscar estados, já ordenada por nome
        URL url = new URL(URL_ESTADOS + "?orderBy=nome");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Verifica se a requisição foi bem sucedida (código 200)
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Lê a resposta da API
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            Type tipoListaEstados = new TypeToken<ArrayList<Estado>>() {
            }.getType();
            List<Estado> estados = gson.fromJson(content.toString(), tipoListaEstados);

            return estados;
        } else {
            con.disconnect();
            throw new IOException("Falha ao buscar estados da API do IBGE. Código de resposta: " + responseCode);
        }
    }

    public List<Cidade> buscarCidadesPorEstado(Long estadoId) throws IOException {
        // Formata a URL com o ID do estado recebido
        String urlFormatada = String.format(URL_CIDADES_POR_ESTADO, estadoId);
        URL url = new URL(urlFormatada);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            // A lógica do TypeToken para cidades também está correta
            Type tipoListaCidades = new TypeToken<ArrayList<Cidade>>() {
            }.getType();
            List<Cidade> cidades = gson.fromJson(content.toString(), tipoListaCidades);

            // Ordena a lista de cidades por nome
            cidades.sort(Comparator.comparing(Cidade::getNome));

            return cidades;
        } else {
            con.disconnect();
            throw new IOException("Falha ao buscar cidades da API do IBGE. Código de resposta: " + responseCode);
        }
    }

}
