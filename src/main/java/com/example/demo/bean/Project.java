package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 项目
 */
@Data
public class Project {
	/**
	 * 项目名
	 */
	private String title;
	/**
	 * 环境本体
	 */
	private Ontology ontology;
	/**
	 * 上下文图
	 */
	private ContextDiagram contextDiagram;
	/**
	 * 问题图
	 */
	private ProblemDiagram problemDiagram;
	/**
	 * 子情景图
	 */
	private List<ScenarioGraph> scenarioGraphList;
	/**
	 * 情景图对应的 CCSL 约束
	 */
	private List<CCSLSet> ccslSetList;
	/**
	 * 组合情景图对应的 CCSL 约束
	 */
	private CCSLSet composedCcslSet;
	/**
	 * 简化情景图对应的 CCSL 约束
	 */
	private CCSLSet simplifiedCcslSet;
	/**
	 * 编排的情景图对应的CCSL约束
	 */
	private CCSLSet orchestrateCcslSet;
	/**
	 * 用于互斥不一致场景编排的CCSL约束 Set
	 */
	private CCSLSet inconsistentLocateCcslSet;
	/**
	 * 用于互斥不一致场景编排的CCSL约束 List
	 */
	public static List<String> theOrchestrateCcslList;
}
