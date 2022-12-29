package com.example.demo.bean;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Edge implements  Serializable{
	private Clock from;
	private Clock to;
	private String type;
	private String ccsl;
	private List<String> Req;	//编号（记录用）
	private String weight;
	private Float min;
	private Float max;
	private int min_tag;
	private int max_tag;
	
	public Float getMin() {
		return min;
	}
	public void setMin(Float min) {
		this.min = min;
	}
	public Float getMax() {
		return max;
	}
	public void setMax(Float max) {
		this.max = max;
	}
	public int getMin_tag() {
		return min_tag;
	}
	public void setMin_tag(int min_tag) {
		this.min_tag = min_tag;
	}
	public int getMax_tag() {
		return max_tag;
	}
	public void setMax_tag(int max_tag) {
		this.max_tag = max_tag;
	}
	public Clock getFrom() {
		return from;
	}
	public void setFrom(Clock from) {
		this.from = from;
	}
	public Clock getTo() {
		return to;
	}
	public void setTo(Clock to) {
		this.to = to;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean equals(Object object) {
		Edge edge = (Edge)object;
		if(this.from.equals(edge.getFrom()) && this.to.equals(edge.getTo()) && this.type.equals(edge.getType())) {
			if(this.type.equals("boundedDiff") && !this.weight.equals(edge.getWeight())) {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
	public List<String> getReq() {
		return Req;
	}
	public void setReq(List<String> req) {
		Req = req;
	}
	public String getCcsl() {
		return ccsl;
	}
	public void setCcsl(String ccsl) {
		this.ccsl = ccsl;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
}
