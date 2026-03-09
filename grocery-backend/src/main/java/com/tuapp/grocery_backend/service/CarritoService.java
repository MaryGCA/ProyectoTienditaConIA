package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class CarritoService {

    private final ProductoRepository productoRepository;

    // Carrito en memoria
    private final List<Map<String, Object>> carrito = new ArrayList<>();

    public CarritoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public void agregar(Producto producto, int cantidad) {

        if (producto.getStock() < cantidad) return;

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        Map<String, Object> item = new HashMap<>();
        item.put("id", producto.getId()); // 👈 SOLO ESTA LINEA NUEVA
        item.put("nombre", producto.getNombre());
        item.put("precio", producto.getPrecio());
        item.put("cantidad", cantidad);
        item.put("subtotal", producto.getPrecio() * cantidad);
        
        carrito.add(item);
    }

    public List<Map<String, Object>> obtenerCarrito() {
        return carrito;
    }

    public double obtenerTotal() {
        return carrito.stream()
                .mapToDouble(item -> (double) item.get("subtotal"))
                .sum();
    }

    public void vaciarCarrito() {
        carrito.clear();
    }
    public void eliminarProducto(Long id) {
    carrito.removeIf(item -> item.get("id").equals(id));
    }
}