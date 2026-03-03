package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class AssistantService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    // =========================
    // Guardar el producto pendiente para agregar
    // =========================
    private Producto productoPendiente = null;

    public String chat(String message) {

        if (message == null || message.isBlank()) {
            return "¿Podrías escribir tu consulta nuevamente? 😊";
        }

        String msg = message.toLowerCase().trim();
        List<Producto> productos = productoRepository.findAll();

        // ===== SALUDOS =====
        if (msg.matches(".*\\b(hola|buenas|hey|hi|hello)\\b.*")) {
            return "¡Hola! 👋 Soy tu asistente virtual. Puedes preguntarme por productos o hacer pedidos.";
        }

        // ===== LIMPIAR PALABRAS IRRELEVANTES =====
        String[] stopWords = {"quiero", "dame", "agrega", "al", "carrito", "por favor", "vendeme"};
        for (String w : stopWords) {
            msg = msg.replace(w, "").trim();
        }

        // ===== CONSULTA DE VARIEDADES =====
        if (msg.matches(".*\\b(tipo|variedad|qué tipo)\\b.*")) {

            for (Producto p : productos) {

                if (msg.contains(p.getNombre().toLowerCase())) {

                    List<Producto> encontrados =
                            productoRepository.findByNombreContainingIgnoreCase(p.getNombre());

                    if (encontrados == null || encontrados.isEmpty()) {
                        return "No encontré variedades disponibles de " +
                                capitalize(p.getNombre()) + ".";
                    }

                    StringBuilder res = new StringBuilder();
                    res.append("Tenemos las siguientes variedades de ")
                       .append(capitalize(p.getNombre()))
                       .append(":\n");

                    for (Producto prod : encontrados) {
                        res.append("- ")
                           .append(prod.getVariedad() != null ?
                                   prod.getVariedad() : "Sin variedad")
                           .append(" ($")
                           .append(prod.getPrecio())
                           .append(")\n");
                    }

                    return res.toString();
                }
            }

            return "No encontré el producto solicitado para consultar variedades.";
        }

        // ===== SI HAY PRODUCTO PENDIENTE =====
        if (productoPendiente != null) {

            double cantidad = detectarCantidadNumero(msg);

            if (cantidad > 0) {

                String displayName =
                        productoPendiente.getVariedad() != null
                                ? productoPendiente.getVariedad()
                                : capitalize(productoPendiente.getNombre());

                String resultado =
                        agregarAlCarrito(productoPendiente, cantidad, displayName);

                productoPendiente = null;
                return resultado;

            } else {
                return "Por favor, indica la cantidad que deseas agregar al carrito para "
                        + (productoPendiente.getVariedad() != null
                        ? productoPendiente.getVariedad()
                        : capitalize(productoPendiente.getNombre()))
                        + ".";
            }
        }

        // ==================================================
// ===== DETECCIÓN DE LISTA (CORREGIDA) ============
// ==================================================
if (msg.contains(",")) {

    String[] partes = msg.split(",");
    StringBuilder respuestaFinal = new StringBuilder();
    boolean agregado = false;

    for (String parte : partes) {

        String fragmento = parte.trim();
        double cantidad = detectarCantidadNumero(fragmento);

        if (cantidad <= 0) continue;

        Producto productoEncontrado = null;

        // 1️⃣ PRIORIDAD: COINCIDENCIA EXACTA POR VARIEDAD
        for (Producto p : productos) {
            if (p.getVariedad() != null &&
                fragmento.contains(p.getVariedad().toLowerCase())) {

                productoEncontrado = p;
                break;
            }
        }

        // 2️⃣ SI NO ENCONTRÓ POR VARIEDAD, BUSCAR POR NOMBRE BASE
        if (productoEncontrado == null) {
            for (Producto p : productos) {
                if (fragmento.contains(p.getNombre().toLowerCase())) {
                    productoEncontrado = p;
                    break;
                }
            }
        }

        if (productoEncontrado != null) {

            String displayName =
                    productoEncontrado.getVariedad() != null
                            ? productoEncontrado.getVariedad()
                            : capitalize(productoEncontrado.getNombre());

            respuestaFinal.append(
                    agregarAlCarrito(productoEncontrado, cantidad, displayName)
            ).append(" ");

            agregado = true;
        }
    }

    if (agregado) {
        return respuestaFinal.toString();
    }
}
        // ==================================================
        // ===== DETECCIÓN DE PRODUCTO INDIVIDUAL ==========
        // ==================================================
        for (Producto p : productos) {

            String nombreLower = p.getNombre().toLowerCase();
            String variedadLower =
                    p.getVariedad() != null
                            ? p.getVariedad().toLowerCase()
                            : "";

            if (msg.contains(nombreLower) ||
               (!variedadLower.isEmpty() &&
                msg.contains(variedadLower))) {

                double cantidad = detectarCantidadNumero(msg);

                if (cantidad > 0) {

                    String displayName =
                            p.getVariedad() != null
                                    ? p.getVariedad()
                                    : capitalize(p.getNombre());

                    return agregarAlCarrito(p, cantidad, displayName);
                }

                productoPendiente = p;

                return "Encontré "
                        + (p.getVariedad() != null
                        ? p.getVariedad()
                        : capitalize(p.getNombre()))
                        + ". Por favor, indica la cantidad que deseas agregar al carrito.";
            }
        }

        // ===== RESPUESTAS FALLBACK =====
        String[] respuestasFallback = {
                "Hmm 🤔 no estoy seguro, ¿puedes reformularlo?",
                "No entendí eso, pero podemos intentarlo de nuevo 😊",
                "¡Ups! Creo que eso no es un producto válido, intenta otra vez 🛒"
        };

        return respuestasFallback[
                new Random().nextInt(respuestasFallback.length)
        ];
    }

    // =========================
    // AGREGAR AL CARRITO
    // =========================
    private String agregarAlCarrito(Producto p, double cantidad, String displayName) {

        int cantidadEntera = (int) Math.ceil(cantidad);

        if (p.getStock() == null || p.getStock() < cantidadEntera) {

            if (p.getStock() != null && p.getStock() > 0) {
                carritoService.agregar(p, p.getStock());
                return "Solo quedan " + p.getStock()
                        + " unidad(es) de " + displayName
                        + ", las agregué al carrito 📦";
            }

            return "No hay suficiente stock de " + displayName + " 📦";
        }

        carritoService.agregar(p, cantidadEntera);

        return "Perfecto 🙌 Agregué "
                + cantidad
                + " unidad(es)/kilo(s) de "
                + displayName
                + " al carrito.";
    }

    // =========================
    // DETECTAR CANTIDAD
    // =========================
    private double detectarCantidadNumero(String msg) {

        msg = msg.toLowerCase();

        if (msg.contains("medio kilo") ||
            msg.contains("1/2 kilo") ||
            msg.contains("1/2k")) return 0.5;

        if (msg.contains("cuarto de kilo") ||
            msg.contains("1/4 kilo") ||
            msg.contains("1/4k")) return 0.25;

        Pattern patternKilo =
                Pattern.compile("(\\d+(\\.\\d+)?)\\s*(kilo|kg|k)");
        Matcher matcherKilo = patternKilo.matcher(msg);

        if (matcherKilo.find()) {
            try {
                return Double.parseDouble(matcherKilo.group(1));
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        Pattern patternNum = Pattern.compile("(\\d+)");
        Matcher matcherNum = patternNum.matcher(msg);

        if (matcherNum.find()) {
            try {
                return Double.parseDouble(matcherNum.group(1));
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        return 0;
    }

    // =========================
    // CAPITALIZAR
    // =========================
    private String capitalize(String str) {

        if (str == null || str.isEmpty()) return str;

        return str.substring(0, 1).toUpperCase()
                + str.substring(1);
    }
}