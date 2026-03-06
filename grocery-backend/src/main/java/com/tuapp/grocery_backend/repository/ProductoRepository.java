package com.tuapp.grocery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tuapp.grocery_backend.model.Producto;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByNombreIgnoreCase(String nombre);
    
    List<Producto> findByCategoriaId(Long categoriaId);

    Producto findByVariedadIgnoreCase(String variedad);

    Producto findByNombreIgnoreCaseAndVariedadIgnoreCase(String nombre, String variedad);
}