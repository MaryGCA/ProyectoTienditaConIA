package com.tuapp.grocery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.tuapp.grocery_backend.model.Producto;   // 👈 CORREGIDO
import com.tuapp.grocery_backend.repository.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ProductoController {

    private final ProductoRepository productoRepository;

    @GetMapping
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }
}