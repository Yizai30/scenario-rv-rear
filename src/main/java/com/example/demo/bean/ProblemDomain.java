package com.example.demo.bean;

import lombok.Data;

/**
 * 问题领域
 */
@Data
public class ProblemDomain {
	/**
	 * 领域编号
	 */
	private int problemdomain_no;
	/**
	 * 领域名称
	 */
	private String problemdomain_name;
	/**
	 * 名称缩写
	 */
	private String problemdomain_shortname;
	/**
	 * 领域类型
	 */
	private String problemdomain_type;
	/**
	 * 物理特性
	 */
	private String problemdomain_property;
	/**
	 * 位置信息
	 */
	private int problemdomain_x;
	private int problemdomain_y;	
	private int problemdomain_h;	
	private int problemdomain_w;
	private String state;

	public String getProblemdomain_property() {
		if(problemdomain_property==null) {
			problemdomain_property="GivenDomain";
		}
		return problemdomain_property;
	}
}
