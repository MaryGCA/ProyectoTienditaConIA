package com.tuapp.grocery_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String variedad;

    private Double precio;

    private Integer stock;

    @Column(name = "categoria_id")
    private Long categoriaId;

    private Boolean activo;

    private String imagen; // campo agregado

    public Producto() {}

    // Getters y Setters

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getNombre() { 
        return nombre; 
    }

    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public String getVariedad() { 
        return variedad; 
    }

    public void setVariedad(String variedad) { 
        this.variedad = variedad; 
    }

    public Double getPrecio() { 
        return precio; 
    }

    public void setPrecio(Double precio) { 
        this.precio = precio; 
    }

    public Integer getStock() { 
        return stock; 
    }

    public void setStock(Integer stock) { 
        this.stock = stock; 
    }

    public Long getCategoriaId() { 
        return categoriaId; 
    }

    public void setCategoriaId(Long categoriaId) { 
        this.categoriaId = categoriaId; 
    }

    public Boolean getActivo() { 
        return activo; 
    }

    public void setActivo(Boolean activo) { 
        this.activo = activo; 
    }

    public String getImagen() { 
        return imagen; 
    }

    public void setImagen(String imagen) { 
        this.imagen = imagen; 
    }
}