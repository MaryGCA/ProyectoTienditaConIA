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

    // =========================
    // OBTENER CARRITO POR USUARIO
    // =========================
    private List<Map<String, Object>> getCarrito(String usuario) {
        return carritos.computeIfAbsent(usuario, k -> new ArrayList<>());
    }

    // =========================
    // AGREGAR PRODUCTO
    // =========================
    public void agregar(Producto producto, int cantidad, String usuario) {

        if (producto == null) return;

        if (producto.getStock() == null || producto.getStock() < cantidad) return;

        List<Map<String, Object>> carrito = getCarrito(usuario);

        for (Map<String, Object> item : carrito) {

            Long id = ((Number) item.get("id")).longValue();
            String variedad = (String) item.get("variedad");

            if (id.equals(producto.getId()) &&
                Objects.equals(variedad, producto.getVariedad())) {

                int cantidadActual = ((Number) item.get("cantidad")).intValue();
                int nuevaCantidad = cantidadActual + cantidad;

                item.put("cantidad", nuevaCantidad);
                item.put("subtotal", producto.getPrecio() * nuevaCantidad);

                producto.setStock(producto.getStock() - cantidad);
                productoRepository.save(producto);

                return;
            }
        }

        Map<String, Object> nuevoItem = new HashMap<>();
        nuevoItem.put("id", producto.getId());
        nuevoItem.put("nombre", producto.getNombre());
        nuevoItem.put("variedad", producto.getVariedad());
        nuevoItem.put("precio", producto.getPrecio());
        nuevoItem.put("cantidad", cantidad);
        nuevoItem.put("subtotal", producto.getPrecio() * cantidad);

        carrito.add(nuevoItem);

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    // =========================
    // OBTENER CARRITO
    // =========================
    public List<Map<String, Object>> obtenerCarrito(String usuario) {
        return getCarrito(usuario);
    }

    // =========================
    // TOTAL
    // =========================
    public double obtenerTotal(String usuario) {
        return getCarrito(usuario).stream()
                .mapToDouble(item -> ((Number) item.get("subtotal")).doubleValue())
                .sum();
    }

    // =========================
    // ELIMINAR PRODUCTO
    // =========================
    public void eliminarProducto(Long id, String variedad, String usuario) {

        getCarrito(usuario).removeIf(item ->
                ((Number) item.get("id")).longValue() == id &&
                Objects.equals(
                        ((String) item.get("variedad")),
                        variedad
                )
        );
    }

    // =========================
    // VACIAR CARRITO
    // =========================
    public void vaciarCarrito(String usuario) {
        getCarrito(usuario).clear();
    }
}