package com.tuapp.grocery_backend.tools;

import org.springframework.stereotype.Component;
import java.util.List;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;
import com.tuapp.grocery_backend.service.CarritoService;

@Component
public class CarritoTool {

    private final ProductoRepository productoRepository;
    private final CarritoService carritoService;

    public CarritoTool(ProductoRepository productoRepository,
                       CarritoService carritoService) {
        this.productoRepository = productoRepository;
        this.carritoService = carritoService;
    }

    public String agregarAlCarrito(String nombre, int cantidad) {

        Producto p = null;

        // Buscar por nombre base
        List<Producto> productos = productoRepository.findByNombreIgnoreCase(nombre);
        if (!productos.isEmpty()) {
            p = productos.get(0); // tomar el primero
        }

        // Si no encontró por nombre, intentar por variedad
        if (p == null) {
            p = productoRepository.findByVariedadIgnoreCase(nombre);
        }

        // No encontrado
        if (p == null) {
            return "Producto no encontrado.";
        }

        // Verificar stock
        if (p.getStock() < cantidad) {
            return "No hay suficiente stock.";
        }

        // Agregar al carrito
        carritoService.agregar(p, cantidad, "tool");
        return cantidad + " unidades de " + p.getNombre() + " fueron agregadas al carrito.";
    }
}