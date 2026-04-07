package com.tuapp.grocery_backend.controller;

import com.tuapp.grocery_backend.model.Usuario;
import com.tuapp.grocery_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth") 
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Email y contraseña son requeridos")
                );
            }

            return authService.login(email, password)
                    .map(usuario -> ResponseEntity.ok(Map.of(
                            "message", "Login correcto",
                            "user", Map.of(
                                    "id", usuario.getId(),
                                    "nombre", usuario.getNombre(),
                                    "email", usuario.getEmail(),
                                    "imagen", usuario.getImagen(),
                                    "rol", usuario.getRol()
                            )
                    )))
                    .orElse(ResponseEntity.status(401).body(
                            Map.of("message", "Usuario o contraseña incorrectos")
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error interno en login")
            );
        }
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String nombre = body.get("nombre");
            String email = body.get("email");
            String password = body.get("password");
            String imagen = body.get("imagen");

            // Validación básica
            if (nombre == null || email == null || password == null ||
                nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Todos los campos son obligatorios")
                );
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setImagen(imagen); // puede ser null

            Usuario savedUser = authService.registrar(usuario, password);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado correctamente",
                    "user", Map.of(
                            "id", savedUser.getId(),
                            "nombre", savedUser.getNombre(),
                            "email", savedUser.getEmail(),
                            "imagen", savedUser.getImagen(),
                            "rol", savedUser.getRol()
                    )
            ));

        } catch (DataIntegrityViolationException e) {
            // Error típico: email duplicado
            return ResponseEntity.status(400).body(
                    Map.of("message", "El correo ya está registrado")
            );

        } catch (Exception e) {
            e.printStackTrace(); // <-- MUY IMPORTANTE para debug
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error al crear usuario: " + e.getMessage())
            );
        }
    }
}