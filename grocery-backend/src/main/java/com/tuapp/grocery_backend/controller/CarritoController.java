package com.tuapp.grocery_backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.tuapp.grocery_backend.service.CarritoService;
import com.tuapp.grocery_backend.tools.CarritoTool;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoTool carritoTool;

    public CarritoController(CarritoService carritoService,
                             CarritoTool carritoTool) {
        this.carritoService = carritoService;
        this.carritoTool = carritoTool;
    }

    // Ver productos del carrito
    @GetMapping
    public List<Map<String, Object>> verCarrito() {
        return carritoService.obtenerCarrito();
    }

    // Obtener total
    @GetMapping("/total")
    public double total() {
        return carritoService.obtenerTotal();
    }

    // Vaciar carrito
    @DeleteMapping
    public void vaciar() {
        carritoService.vaciarCarrito();
    }

    // Agregar producto (lo usa frontend y agente)
    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam String nombre,
                                  @RequestParam int cantidad) {

        return carritoTool.agregarAlCarrito(nombre, cantidad);

    }
    // Eliminar producto individual
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {
        carritoService.eliminarProducto(id);
    }
}