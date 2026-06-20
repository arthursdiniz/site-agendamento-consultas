package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Diet;
import com.clinica.agendamento.model.User;
import com.clinica.agendamento.service.DietService;
import com.clinica.agendamento.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diets")
public class DietController {

    @Autowired
    private DietService dietService;

    @Autowired
    private UserService userService;

    @GetMapping("/my")
    public ResponseEntity<?> getMyDiets(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(dietService.getDietsByClient(user.getId()));
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignDiet(@RequestBody Map<String, Object> payload, Authentication authentication) {
        User admin = userService.findByEmail(authentication.getName()).orElse(null);
        if (admin == null || !admin.getRole().name().equals("NUTRICIONISTA")) {
            return ResponseEntity.status(403).body("Acesso negado");
        }

        Long clientId = Long.parseLong(payload.get("clientId").toString());
        String description = payload.get("description").toString();

        User client = userService.findById(clientId).orElse(null);
        if (client == null) {
            return ResponseEntity.status(404).body("Cliente não encontrado");
        }
        
        Diet diet = dietService.assignDiet(client, description);
        return ResponseEntity.ok(diet);
    }
}
