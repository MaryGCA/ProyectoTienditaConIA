package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;
import java.text.Normalizer;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class AssistantService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    private Map<String, Producto> productoPendienteMap = new ConcurrentHashMap<>();

    // =========================
    // CHAT PRINCIPAL
    // =========================
    public String chat(String message, String usuario) {

        if (usuario == null || usuario.isBlank()) {
            return "⚠️ Usuario no identificado";
        }

        if (message == null || message.isBlank()) {
            return "¿Puedes escribirlo otra vez? 😊";
        }

        String msgOriginal = message;
        String msg = normalizar(message);

        List<Producto> productos = productoRepository.findAll();

        // 🔥 FIX LAMBDA (variable final)
        final String msgFinal = msg;

        productos.sort((a, b) -> Integer.compare(
                scoreProducto(msgFinal, b),
                scoreProducto(msgFinal, a)
        ));

        // ===== SALUDO =====
        if (msg.matches(".*\\b(hola|buenas|hey|hi|hello)\\b.*")) {
            return "¡Hola! 👋 ¿Qué te gustaría comprar hoy? 🛒";
        }

        // ===== DESPEDIDA / FINALIZAR ===== 🔥 FIX NUEVO
        if (msg.matches(".*\\b(no|gracias|nada|eso es todo|ya no)\\b.*")) {
            productoPendienteMap.remove(usuario);
            return "¡Perfecto! 😊 Gracias por tu pedido 🛒";
        }

        msg = limpiarMensaje(msg);

        // ===== VARIEDADES =====
        if (msg.matches(".*\\b(tipo|variedad|que tipo|tipos|cuales)\\b.*")) {

            for (Producto p : productos) {
                if (msg.contains(normalizar(p.getNombre()))) {

                    List<Producto> encontrados =
                            productoRepository.findByNombreContainingIgnoreCase(p.getNombre());

                    if (encontrados == null || encontrados.isEmpty()) {
                        return "No encontré variedades de " + capitalize(p.getNombre());
                    }

                    StringBuilder res = new StringBuilder("Tenemos:\n");

                    for (Producto prod : encontrados) {
                        res.append("- ")
                           .append(prod.getVariedad())
                           .append(" ($")
                           .append(prod.getPrecio())
                           .append(")\n");
                    }

                    return res.toString();
                }
            }
        }

        // ===== DISPONIBILIDAD =====
        if (msg.matches(".*\\b(tienes|hay|vendes)\\b.*")) {

            productoPendienteMap.remove(usuario);

            for (Producto p : productos) {
                if (coincideProducto(msg, p)) {
                    return "Sí 😊 tenemos " + getNombre(p) + ". ¿Cuántos quieres?";
                }
            }

            return "Ups 😕 no encontré ese producto";
        }

        // ===== PRODUCTO PENDIENTE =====
        Producto pendiente = productoPendienteMap.get(usuario);

        if (pendiente != null) {

            double cantidad = detectarCantidadNumero(msg);

            if (cantidad > 0) {

                String res = agregarAlCarrito(pendiente, cantidad, getNombre(pendiente), usuario);

                productoPendienteMap.remove(usuario);

                return res + " 🛒 ¿Quieres algo más?";
            }

            return "Claro 😊 ¿cuántos quieres de " + getNombre(pendiente) + "?";
        }

        // ===== LISTA =====
        if (msg.contains(",")) {

            String[] partes = msg.split(",");
            StringBuilder respuesta = new StringBuilder();

            for (String parte : partes) {

                String fragmento = normalizar(parte);
                double cantidad = detectarCantidadNumero(fragmento);

                if (cantidad <= 0) continue;

                Producto p = buscarMejorProducto(fragmento, productos);
                if (p == null) continue;

                respuesta.append(
                        agregarAlCarrito(p, cantidad, getNombre(p), usuario)
                ).append(" ");
            }

            if (!respuesta.isEmpty()) {
                return respuesta.toString() + "🛒";
            }
        }

        // ===== PRODUCTO INDIVIDUAL =====
        for (Producto p : productos) {

            if (coincideProducto(msg, p)) {

                double cantidad = detectarCantidadNumero(msg);

                if (cantidad > 0) {
                    return agregarAlCarrito(p, cantidad, getNombre(p), usuario)
                            + " 🛒 ¿Quieres algo más?";
                }

                productoPendienteMap.put(usuario, p);

                return "Claro 😊 ¿cuántos quieres de " + getNombre(p) + "?";
            }
        }

        return "No entendí bien 🤔 ¿puedes intentarlo diferente?";
    }

    // =========================
    // NORMALIZAR TEXTO
    // =========================
    private String normalizar(String texto) {

        if (texto == null) return "";

        texto = texto.toLowerCase().trim();

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        if (texto.endsWith("s")) {
            texto = texto.substring(0, texto.length() - 1);
        }

        return texto.replaceAll("\\s+", " ");
    }

    private String limpiarMensaje(String msg) {

        String[] stopWords = {
                "quiero", "dame", "agrega", "al", "carrito",
                "por favor", "vendeme", "me das", "ponme"
        };

        for (String w : stopWords) {
            msg = msg.replace(w, "").trim();
        }

        return msg;
    }

    // =========================
    // MATCH INTELIGENTE
    // =========================
    private boolean coincideProducto(String msg, Producto p) {

        String nombre = normalizar(p.getNombre());
        String variedad = p.getVariedad() != null ? normalizar(p.getVariedad()) : "";

        if (!variedad.isEmpty() && msg.contains(nombre + " " + variedad)) return true;
        if (!variedad.isEmpty() && msg.contains(variedad)) return true;
        if (msg.contains(nombre)) return true;

        return false;
    }

    private Producto buscarMejorProducto(String msg, List<Producto> productos) {

        Producto mejor = null;
        int mejorScore = -1;

        for (Producto p : productos) {
            int score = scoreProducto(msg, p);

            if (score > mejorScore) {
                mejorScore = score;
                mejor = p;
            }
        }

        return mejor;
    }

    private int scoreProducto(String msg, Producto p) {

        String nombre = normalizar(p.getNombre());
        String variedad = p.getVariedad() != null ? normalizar(p.getVariedad()) : "";

        int score = 0;

        if (msg.contains(nombre + " " + variedad)) score += 5;
        if (!variedad.isEmpty() && msg.contains(variedad)) score += 3;
        if (msg.contains(nombre)) score += 1;

        return score;
    }

    private String getNombre(Producto p) {
        return (p.getVariedad() != null && !p.getVariedad().isBlank())
                ? p.getVariedad()
                : capitalize(p.getNombre());
    }

    private String agregarAlCarrito(Producto p, double cantidad, String nombre, String usuario) {

        int cantidadEntera = (int) Math.ceil(cantidad);

        if (p.getStock() == null || p.getStock() < cantidadEntera) {

            if (p.getStock() != null && p.getStock() > 0) {
                carritoService.agregar(p, p.getStock(), usuario);
                return "Solo quedan " + p.getStock() + " de " + nombre + " 😮";
            }

            return "Ups 😕 por ahora no tenemos " + nombre;
        }

        carritoService.agregar(p, cantidadEntera, usuario);

        return "Perfecto 👍 agregué " + cantidad + " de " + nombre;
    }

    private double detectarCantidadNumero(String msg) {

        msg = normalizar(msg);

        if (msg.contains("medio")) return 0.5;
        if (msg.contains("cuarto")) return 0.25;

        Pattern p = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(msg);

        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }

        return 0;
    }

    private String capitalize(String str) {
        return (str == null || str.isEmpty())
                ? str
                : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}