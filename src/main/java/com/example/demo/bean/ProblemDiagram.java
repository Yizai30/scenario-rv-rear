package com.example.demo.bean;

import java.util.List;

public class ProblemDiagram {
	private String title;	//文件名
	private ContextDiagram contextDiagram;	//上下文图
	private List<Requirement> requirementList;	//需求列表
	private List<Constraint> constraintList;	//约束列表
	private List<Reference> referenceList;	//引用列表
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ContextDiagram getContextDiagram() {
		return contextDiagram;
	}
	public void setContextDiagram(ContextDiagram contextDiagram) {
		this.contextDiagram = contextDiagram;
	}
	public List<Requirement> getRequirementList() {
		return requirementList;
	}
	public void setRequirementList(List<Requirement> requirementList) {
		this.requirementList = requirementList;
	}
	public List<Constraint> getConstraintList() {
		return constraintList;
	}
	public void setConstraintList(List<Constraint> constraintList) {
		this.constraintList = constraintList;
	}
	public List<Reference> getReferenceList() {
		return referenceList;
	}
	public void setReferenceList(List<Reference> referenceList) {
		this.referenceList = referenceList;
	}
}
