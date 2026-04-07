package com.tuapp.grocery_backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private List<Map<String, Object>> pedidos = new ArrayList<>();

    @GetMapping
    public List<Map<String, Object>> obtenerPedidos() {
        return pedidos;
    }

    @PostMapping
    public Map<String, Object> crearPedido(@RequestBody Map<String, Object> pedido) {

        pedido.put("id", pedidos.size() + 1);
        pedidos.add(pedido);

        return pedido;
    }
}