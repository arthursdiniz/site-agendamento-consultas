package com.clinica.agendamento.repository;

import com.clinica.agendamento.model.Diet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DietRepository extends JpaRepository<Diet, Long> {
    List<Diet> findByClientId(Long clientId);
}
