package com.example.demo.bean;

import java.util.List;

public class Reference {
	private int reference_no;	//引用编号
	private String reference_name;	//引用名称
	private String reference_description;	//引用详情
	private String reference_constraint;
	private String reference_from;	
	private String reference_to;
	private int reference_x1;	//位置信息
	private int reference_y1;
	private int reference_x2;
	private int reference_y2;
	private List<RequirementPhenomenon> phenomenonList;	//现象列表

	public int getReference_no() {
		return reference_no;
	}
	public void setReference_no(int reference_no) {
		this.reference_no = reference_no;
	}
	public String getReference_name() {
		return reference_name;
	}
	public void setReference_name(String reference_name) {
		this.reference_name = reference_name;
	}
	public String getReference_description() {
		return reference_description;
	}
	public void setReference_description(String reference_description) {
		this.reference_description = reference_description;
	}
	public String getReference_from() {
		return reference_from;
	}
	public void setReference_from(String reference_from) {
		this.reference_from = reference_from;
	}
	public String getReference_to() {
		return reference_to;
	}
	public void setReference_to(String reference_to) {
		this.reference_to = reference_to;
	}
	public int getReference_x1() {
		return reference_x1;
	}
	public void setReference_x1(int reference_x1) {
		this.reference_x1 = reference_x1;
	}
	public int getReference_y1() {
		return reference_y1;
	}
	public void setReference_y1(int reference_y1) {
		this.reference_y1 = reference_y1;
	}
	public int getReference_x2() {
		return reference_x2;
	}
	public void setReference_x2(int reference_x2) {
		this.reference_x2 = reference_x2;
	}
	public int getReference_y2() {
		return reference_y2;
	}
	public void setReference_y2(int reference_y2) {
		this.reference_y2 = reference_y2;
	}
	public List<RequirementPhenomenon> getPhenomenonList() {
		return phenomenonList;
	}
	public void setPhenomenonList(List<RequirementPhenomenon> phenomenonList) {
		this.phenomenonList = phenomenonList;
	}
	public String getReference_constraint() {
		return reference_constraint;
	}
	public void setReference_constraint(String reference_constraint) {
		this.reference_constraint = reference_constraint;
	}
	
	@Override    
	public Object clone() {        
		Reference reference = null;        
		try {            
			reference = (Reference) super.clone();        
		} catch (CloneNotSupportedException e) {            
			e.printStackTrace();        
			}        
		return reference;    
	}
}
