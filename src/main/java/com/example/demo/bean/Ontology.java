package com.example.demo.bean;

import java.util.List;

public class Ontology {
	private List<String> forbidEvents;    // 互斥的事件对
	private List<String> excludeStates;   // 互斥的状态对
	private CCSLSet ccslSet;              // 转换得到的ccsl约束
	public List<String> getForbidEvents() {
		return forbidEvents;
	}
	public void setForbidEvents(List<String> forbidEvents) {
		this.forbidEvents = forbidEvents;
	}
	public List<String> getExcludeStates() {
		return excludeStates;
	}
	public void setExcludeStates(List<String> excludeStates) {
		this.excludeStates = excludeStates;
	}
	public CCSLSet getCcslSet() {
		return ccslSet;
	}
	public void setCcslSet(CCSLSet ccslSet) {
		this.ccslSet = ccslSet;
	}
	
}
