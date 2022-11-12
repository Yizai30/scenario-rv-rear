package com.example.demo.bean;

import lombok.Data;

/**
 * 机器
 */
@Data
public class Machine {
	/**
	 * 机器名称
	 */
	private String machine_name;
	/**
	 * 名称缩写
	 */
	private String machine_shortName;
	/**
	 * 位置信息
	 */
	private int machine_x;
	private int machine_y;
	private int machine_h;
	private int machine_w;
}
