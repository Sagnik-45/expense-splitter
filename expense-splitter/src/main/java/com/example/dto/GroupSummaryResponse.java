package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupSummaryResponse {
    private Long groupId;
    private String groupName;
    private LocalDate createdDate;
    private Double totalExpenses;
    private List<MemberSummary> memberSummaries;
    private List<Settlement> settlements;
}
