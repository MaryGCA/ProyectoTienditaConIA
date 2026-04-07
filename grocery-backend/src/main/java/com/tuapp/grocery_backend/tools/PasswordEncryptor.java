package com.tuapp.grocery_backend.tools;

import com.tuapp.grocery_backend.model.Usuario;
import com.tuapp.grocery_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptor implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public PasswordEncryptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo como herramienta: no se crea nada por defecto
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
                usuario.setPassword(encoder.encode(usuario.getPassword()));
                usuarioRepository.save(usuario);
                System.out.println("Contraseña encriptada para: " + usuario.getEmail());
            }
        }
    }
}