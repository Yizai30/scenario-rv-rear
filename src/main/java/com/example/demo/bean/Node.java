package com.example.demo.bean;

import java.util.List;

public class Node {
	private int node_no;	//节点编号
	private String node_type;	//节点类型
	private int node_x;		//位置信息
	private int node_y;
	private List<Node> node_fromList;	
	private List<Node> node_toList;
	private Phenomenon pre_condition;
	private Phenomenon post_condition;
	
	public List<Node> getNode_fromList() {
		return node_fromList;
	}
	public void setNode_fromList(List<Node> node_fromList) {
		this.node_fromList = node_fromList;
	}
	public List<Node> getNode_toList() {
		return node_toList;
	}
	public void setNode_toList(List<Node> node_toList) {
		this.node_toList = node_toList;
	}
	public int getNode_no() {
		return node_no;
	}
	public void setNode_no(int node_no) {
		this.node_no = node_no;
	}
	public String getNode_type() {
		return node_type;
	}
	public void setNode_type(String node_type) {
		this.node_type = node_type;
	}
	public int getNode_x() {
		return node_x;
	}
	public void setNode_x(int node_x) {
		this.node_x = node_x;
	}
	public int getNode_y() {
		return node_y;
	}
	public void setNode_y(int node_y) {
		this.node_y = node_y;
	}
	public Phenomenon getPre_condition() {
		return pre_condition;
	}
	public void setPre_condition(Phenomenon pre_condition) {
		this.pre_condition = pre_condition;
	}
	public Phenomenon getPost_condition() {
		return post_condition;
	}
	public void setPost_condition(Phenomenon post_condition) {
		this.post_condition = post_condition;
	}
	
}
