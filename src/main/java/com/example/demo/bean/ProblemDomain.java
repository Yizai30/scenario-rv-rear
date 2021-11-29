package com.example.demo.bean;

public class ProblemDomain {
	private int problemdomain_no;	//领域编号
	private String problemdomain_name;	//领域名称
	private String problemdomain_shortname;	//名称缩写
	private String problemdomain_type;	//领域类型
	private String problemdomain_property;	//物理特性
	private int problemdomain_x;	//位置信息
	private int problemdomain_y;	
	private int problemdomain_h;	
	private int problemdomain_w;
	private String state;
	
	public int getProblemdomain_no() {
		return problemdomain_no;
	}
	public void setProblemdomain_no(int problemdomain_no) {
		this.problemdomain_no = problemdomain_no;
	}
	public String getProblemdomain_name() {
		return problemdomain_name;
	}
	public void setProblemdomain_name(String problemdomain_name) {
		this.problemdomain_name = problemdomain_name;
	}
	public String getProblemdomain_shortname() {
		return problemdomain_shortname;
	}
	public void setProblemdomain_shortname(String problemdomain_shortname) {
		this.problemdomain_shortname = problemdomain_shortname;
	}
	public String getProblemdomain_type() {
		return problemdomain_type;
	}
	public void setProblemdomain_type(String problemdomain_type) {
		this.problemdomain_type = problemdomain_type;
	}
	public String getProblemdomain_property() {
		if(problemdomain_property==null) {
			problemdomain_property="GivenDomain";
		}
		return problemdomain_property;
	}
	public void setProblemdomain_property(String problemdomain_property) {
		this.problemdomain_property = problemdomain_property;
	}
	public int getProblemdomain_x() {
		return problemdomain_x;
	}
	public void setProblemdomain_x(int problemdomain_x) {
		this.problemdomain_x = problemdomain_x;
	}
	public int getProblemdomain_y() {
		return problemdomain_y;
	}
	public void setProblemdomain_y(int problemdomain_y) {
		this.problemdomain_y = problemdomain_y;
	}
	public int getProblemdomain_h() {
		return problemdomain_h;
	}
	public void setProblemdomain_h(int problemdomain_h) {
		this.problemdomain_h = problemdomain_h;
	}
	public int getProblemdomain_w() {
		return problemdomain_w;
	}
	public void setProblemdomain_w(int problemdomain_w) {
		this.problemdomain_w = problemdomain_w;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
