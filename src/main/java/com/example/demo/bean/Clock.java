package com.example.demo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Clock implements  Serializable{
	private String clockName;
	private int state;	//初始0，1只有进路，2只有出路，3有进有出
	private List<Edge> fromList;
	private List<Edge> toList;
	private int from_num;
	private int to_num;
	
	public Clock(String name) {
		this.clockName = name;
		this.state = 0;
		this.fromList = new ArrayList<Edge>();
		this.toList = new ArrayList<Edge>();
		this.from_num = 0;
		this.to_num = 0;
	}

	public List<Edge> getFromList() {
		return fromList;
	}

	public void setFromList(List<Edge> fromList) {
		this.fromList = fromList;
	}

	public List<Edge> getToList() {
		return toList;
	}

	public void setToList(List<Edge> toList) {
		this.toList = toList;
	}

	public String getClockName() {
		return clockName;
	}

	public void setClockName(String clockName) {
		this.clockName = clockName;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public boolean equals(Object object) {
		Clock clock = (Clock)object;
		if (this.clockName.equals(clock.getClockName())) {
			return true;
		}else {
			return false;
		}
	}

	public int getFrom_num() {
		return from_num;
	}

	public void setFrom_num(int from_num) {
		this.from_num = from_num;
	}

	public int getTo_num() {
		return to_num;
	}

	public void setTo_num(int to_num) {
		this.to_num = to_num;
	}
}
