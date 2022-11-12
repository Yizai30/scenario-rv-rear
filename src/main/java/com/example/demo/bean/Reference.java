package com.example.demo.bean;

import lombok.Data;

import java.util.List;

/**
 * 引用
 */
@Data
public class Reference {
	/**
	 * 引用编号
	 */
	private int reference_no;
	/**
	 * 引用名称
	 */
	private String reference_name;
	/**
	 * 引用详情
	 */
	private String reference_description;
	private String reference_constraint;
	private String reference_from;	
	private String reference_to;
	/**
	 * 位置信息
	 */
	private int reference_x1;
	private int reference_y1;
	private int reference_x2;
	private int reference_y2;
	/**
	 * 现象列表
	 */
	private List<RequirementPhenomenon> phenomenonList;
	
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
