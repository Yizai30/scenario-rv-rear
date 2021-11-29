package com.example.demo.bean;

import java.util.List;

public class ContextDiagram {
	private String title;	//文件名
	private Machine machine;	//机器
	private List<ProblemDomain> problemDomainList;	//领域列表
	private List<Interface> interfaceList;	//交互列表
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Machine getMachine() {
		return machine;
	}
	public void setMachine(Machine machine) {
		this.machine = machine;
	}
	public List<ProblemDomain> getProblemDomainList() {
		return problemDomainList;
	}
	public void setProblemDomainList(List<ProblemDomain> problemDomainList) {
		this.problemDomainList = problemDomainList;
	}
	public List<Interface> getInterfaceList() {
		return interfaceList;
	}
	public void setInterfaceList(List<Interface> interfaceList) {
		this.interfaceList = interfaceList;
	}
}
