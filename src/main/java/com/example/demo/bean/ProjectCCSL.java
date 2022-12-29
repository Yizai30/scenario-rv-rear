package com.example.demo.bean;

import lombok.Data;

import java.util.List;

@Data
public class ProjectCCSL {
    private List<String> ccsl;
    private String title;
    private String fromNode;
    private String toNode;
    private String fromType;
    private String toType;
}
