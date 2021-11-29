package com.example.demo.bean;

public class Line {
	private int line_no;
	private String line_type;
	private Node fromNode;
	private Node toNode;
	private String turnings;	//转折点
	private String condition;

	public int getLine_no() {
		return line_no;
	}
	public void setLine_no(int line_no) {
		this.line_no = line_no;
	}
	public String getLine_type() {
		return line_type;
	}
	public void setLine_type(String line_type) {
		this.line_type = line_type;
	}
	public Node getFromNode() {
		return fromNode;
	}
	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}
	public Node getToNode() {
		return toNode;
	}
	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}
	public String getTurnings() {
		return turnings;
	}
	public void setTurnings(String turnings) {
		this.turnings = turnings;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
}
