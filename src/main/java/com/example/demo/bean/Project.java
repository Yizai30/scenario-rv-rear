package com.example.demo.bean;

import java.util.List;

public class Project {
	private String title;	//项目名
	private Ontology ontology;  // 环境本体
	private ContextDiagram contextDiagram;	//上下文图
	private ProblemDiagram problemDiagram;	//问题图
	private List<ScenarioGraph> scenarioGraphList; 	//子情景图
	private List<CCSLSet> ccslSetList;	//情景图对应的CCSL约束
	private CCSLSet composedCcslSet; 	//组合情景图对应的CCSL约束
	private CCSLSet simplifiedCcslSet; 	//简化情景图对应的CCSL约束
	private CCSLSet orchestrateCcslSet; 	//编排的情景图对应的CCSL约束
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Ontology getOntology() {
		return ontology;
	}
	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}
	public ContextDiagram getContextDiagram() {
		return contextDiagram;
	}
	public void setContextDiagram(ContextDiagram contextDiagram) {
		this.contextDiagram = contextDiagram;
	}
	public ProblemDiagram getProblemDiagram() {
		return problemDiagram;
	}
	public void setProblemDiagram(ProblemDiagram problemDiagram) {
		this.problemDiagram = problemDiagram;
	}
	public List<ScenarioGraph> getScenarioGraphList() {
		return scenarioGraphList;
	}
	public void setScenarioGraphList(List<ScenarioGraph> scenarioGraphList) {
		this.scenarioGraphList = scenarioGraphList;
	}
	public List<CCSLSet> getCcslSetList() {
		return ccslSetList;
	}
	public void setCcslSetList(List<CCSLSet> ccslSetList) {
		this.ccslSetList = ccslSetList;
	}
	public CCSLSet getComposedCcslSet() {
		return composedCcslSet;
	}
	public void setComposedCcslSet(CCSLSet composedCcslSet) {
		this.composedCcslSet = composedCcslSet;
	}
	public CCSLSet getSimplifiedCcslSet() {
		return simplifiedCcslSet;
	}
	public void setSimplifiedCcslSet(CCSLSet simplifiedCcslSet) {
		this.simplifiedCcslSet = simplifiedCcslSet;
	}
	public CCSLSet getOrchestrateCcslSet() {
		return orchestrateCcslSet;
	}
	public void setOrchestrateCcslSet(CCSLSet orchestrateCcslSet) {
		this.orchestrateCcslSet = orchestrateCcslSet;
	}
	
	
}
