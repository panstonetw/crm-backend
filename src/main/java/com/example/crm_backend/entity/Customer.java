package com.example.crm_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String taxId;
    private String owner;
    private String contactPerson;
    private String address;
}
