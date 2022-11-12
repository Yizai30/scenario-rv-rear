package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 情景图
 */
@Data
public class ScenarioGraph {
	/**
	 * 文件名
	 */
	private String title;
	/**
	 * 对应的需求
	 */
	private String requirement;
	/**
	 * 节点列表
	 */
	private List<Node> intNodeList;
	/**
	 * 控制节点列表
	 */
	private List<CtrlNode> ctrlNodeList;
	/**
	 * 边列表
	 */
	private List<Line> lineList;
}
