package com.example.demo.bean;

public class DirectedLine {
	/**上游节点*/
    private String source;
    /**下游节点*/
    private String target;
    
    public DirectedLine(String source, String target) {
        this.source = source;
        this.target = target;
    }

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
