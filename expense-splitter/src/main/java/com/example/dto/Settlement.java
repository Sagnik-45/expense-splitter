package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Settlement {
    private String fromName;   // "raktim"
    private String toName;     // "sagnik"
    private Double amount;     // 101.43
}