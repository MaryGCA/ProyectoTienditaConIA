package com.tuapp.grocery_backend.controller;

import com.tuapp.grocery_backend.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recetas")
@CrossOrigin
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping("/buscar")
    public Map<String, String> buscar(@RequestBody Map<String, Object> body) {

        List<String> ingredientes = (List<String>) body.get("ingredientes");
        String usuario = (String) body.get("usuario");

        String respuesta = recipeService.buscarRecetas(ingredientes, usuario);

        return Map.of("reply", respuesta);
    }

    @PostMapping("/instrucciones")
    public Map<String, String> instrucciones(@RequestBody Map<String, Object> body) {

        String usuario = (String) body.get("usuario");

        String respuesta = recipeService.obtenerInstrucciones(usuario);

        return Map.of("reply", respuesta);
    }
}