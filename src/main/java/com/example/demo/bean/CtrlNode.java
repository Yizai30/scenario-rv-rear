package com.example.demo.bean;

import java.util.List;

public class CtrlNode extends Node{
	private List<Node> node_fromList;	
	private List<Node> node_toList;
	private String node_text;	//判断
	private String node_consition1;	//分支条件
	private String node_consition2;
	private String delay_type;
	
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
	public String getNode_text() {
		return node_text;
	}
	public void setNode_text(String node_text) {
		this.node_text = node_text;
	}
	public String getNode_consition1() {
		return node_consition1;
	}
	public void setNode_consition1(String node_consition1) {
		this.node_consition1 = node_consition1;
	}
	public String getNode_consition2() {
		return node_consition2;
	}
	public void setNode_consition2(String node_consition2) {
		this.node_consition2 = node_consition2;
	}
	public String getDelay_type() {
		return delay_type;
	}
	public void setDelay_type(String delay_type) {
		this.delay_type = delay_type;
	}
	
	
}
