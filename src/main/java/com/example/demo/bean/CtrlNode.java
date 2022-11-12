package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 控制节点
 *
 * @author yizai
 */
@Data
public class CtrlNode extends Node {
    private List<Node> node_fromList;
    private List<Node> node_toList;
    /**
     * 判断
     */
    private String node_text;
    /**
     * 分支条件
     */
    private String node_consition1;
    private String node_consition2;
    private String delay_type;
}
