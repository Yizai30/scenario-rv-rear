package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 节点
 */
@Data
public class Node {
	/**
	 * 节点编号
	 */
	private int node_no;
	/**
	 * 节点类型
	 */
	private String node_type;
	/**
	 * 位置信息
	 */
	private int node_x;
	private int node_y;
	private List<Node> node_fromList;	
	private List<Node> node_toList;
	private Phenomenon pre_condition;
	private Phenomenon post_condition;
}
