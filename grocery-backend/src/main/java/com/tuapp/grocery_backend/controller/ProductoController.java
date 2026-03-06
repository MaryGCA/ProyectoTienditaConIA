package com.tuapp.grocery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ProductoController {

    private final ProductoRepository productoRepository;

    // 🔹 Obtener todos los productos
    @GetMapping
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }

    // 🔹 Obtener productos por categoría
    @GetMapping("/categoria/{id}")
    public List<Producto> getByCategoria(@PathVariable Long id) {
        return productoRepository.findByCategoriaId(id);
    }
}