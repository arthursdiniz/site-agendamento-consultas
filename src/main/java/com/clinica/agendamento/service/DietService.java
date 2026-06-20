package com.clinica.agendamento.service;

import com.clinica.agendamento.model.Diet;
import com.clinica.agendamento.model.User;
import com.clinica.agendamento.repository.DietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DietService {

    @Autowired
    private DietRepository dietRepository;

    public List<Diet> getDietsByClient(Long clientId) {
        return dietRepository.findByClientId(clientId);
    }

    public Diet assignDiet(User client, String description) {
        Diet diet = new Diet();
        diet.setClient(client);
        diet.setDescription(description);
        diet.setCreationDate(LocalDate.now());
        return dietRepository.save(diet);
    }
}
