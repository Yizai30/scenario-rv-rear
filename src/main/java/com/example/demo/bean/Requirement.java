package com.example.demo.bean;

import lombok.Data;

/**
 * 需求
 */
@Data
public class Requirement {
	/**
	 * 需求编号
	 */
	private int requirement_no;
	/**
	 * 需求描述
	 */
	private String requirement_context;
	/**
	 * 位置信息
	 */
	private int requirement_x;
	private int requirement_y;
	private int requirement_h;
	private int requirement_w;
}
