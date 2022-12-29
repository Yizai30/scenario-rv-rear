package com.example.demo.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class ConsistentRes {
    private String name;
    private List<String> ccslList;
    private List<String> resListEnv;
    private List<ProjectCCSL> resListScene;
}
