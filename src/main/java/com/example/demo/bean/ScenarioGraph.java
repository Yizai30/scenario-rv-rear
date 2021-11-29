package com.example.demo.bean;

import java.util.List;

public class ScenarioGraph {
	private String title;	//文件名
	private String requirement;	//对应的需求
	private List<Node> intNodeList;	//节点列表
	private List<CtrlNode> ctrlNodeList;	//控制节点列表
	private List<Line> lineList;	//边列表
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRequirement() {
		return requirement;
	}
	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}
	public List<Node> getIntNodeList() {
		return intNodeList;
	}
	public void setIntNodeList(List<Node> intNodeList) {
		this.intNodeList = intNodeList;
	}
	public List<CtrlNode> getCtrlNodeList() {
		return ctrlNodeList;
	}
	public void setCtrlNodeList(List<CtrlNode> ctrlNodeList) {
		this.ctrlNodeList = ctrlNodeList;
	}
	public List<Line> getLineList() {
		return lineList;
	}
	public void setLineList(List<Line> lineList) {
		this.lineList = lineList;
	}
}
