package com.clinica.agendamento.service;

import com.clinica.agendamento.model.Appointment;
import com.clinica.agendamento.model.AppointmentStatus;
import com.clinica.agendamento.model.User;
import com.clinica.agendamento.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }

    public List<Appointment> getAppointmentsByClient(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    public Appointment scheduleAppointment(Appointment appointment, User client) {
        // Verifica se já existe agendamento no mesmo dia e horário
        List<Appointment> existingAppointments = appointmentRepository.findByDate(appointment.getDate());
        
        boolean hasConflict = existingAppointments.stream()
                .anyMatch(a -> a.getStatus() != AppointmentStatus.CANCELADO &&
                        (appointment.getStartTime().isBefore(a.getEndTime()) && 
                         appointment.getEndTime().isAfter(a.getStartTime())));

        if (hasConflict) {
            throw new RuntimeException("Horário já está ocupado.");
        }

        appointment.setClient(client);
        appointment.setStatus(AppointmentStatus.AGENDADO);
        return appointmentRepository.save(appointment);
    }

    public void cancelAppointment(Long appointmentId, User client) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("Você só pode cancelar seus próprios agendamentos.");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELADO);
        appointmentRepository.save(appointment);
    }
}
