package com.clinica.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "diets")
public class Diet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate creationDate;

    public Diet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
}
