package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class CarritoService {

    private final ProductoRepository productoRepository;

    private final Map<String, List<Map<String, Object>>> carritos = new HashMap<>();

    public CarritoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    private List<Map<String, Object>> getCarrito(String usuario) {
        return carritos.computeIfAbsent(usuario, k -> new ArrayList<>());
    }

    // ✅ AGREGAR
    public void agregar(Producto producto, int cantidad, String usuario) {

        if (producto == null) return;
        if (producto.getStock() < cantidad) return;

        List<Map<String, Object>> carrito = getCarrito(usuario);

        for (Map<String, Object> item : carrito) {

            Long id = ((Number) item.get("id")).longValue();
            String variedad = (String) item.get("variedad");

            if (id.equals(producto.getId()) &&
                variedad.equalsIgnoreCase(producto.getVariedad())) {

                int nuevaCantidad = (int) item.get("cantidad") + cantidad;

                item.put("cantidad", nuevaCantidad);
                item.put("subtotal", producto.getPrecio() * nuevaCantidad);

                producto.setStock(producto.getStock() - cantidad);
                productoRepository.save(producto);
                return;
            }
        }

        Map<String, Object> item = new HashMap<>();
        item.put("id", producto.getId());
        item.put("nombre", producto.getNombre());
        item.put("variedad", producto.getVariedad());
        item.put("precio", producto.getPrecio());
        item.put("cantidad", cantidad);
        item.put("subtotal", producto.getPrecio() * cantidad);

        carrito.add(item);

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    public List<Map<String, Object>> obtenerCarrito(String usuario) {
        return getCarrito(usuario);
    }

    public double obtenerTotal(String usuario) {
        return getCarrito(usuario).stream()
                .mapToDouble(item -> ((Number) item.get("subtotal")).doubleValue())
                .sum();
    }

    // ❌ ELIMINAR SOLO UNO (ID + VARIEDAD)
    public void eliminarProducto(Long id, String variedad, String usuario) {
    getCarrito(usuario).removeIf(item ->
            ((Number) item.get("id")).longValue() == id &&
            ((String) item.get("variedad")).equalsIgnoreCase(variedad)
    );
}

    public void vaciarCarrito(String usuario) {
        getCarrito(usuario).clear();
    }
}