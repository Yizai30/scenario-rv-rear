package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.apache.jena.ontology.OntClass;

/**
 * 状态机
 */
@Data
public class MyOntClass {
	/**
	 * 唯一标识符id
	 */
	Integer id;
	/**
	 * class 名称
	 */
	String name;
	/**
	 * class 类型
	 */
	String type;
	/**
	 * 是否有状态机
	 */
	Boolean isdynamic;
	/**
	 * 状态机名称
	 */
	String SM_name;
	/**
	 * 状态机状态集合
	 */
	ArrayList<String> states;
	/**
	 * 状态机操作集合
	 */
	ArrayList<String> opts;
	/**
	 * 静态属性值
	 */
	ArrayList<String> values;
	/**
	 * 状态机
	 */
	private List<OntClass> stateMachine;
	/**
	 * 输入输出状态机
	 */
	private List<OntClass> IOAutomata;

	public MyOntClass() {
		this.name = null;
		this.type = null;
		this.isdynamic = false;
		this.SM_name = null;
		this.states = new ArrayList<String>();
		this.opts = new ArrayList<String>();
		this.values = new ArrayList<String>();
	}

	public void addOpts(ArrayList<String> opts) {
		for (String opt : opts) {
			this.opts.add(opt);
		}
	}

	public boolean hasValues() {
		return this.values.size() != 0;
	}

	public ArrayList<String> getPhes() {
		ArrayList<String> phes = new ArrayList<String>();
		for (int i = 0; i < this.getValues().size(); i++) {
			phes.add(this.getValues().get(i));
		}
		for (int i = 0; i < this.getStates().size(); i++) {
			phes.add(this.getStates().get(i));
		}
		for (int i = 0; i < this.getOpts().size(); i++) {
			phes.add(this.getOpts().get(i));
		}
		return phes;
	}
}
