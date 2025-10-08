/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilitario;

import Entidades.ContaPagar;
import Entidades.ContaReceber;
import Entidades.Venda;

/**
 *
 * @author felip
 */
public final class FinanceDesc {

    private FinanceDesc() {
    }

    // ====== PÚBLICOS =========================================================
    public static String pagamentoContaPagar(ContaPagar cp, String observacao) {
        String titulo = String.format("Pagamento de Conta a Pagar #%s", safeId(cp));
        String desc = join(titulo, optionalDescricao(cp), optionalObs(observacao));
        return sanitize(desc);
    }

    public static String recebimentoContaReceber(ContaReceber cr, String observacao) {
        String titulo = String.format("Recebimento da Conta a Receber #%s", safeId(cr));
        String refVenda = cr != null && cr.getVenda() != null
                ? String.format("referente à Venda #%s", safeId(cr.getVenda()))
                : null;
        String desc = join(join(titulo, refVenda), optionalDescricao(cr), optionalObs(observacao));
        return sanitize(desc);
    }

    public static String recebimentoVendaAVista(Venda venda, ContaReceber cr, String observacao) {
        String titulo = String.format("Recebimento de venda à vista #%s", safeId(venda));
        String refCR = cr != null ? String.format("(Conta a Receber #%s)", safeId(cr)) : null;
        String desc = join(join(titulo, refCR), optionalObs(observacao));
        return sanitize(desc);
    }

    public static String estornoRecebimentoCR(ContaReceber cr, String motivo) {
        String titulo = String.format("Estorno de recebimento da Conta a Receber #%s", safeId(cr));
        String refVenda = cr != null && cr.getVenda() != null
                ? String.format("(Venda #%s)", safeId(cr.getVenda()))
                : null;
        String desc = join(join(titulo, refVenda), optionalDescricao(cr), optionalMotivo(motivo));
        return sanitize(desc);
    }

    public static String estornoPagamentoCP(ContaPagar cp, String motivo) {
        String titulo = String.format("Estorno de pagamento da Conta a Pagar #%s", safeId(cp));
        String desc = join(titulo, optionalDescricao(cp), optionalMotivo(motivo));
        return sanitize(desc);
    }

    // ====== PRIVADOS =========================================================
    private static String optionalDescricao(Object obj) {
        String d = null;
        if (obj instanceof ContaPagar) {
            d = ((ContaPagar) obj).getDescricao();
        } else if (obj instanceof ContaReceber) {
            d = ((ContaReceber) obj).getDescricao();
        } else if (obj instanceof Venda) {
            // se quiser usar alguma descrição de venda no futuro
            // d = ((Venda) obj).getDescricao();
        }
        d = clean(d);
        return isEmpty(d) ? null : d;
    }

    private static String optionalObs(String obs) {
        obs = clean(obs);
        return isEmpty(obs) ? null : "(" + obs + ")";
    }

    private static String optionalMotivo(String motivo) {
        motivo = clean(motivo);
        return isEmpty(motivo) ? null : "Motivo: " + motivo;
    }

    private static String join(String a, String b) {
        if (isEmpty(a)) {
            return b;
        }
        if (isEmpty(b)) {
            return a;
        }
        return a + " " + (b.startsWith("(") ? b : "- " + b);
    }

    private static String join(String... parts) {
        String out = null;
        for (String p : parts) {
            if (!isEmpty(p)) {
                out = join(out, p);
            }
        }
        return out == null ? "" : out;
    }

    private static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        // remove espaços duplicados e ajusta "- (" para " ("
        s = s.replaceAll("\\s+", " ").trim()
                .replaceAll("- \\(", "(");
        return s;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String clean(String s) {
        return s == null ? null : s.trim();
    }

    private static String safeId(Object o) {
        if (o == null) {
            return "?";
        }
        try {
            // tenta getId() via reflexão para classe genérica
            Object id = o.getClass().getMethod("getId").invoke(o);
            return id == null ? "?" : String.valueOf(id);
        } catch (Exception e) {
            return "?";
        }
    }
}
