package com.tuapp.grocery_backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;
import com.tuapp.grocery_backend.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin
public class CarritoController {

    private final CarritoService carritoService;
    private final ProductoRepository productoRepository;

    public CarritoController(CarritoService carritoService, ProductoRepository productoRepository) {
        this.carritoService = carritoService;
        this.productoRepository = productoRepository;
    }

    // 🛒 AGREGAR
    @PostMapping("/agregar")
    public void agregar(
            @RequestParam String nombre,
            @RequestParam String variedad, // 🔥 IMPORTANTE
            @RequestParam int cantidad,
            @RequestParam String usuario
    ) {
        Producto producto = productoRepository
                .findByNombreIgnoreCaseAndVariedadIgnoreCase(nombre, variedad);

        carritoService.agregar(producto, cantidad, usuario);
    }

    // 📦 OBTENER
    @GetMapping
    public List<Map<String, Object>> obtener(@RequestParam String usuario) {
        return carritoService.obtenerCarrito(usuario);
    }

    // 💰 TOTAL
    @GetMapping("/total")
    public double total(@RequestParam String usuario) {
        return carritoService.obtenerTotal(usuario);
    }

    // ❌ ELIMINAR SOLO UNO
  @DeleteMapping("/{id}")
public void eliminar(
        @PathVariable Long id,
        @RequestParam String variedad,
        @RequestParam String usuario
) {
    carritoService.eliminarProducto(id, variedad, usuario);
}

    // 🧹 VACIAR
    @DeleteMapping
    public void vaciar(@RequestParam String usuario) {
        carritoService.vaciarCarrito(usuario);
    }
}