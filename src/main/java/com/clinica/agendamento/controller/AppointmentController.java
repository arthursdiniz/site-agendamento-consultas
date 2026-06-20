package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Appointment;
import com.clinica.agendamento.model.User;
import com.clinica.agendamento.service.AppointmentService;
import com.clinica.agendamento.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Appointment>> getByDate(@PathVariable String date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDate(LocalDate.parse(date)));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(appointmentService.getAppointmentsByClient(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> schedule(@RequestBody Appointment appointment, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        
        try {
            Appointment scheduled = appointmentService.scheduleAppointment(appointment, user);
            return ResponseEntity.ok(scheduled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        try {
            appointmentService.cancelAppointment(id, user);
            return ResponseEntity.ok("Agendamento cancelado com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
