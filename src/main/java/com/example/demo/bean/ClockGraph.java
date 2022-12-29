package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ClockGraph {
	private List<Clock> clocks;
	private List<Edge> edges;

	public ClockGraph(){
		this.clocks = new ArrayList<Clock>();
		this.edges = new ArrayList<Edge>();
	}
	public List<Clock> getClocks() {
		return clocks;
	}

	public void setClocks(List<Clock> clocks) {
		this.clocks = clocks;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
}
