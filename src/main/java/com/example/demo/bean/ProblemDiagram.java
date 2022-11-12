package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 问题图
 */
@Data
public class ProblemDiagram {
	/**
	 * 文件名
	 */
	private String title;
	/**
	 * 上下文图
	 */
	private ContextDiagram contextDiagram;
	/**
	 * 需求列表
	 */
	private List<Requirement> requirementList;
	/**
	 * 约束列表
	 */
	private List<Constraint> constraintList;
	/**
	 * 引用列表
	 */
	private List<Reference> referenceList;
}
