package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 环境本体
 */
@Data
public class Ontology {
	/**
	 * 互斥的事件对
	 */
	private List<String> forbidEvents;
	/**
	 * 互斥的状态对
	 */
	private List<String> excludeStates;
	/**
	 * 转换得到的ccsl约束
	 */
	private CCSLSet ccslSet;
	
}
