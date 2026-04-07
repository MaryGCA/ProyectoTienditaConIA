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

    @Autowired
    private RecipeService recipeService;

    private Map<String, Producto> productoPendienteMap = new ConcurrentHashMap<>();

    public String chat(String message, String usuario) {

        if (usuario == null || usuario.isBlank()) {
            return "⚠️ Usuario no identificado";
        }

        if (message == null || message.isBlank()) {
            return "¿Puedes escribirlo otra vez? 😊";
        }

        String lower = message.toLowerCase();
        
        // 🔥 detectar ingrediente específico
        String ingredienteDetectado = null;

        if (message.toLowerCase().contains("receta con")) {

            String[] partes = message.toLowerCase().split("receta con");

            if (partes.length > 1) {
                ingredienteDetectado = partes[1].trim();
            }
        }

        // =========================
        // INSTRUCCIONES DE RECETA buscarRecetas
        // =========================
        if (lower.equals("si") || lower.equals("sí") || lower.contains("instrucciones")) {
            return recipeService.obtenerInstrucciones(usuario);
        }

        // =========================
        // RECETAS
        // =========================
        if (lower.contains("receta") ||
            lower.contains("qué puedo cocinar") ||
            lower.contains("que puedo cocinar") ||
            lower.contains("qué puedo hacer") ||
            lower.contains("que puedo hacer") ||
            lower.contains("recomiendame") ||
            lower.contains("recomiéndame")) {

            List<Map<String, Object>> carritoData = carritoService.obtenerCarrito(usuario);

            // 🔥 convertir carrito a lista de ingredientes reales
            List<String> carrito = new ArrayList<>();

            for (Map<String, Object> item : carritoData) {
                Object nombre = item.get("nombre");
                if (nombre != null) {
                    carrito.add(nombre.toString());
                }
            }

            // 🔥 si usuario pidió ingrediente específico
            if (ingredienteDetectado != null && !ingredienteDetectado.isEmpty()) {

                List<String> filtro = new ArrayList<>();
                filtro.add(ingredienteDetectado);

                return recipeService.buscarRecetas(filtro, usuario);

            } else {

                return recipeService.buscarRecetas(carrito, usuario);
            }
        }

        String msg = normalizar(message);

        List<Producto> productos = productoRepository.findAll();

        final String msgFinal = msg;

        productos.sort((a, b) -> Integer.compare(
                scoreProducto(msgFinal, b),
                scoreProducto(msgFinal, a)
        ));

        // =========================
        // SALUDO
        // =========================
        if (msg.matches(".*\\b(hola|buenas|hey|hi|hello)\\b.*")) {
            return "¡Hola! 👋 ¿Qué te gustaría comprar hoy? 🛒";
        }

        // =========================
        // DESPEDIDA
        // =========================
        if (msg.matches(".*\\b(no|gracias|nada|eso es todo|ya no)\\b.*")) {
            productoPendienteMap.remove(usuario);
            return "¡Perfecto! 😊 Gracias por tu pedido 🛒";
        }

        msg = limpiarMensaje(msg);

        // =========================
        // VARIEDADES
        // =========================
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

        // =========================
        // DISPONIBILIDAD
        // =========================
        if (msg.matches(".*\\b(tienes|hay|vendes)\\b.*")) {

            productoPendienteMap.remove(usuario);

            for (Producto p : productos) {
                if (coincideProducto(msg, p)) {
                    productoPendienteMap.put(usuario, p);
                    return "Sí 😊 tenemos " + getNombre(p) + ". ¿Cuántos quieres?";
                }
            }

            return "Ups 😕 no encontré ese producto";
        }

        // =========================
        // PRODUCTO PENDIENTE
        // =========================
        Producto pendiente = productoPendienteMap.get(usuario);

        if (pendiente != null) {

            double cantidad = detectarCantidadNumero(msg);

            if (cantidad > 0) {

                String res = agregarAlCarrito(pendiente, cantidad, getNombre(pendiente), usuario);

                productoPendienteMap.remove(usuario);

                return res + " 🛒 ¿Quieres algo más?";
            }

            return "Claro 😊 ¿Cuántos quieres de " + getNombre(pendiente) + "?";
        }

        // =========================
        // LISTA DE PRODUCTOS
        // =========================
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

        // =========================
        // PRODUCTO INDIVIDUAL
        // =========================
        for (Producto p : productos) {

            if (coincideProducto(msg, p)) {

                double cantidad = detectarCantidadNumero(msg);

                if (cantidad > 0) {
                    return agregarAlCarrito(p, cantidad, getNombre(p), usuario)
                            + " 🛒 ¿Quieres algo más?";
                }

                productoPendienteMap.put(usuario, p);

                return "Claro 😊 tengo " + getNombre(p) + ", ¿cuántos deseas?";
            }
        }

        return "No entendí bien 🤔 ¿puedes intentarlo diferente?";
    }

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

    private boolean coincideProducto(String msg, Producto p) {

        String nombre = normalizar(p.getNombre());
        String variedad = p.getVariedad() != null ? normalizar(p.getVariedad()) : "";

        if (!variedad.isEmpty() && msg.contains(nombre + " " + variedad)) return true;
        if (!variedad.isEmpty() && msg.contains(variedad)) return true;
        return msg.contains(nombre);
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
        return capitalize(p.getNombre());
    }

    private String agregarAlCarrito(Producto p, double cantidad, String nombre, String usuario) {

        int cantidadEntera = (int) Math.ceil(cantidad);

        if (p.getStock() == null || p.getStock() < cantidadEntera) {

            if (p.getStock() != null && p.getStock() > 0) {
                carritoService.agregar(p, p.getStock(), usuario);
                return "Solo quedan " + p.getStock() + " de " + nombre + " 😮";
            }

            return "No encontré suficiente stock 😕";
        }

        carritoService.agregar(p, cantidadEntera, usuario);

        return "Perfecto 👍 agregué " + cantidadEntera + " de " + nombre + " al carrito";
    }

    private double detectarCantidadNumero(String msg) {

        msg = normalizar(msg);

        Map<String, Double> numeros = new HashMap<>();
        numeros.put("uno", 1.0);
        numeros.put("un", 1.0);
        numeros.put("dos", 2.0);
        numeros.put("tres", 3.0);
        numeros.put("cuatro", 4.0);
        numeros.put("cinco", 5.0);
        numeros.put("seis", 6.0);
        numeros.put("siete", 7.0);
        numeros.put("ocho", 8.0);
        numeros.put("nueve", 9.0);
        numeros.put("diez", 10.0);

        for (Map.Entry<String, Double> e : numeros.entrySet()) {
            if (msg.contains(e.getKey())) {
                return e.getValue();
            }
        }

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