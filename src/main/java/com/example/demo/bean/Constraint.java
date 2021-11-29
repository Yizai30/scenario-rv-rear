package com.example.demo.bean;

import java.util.List;

public class Constraint {
	private int constraint_no;	//约束编号
	private String constraint_name;	//约束名称
	private String constraint_description;	//约束详情
	private String constraint_constraint;	
	private String constraint_from;	
	private String constraint_to;
	private int constraint_x1;	//位置信息
	private int constraint_y1;
	private int constraint_x2;
	private int constraint_y2;
	private List<RequirementPhenomenon> phenomenonList;	//现象列表

	public int getConstraint_no() {
		return constraint_no;
	}
	public void setConstraint_no(int constraint_no) {
		this.constraint_no = constraint_no;
	}
	public String getConstraint_name() {
		return constraint_name;
	}
	public void setConstraint_name(String constraint_name) {
		this.constraint_name = constraint_name;
	}
	public String getConstraint_description() {
		return constraint_description;
	}
	public void setConstraint_description(String constraint_description) {
		this.constraint_description = constraint_description;
	}
	public String getConstraint_from() {
		return constraint_from;
	}
	public void setConstraint_from(String constraint_from) {
		this.constraint_from = constraint_from;
	}
	public String getConstraint_to() {
		return constraint_to;
	}
	public void setConstraint_to(String constraint_to) {
		this.constraint_to = constraint_to;
	}
	public int getConstraint_x1() {
		return constraint_x1;
	}
	public void setConstraint_x1(int constraint_x1) {
		this.constraint_x1 = constraint_x1;
	}
	public int getConstraint_y1() {
		return constraint_y1;
	}
	public void setConstraint_y1(int constraint_y1) {
		this.constraint_y1 = constraint_y1;
	}
	public int getConstraint_x2() {
		return constraint_x2;
	}
	public void setConstraint_x2(int constraint_x2) {
		this.constraint_x2 = constraint_x2;
	}
	public int getConstraint_y2() {
		return constraint_y2;
	}
	public void setConstraint_y2(int constraint_y2) {
		this.constraint_y2 = constraint_y2;
	}
	public List<RequirementPhenomenon> getPhenomenonList() {
		return phenomenonList;
	}
	public void setPhenomenonList(List<RequirementPhenomenon> phenomenonList) {
		this.phenomenonList = phenomenonList;
	}
	public String getConstraint_constraint() {
		return constraint_constraint;
	}
	public void setConstraint_constraint(String constraint_constraint) {
		this.constraint_constraint = constraint_constraint;
	}
	
	@Override    
	public Object clone() {        
		Constraint constraint = null;        
		try {            
			constraint = (Constraint) super.clone();        
		} catch (CloneNotSupportedException e) {            
			e.printStackTrace();        
			}        
		return constraint;    
	}
}
