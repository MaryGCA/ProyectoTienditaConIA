package com.tuapp.grocery_backend.service;

import com.tuapp.grocery_backend.model.Usuario;
import com.tuapp.grocery_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // LOGIN
    public Optional<Usuario> login(String email, String rawPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(rawPassword, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    // REGISTRO sin validar email
    public Usuario registrar(Usuario usuario, String rawPassword) {
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(rawPassword));

        // Fecha de creación
        usuario.setFechaCreacion(new java.sql.Timestamp(System.currentTimeMillis()));

        // Rol por defecto
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("cliente");
        }

        // Imagen por defecto
        if (usuario.getImagen() == null || usuario.getImagen().isEmpty()) {
            usuario.setImagen("https://www.iml.es/wp-content/uploads/2023/11/tratamiento-suavizar-arrugas-en-hombres-iml.jpg");
        }

        // Guardar en BD
        return usuarioRepository.save(usuario);
    }
}