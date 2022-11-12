package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CCSL 语句组的集合
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CCSLSet {
    /**
     * 编号
     */
    private String id;
    /**
     * CCSL 语句组
     */
    private List<String> ccslList;
    private String begin;
    private String end;
}
