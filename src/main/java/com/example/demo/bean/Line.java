package com.example.demo.bean;

import lombok.Data;

/**
 * 边
 */
@Data
public class Line {
	private int line_no;
	private String line_type;
	private Node fromNode;
	private Node toNode;
	/**
	 * 转折点
	 */
	private String turnings;
	private String condition;
}
