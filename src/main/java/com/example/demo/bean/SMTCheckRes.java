package com.example.demo.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SMTCheckRes {
    private String name;
    private List<String> res;
    private List<String> resListEnv;
    private JSONObject resListScene;

    public SMTCheckRes(){
        this.res = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRes() {
        return res;
    }

    public void setRes(List<String> res) {
        this.res = res;
    }


    public List<String> getResListEnv() {
        return resListEnv;
    }

    public void setResListEnv(List<String> resListEnv) {
        this.resListEnv = resListEnv;
    }

    public JSONObject getResListScene() {
        return resListScene;
    }

    public void setResListScene(JSONObject resListScene) {
        this.resListScene = resListScene;
    }
}
