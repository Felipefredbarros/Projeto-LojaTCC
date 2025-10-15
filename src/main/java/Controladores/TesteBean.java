/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped; 
import javax.inject.Named;
/**
 *
 * @author felip
 */
@Named("testeBean")
@RequestScoped
public class TesteBean implements Serializable{
    public String getJavaHome() {
        return System.getProperty("java.home");
    }

    public String getTrustStorePath() {
        String path = System.getProperty("javax.net.ssl.trustStore");
        if (path == null || path.isEmpty()) {
            return "Nenhum trust store customizado definido (usando o padrão do java.home).";
        }
        return path;
    }
}
