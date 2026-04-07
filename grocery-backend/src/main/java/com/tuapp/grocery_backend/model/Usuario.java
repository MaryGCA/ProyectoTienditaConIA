package com.tuapp.grocery_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;

    private String imagen;

    @Column(name = "fecha_creacion")
    private java.sql.Timestamp fechaCreacion;

    private String rol; // <- agregar este campo
}