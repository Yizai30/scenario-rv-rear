package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntClass;

public class MyOntClass {
	Integer id; // 唯一标识符id
	String name; // class名称
	String type; // class类型
	Boolean isdynamic;// 是否有状态机
	String SM_name;// 状态机名称
	ArrayList<String> states;// 状态机状态集合
	ArrayList<String> opts;// 状态机操作集合
	ArrayList<String> values; // 静态属性值
	private List<OntClass> stateMachine;	//状态机
	private List<OntClass> IOAutomata;	//输入输出状态机

	public MyOntClass() {
		this.name = null;
		this.type = null;
		this.isdynamic = false;
		this.SM_name = null;
		this.states = new ArrayList<String>();
		this.opts = new ArrayList<String>();
		this.values = new ArrayList<String>();
	}

	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

	public ArrayList<String> getValues() {
		return values;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsdynamic() {
		return isdynamic;
	}

	public void setIsdynamic(Boolean isdynamic) {
		this.isdynamic = isdynamic;
	}

	public String getSM_name() {
		return SM_name;
	}

	public void setSM_name(String sM_name) {
		SM_name = sM_name;
	}

	public ArrayList<String> getStates() {
		return states;
	}

	public void setStates(ArrayList<String> states) {
		this.states = states;
	}

	public ArrayList<String> getOpts() {
		return opts;
	}

	public void setOpts(ArrayList<String> opts) {
		this.opts = opts;
	}

	public void addOpts(ArrayList<String> opts) {
		for (String opt : opts)
			this.opts.add(opt);
	}

	public boolean hasValues() {
		if (this.values.size() == 0)
			return false;
		else
			return true;
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
	
	public List<OntClass> getStateMachine() {
		return stateMachine;
	}
	
	public List<OntClass> getIOAutomata() {
		return IOAutomata;
	}
}
