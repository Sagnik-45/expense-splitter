package com.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    private String groupName;
    private List<String> memberNames;  // ["sagnik", "natu", "raktim"]
}