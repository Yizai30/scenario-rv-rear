package com.example.demo.bean;

import lombok.Data;

import java.util.List;

@Data
public class Constraint {
	/**
	 * 约束编号
	 */
	private int constraint_no;
	/**
	 * 约束名称
	 */
	private String constraint_name;
	/**
	 * 约束详情
	 */
	private String constraint_description;
	private String constraint_constraint;	
	private String constraint_from;	
	private String constraint_to;
	/**
	 * 位置信息
	 */
	private int constraint_x1;
	private int constraint_y1;
	private int constraint_x2;
	private int constraint_y2;
	/**
	 * 现象列表
	 */
	private List<RequirementPhenomenon> phenomenonList;
	
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
