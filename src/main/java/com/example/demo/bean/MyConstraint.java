package com.example.demo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MyConstraint implements  Serializable{
	private int no;	//编号（记录用）
	private boolean isReq;
	private List<Integer> sourceList;
	private String myccsl;
	private List<MyConstraint> preMyccslList;
	private List<Edge> dot ;

	public MyConstraint(String myccsl) {
		this.myccsl = myccsl;
		this.sourceList = new ArrayList<Integer>();
	}

	public MyConstraint(int index, String myccsl) {
		this.no = index;
		this.myccsl = myccsl;
//		this.req = new ArrayList<String>();
	}
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public List<Integer> getSourceList() {
		return sourceList;
	}
	public void setSourceList(List<Integer> sourceList) {
		this.sourceList = sourceList;
	}
	public String getMyccsl() {
		return myccsl;
	}
	public void setMyccsl(String myccsl) {
		this.myccsl = myccsl;
	}
	public List<MyConstraint> getPreMyccslList() {
		return preMyccslList;
	}
	public void setPreMyccslList(List<MyConstraint> preMyccslList) {
		this.preMyccslList = preMyccslList;
	}
	public List<Edge> getDot() {
		return dot;
	}
	public void setDot(List<Edge> dot) {
		this.dot = dot;
	}
	public boolean isReq() {
		return isReq;
	}
	public void setIsReq(boolean isReq) {
		this.isReq = isReq;
	}
	public boolean equal(MyConstraint myConstraint) {
		return this.myccsl.equals(myConstraint.getMyccsl());
	}
}
