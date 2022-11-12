package com.example.demo.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 接口
 *
 * @author yizai
 */
@Data
public class Interface implements Cloneable,Serializable {
	private static final long serialVersionUID = -5789566036303249775L;
	/**
	 * 交互编号
	 */
	private int interface_no;
	/**
	 * 交互名称
	 */
	private String interface_name;
	/**
	 * 交互内容
	 */
	private String interface_description;
	private String interface_from;
	private String interface_to;
	/**
	 * 现象列表
	 */
	private List<Phenomenon> phenomenonList;
	/**
	 * 位置信息
	 */
	private int interface_x1;
	private int interface_y1;
	private int interface_x2;
	private int interface_y2;

	@Override    
	public Object clone() {        
		Interface inte = null;        
		try {            
			inte = (Interface) super.clone();        
		} catch (CloneNotSupportedException e) {            
			e.printStackTrace();        
			}        
		return inte;    
	}
}
