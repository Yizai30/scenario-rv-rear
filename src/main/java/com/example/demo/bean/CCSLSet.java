package com.example.demo.bean;

import java.util.List;

public class CCSLSet {
	private String id;
	private List<String> ccslList;
	private String begin;
	private String end;
	
	public CCSLSet(String id, List<String> ccslList, String begin, String end) {
		this.id = id;
		this.ccslList = ccslList;
		this.begin = begin;
		this.end = end;
	}
	public CCSLSet() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getCcslList() {
		return ccslList;
	}
	public void setCcslList(List<String> ccslList) {
		this.ccslList = ccslList;
	}
	public String getBegin() {
		return begin;
	}
	public void setBegin(String begin) {
		this.begin = begin;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	
}
