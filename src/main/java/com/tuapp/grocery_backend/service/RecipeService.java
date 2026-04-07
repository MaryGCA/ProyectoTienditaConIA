package com.tuapp.grocery_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RecipeService {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    private Map<String, Integer> recetaPendiente = new HashMap<>();

    // =========================
    // 🔥 BUSCAR RECETAS
    // =========================
    public String buscarRecetas(List<String> ingredientes, String usuario) {

        try {

            if (ingredientes == null || ingredientes.isEmpty()) {
                return "Primero agrega productos al carrito 🛒";
            }

            if (apiKey == null || apiKey.isBlank()) {
                return "Error: API KEY no configurada ⚠️";
            }

            RestTemplate restTemplate = crearRestTemplateUTF8();

            // 🔥 LIMPIEZA REAL (CLAVE)
            List<String> ingredientesLimpios = ingredientes.stream()
                    .map(this::limpiarIngredienteReal)
                    .map(this::traducirIngredienteAPI)
                    .map(i -> i.toLowerCase().trim())
                    .toList();

            String ingredientesTexto = URLEncoder.encode(
                    String.join(",", ingredientesLimpios),
                    StandardCharsets.UTF_8
            );

            String url = "https://api.spoonacular.com/recipes/findByIngredients"
                    + "?ingredients=" + ingredientesTexto
                    + "&number=5"
                    + "&ranking=2"
                    + "&ignorePantry=true"
                    + "&apiKey=" + apiKey;

            System.out.println("🔥 INGREDIENTES LIMPIOS: " + ingredientesLimpios);
            System.out.println("🔎 URL: " + url);

            List<Map<String, Object>> recetas =
                    restTemplate.getForObject(url, List.class);

            if (recetas == null || recetas.isEmpty()) {
                return "No encontré recetas con tu carrito 😢";
            }

            Map<String, Object> receta = recetas.get(0);

            Integer id = (Integer) receta.get("id");
            String nombre = (String) receta.get("title");

            recetaPendiente.put(usuario, id);

            List<Map<String, Object>> faltantes =
                    (List<Map<String, Object>>) receta.get("missedIngredients");

            StringBuilder respuesta = new StringBuilder();

            respuesta.append("🍳 Puedes preparar:\n")
                    .append(traducirTexto(nombre))
                    .append("\n\n");

            if (faltantes != null && !faltantes.isEmpty()) {

                respuesta.append("Te faltan:\n");

                faltantes.stream()
                        .limit(5)
                        .forEach(f -> {
                            String nombreIng = (String) f.get("name");
                            respuesta.append("• ")
                                    .append(traducirTexto(nombreIng))
                                    .append("\n");
                        });

            } else {
                respuesta.append("¡Tienes todo! 🎉\n");
            }

            respuesta.append("\n¿Quieres instrucciones?");

            return respuesta.toString();

        } catch (Exception e) {

            e.printStackTrace(); // 🔥 MUY IMPORTANTE

            return "Error al buscar recetas 😢\n"
                    + "Detalle técnico (debug): " + e.toString();
        }
    }

    // =========================
    // 🔥 INSTRUCCIONES
    // =========================
    public String obtenerInstrucciones(String usuario) {

        try {

            Integer id = recetaPendiente.get(usuario);

            if (id == null) {
                return "Primero pide una receta 😅";
            }

            RestTemplate restTemplate = crearRestTemplateUTF8();

            String url = "https://api.spoonacular.com/recipes/"
                    + id
                    + "/information?apiKey=" + apiKey;

            Map<String, Object> receta =
                    restTemplate.getForObject(url, Map.class);

            StringBuilder instrucciones = new StringBuilder("👨‍🍳 Instrucciones:\n\n");

            // 🔥 OPCIÓN 1: pasos estructurados
            List<Map<String, Object>> instruccionesAnalizadas =
                    (List<Map<String, Object>>) receta.get("analyzedInstructions");

            if (instruccionesAnalizadas != null && !instruccionesAnalizadas.isEmpty()) {

                Map bloque = instruccionesAnalizadas.get(0);
                List<Map<String, Object>> pasos =
                        (List<Map<String, Object>>) bloque.get("steps");

                if (pasos != null && !pasos.isEmpty()) {

                    for (Map paso : pasos) {
                        instrucciones.append("👉 ")
                                .append(traducirTexto((String) paso.get("step")))
                                .append("\n\n");
                    }

                    return instrucciones.toString();
                }
            }

            // 🔥 OPCIÓN 2: texto plano
            String instruccionesTexto = (String) receta.get("instructions");

            if (instruccionesTexto != null && !instruccionesTexto.isBlank()) {

                return "👨‍🍳 Instrucciones:\n\n"
                        + traducirTexto(limpiarTexto(instruccionesTexto));
            }

            // 🔥 OPCIÓN 3: fallback
            return "No encontré instrucciones para esta receta 😢";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al obtener instrucciones 😢\n" + e.toString();
        }
    }

    // =========================
    // 🔥 LIMPIAR INGREDIENTE REAL (CLAVE)
    // =========================
    private String limpiarIngredienteReal(String ingrediente) {

        ingrediente = ingrediente.toLowerCase();

        if (ingrediente.contains("pepino")) return "cucumber";
        if (ingrediente.contains("lechuga")) return "lettuce";
        if (ingrediente.contains("berenjena")) return "eggplant";
        if (ingrediente.contains("manzana")) return "apple";
        if (ingrediente.contains("fresa")) return "strawberry";

        return ingrediente.replaceAll("[^a-zA-Z ]", "").trim();
    }

    // =========================
    // 🔥 TRADUCIR PARA API
    // =========================
    private String traducirIngredienteAPI(String ingrediente) {
        return ingrediente;
    }

    // =========================
    // 🔥 TRADUCCIÓN GENERAL
    // =========================
    private String traducirTexto(String texto) {

        try {

            String url = "https://api.mymemory.translated.net/get?q="
                    + URLEncoder.encode(texto, StandardCharsets.UTF_8)
                    + "&langpair=en|es";

            RestTemplate restTemplate = crearRestTemplateUTF8();

            Map response = restTemplate.getForObject(url, Map.class);

            Map responseData = (Map) response.get("responseData");

            return limpiarTexto(responseData.get("translatedText").toString());

        } catch (Exception e) {
            return limpiarTexto(texto);
        }
    }

    // =========================
    // 🔥 LIMPIAR TEXTO
    // =========================
    private String limpiarTexto(String texto) {
        return texto
                .replaceAll("<[^>]*>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("%20", " ")
                .replaceAll("%21", "")
                .replaceAll("%22", " ")
                .replaceAll("%23", " ")
                .replaceAll("%24", " ")
                .replaceAll("%25", " ")
                .replaceAll("%26", " ")
                .replaceAll("%27", "")
                .replaceAll("%28", " ")
                .replaceAll("%29", " ")
                .replaceAll("%2C", " ")
                .replaceAll("%2E", " ")
            

                .trim();
    }

    // =========================
    // 🔥 UTF-8
    // =========================
    private RestTemplate crearRestTemplateUTF8() {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().forEach(converter -> {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter)
                        .setDefaultCharset(StandardCharsets.UTF_8);
            }
        });

        return restTemplate;
    }
}