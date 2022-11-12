package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 上下文图
 *
 * @author yizai
 */
@Data
public class ContextDiagram {
    /**
     * 文件名
     */
    private String title;
    /**
     * 机器
     */
    private Machine machine;
    /**
     * 领域列表
     */
    private List<ProblemDomain> problemDomainList;
    /**
     * 交互列表
     */
    private List<Interface> interfaceList;
}
