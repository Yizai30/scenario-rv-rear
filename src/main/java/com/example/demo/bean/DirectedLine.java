package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 直接相连两个节点的边
 *
 * @author yizai
 */
@AllArgsConstructor
@Data
public class DirectedLine {
    /**
     * 上游节点
     */
    private String source;
    /**
     * 下游节点
     */
    private String target;
}
