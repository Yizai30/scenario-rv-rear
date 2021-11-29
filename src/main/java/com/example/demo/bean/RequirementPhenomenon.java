package com.example.demo.bean;

public class RequirementPhenomenon extends Phenomenon{
	private int phenomenon_requirement;
	private String phenomenon_constraint;
	
	public int getPhenomenon_requirement() {
		return phenomenon_requirement;
	}
	public void setPhenomenon_requirement(int phenomenon_requirement) {
		this.phenomenon_requirement = phenomenon_requirement;
	}
	public String getPhenomenon_constraint() {
		return phenomenon_constraint;
	}
	public void setPhenomenon_constraint(String phenomenon_constraint) {
		this.phenomenon_constraint = phenomenon_constraint;
	}
	
}
