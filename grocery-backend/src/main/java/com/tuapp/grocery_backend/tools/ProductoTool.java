package com.tuapp.grocery_backend.tools;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductoTool {

    private final ProductoRepository productoRepository;

    public ProductoTool(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public String consultarProducto(String nombre) {

        List<Producto> productos =
                productoRepository.findByNombreIgnoreCase(nombre);

        if (productos.isEmpty()) {
            return "No encontré ese producto.";
        }

        Producto p = productos.get(0);

        return "El producto " + p.getNombre() +
               " cuesta $" + p.getPrecio() +
               " y hay " + p.getStock() + " unidades disponibles.";
    }
}