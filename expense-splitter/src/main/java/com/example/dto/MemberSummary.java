package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberSummary {
    private Long memberId;
    private String name;
    private Double charged;   // their share of total
    private Double paid;      // what they actually paid
    private Double balance;   // paid - charged
}