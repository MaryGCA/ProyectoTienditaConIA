package com.tuapp.grocery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoRepository productoRepository;

    private final String IMAGE_DIR = "src/main/resources/public/images/";

    // ================================
    // OBTENER TODOS
    @GetMapping
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }

    // ================================
    // POR CATEGORIA
    @GetMapping("/categoria/{id}")
    public List<Producto> getByCategoria(@PathVariable Long id) {
        return productoRepository.findByCategoriaId(id);
    }

    // ================================
    // CREAR
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {

        if (producto.getPrecio() == null) producto.setPrecio(0.0);
        if (producto.getStock() == null) producto.setStock(0);
        if (producto.getCategoriaId() == null) producto.setCategoriaId(1L);

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // ================================
    // EDITAR
    @PutMapping("/{id}")
    public ResponseEntity<Producto> editarProducto(
            @PathVariable Long id,
            @RequestBody Producto data
    ) {

        Optional<Producto> existente = productoRepository.findById(id);
        if (existente.isEmpty()) return ResponseEntity.notFound().build();

        Producto producto = existente.get();

        producto.setNombre(data.getNombre());
        producto.setVariedad(data.getVariedad());
        producto.setPrecio(data.getPrecio());
        producto.setStock(data.getStock());
        producto.setCategoriaId(data.getCategoriaId());

        // 🔥 IMPORTANTE: ahora acepta URLs
        if (data.getImagen() != null) {
            producto.setImagen(data.getImagen());
        }

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // ================================
    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {

        if (!productoRepository.existsById(id))
            return ResponseEntity.notFound().build();

        productoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ================================
    // SERVIR IMÁGENES LOCALES
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws MalformedURLException {

        File file = new File(IMAGE_DIR + filename);

        if (!file.exists())
            return ResponseEntity.notFound().build();

        UrlResource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(resource);
    }
}