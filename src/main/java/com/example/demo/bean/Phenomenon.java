package com.example.demo.bean;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 现象
 */
@Data
public class Phenomenon implements Cloneable,Serializable {
	private static final long serialVersionUID = -5997110113751940365L;
	/**
	 * 现象编号
	 */
	private int phenomenon_no;
	/**
	 * 现象名称
	 */
	private String phenomenon_name;
	/**
	 * 现象类型
	 */
	private String phenomenon_type;
	/**
	 * 发送方
	 */
	private String phenomenon_from;
	/**
	 * 接收方
	 */
	private String phenomenon_to;

	@Override    
	public Object clone() {        
		Phenomenon phenomenon = null;        
		try {            
			phenomenon = (Phenomenon) super.clone();        
		} catch (CloneNotSupportedException e) {            
			e.printStackTrace();        
			}        
		return phenomenon;    
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((phenomenon_from == null) ? 0 : phenomenon_from.hashCode());
		result = prime * result + ((phenomenon_name == null) ? 0 : phenomenon_name.hashCode());
		result = prime * result + phenomenon_no;
		result = prime * result + ((phenomenon_to == null) ? 0 : phenomenon_to.hashCode());
		result = prime * result + ((phenomenon_type == null) ? 0 : phenomenon_type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Phenomenon other = (Phenomenon) obj;
		if (phenomenon_from == null) {
			if (other.phenomenon_from != null) {
				return false;
			}
		} else if (!phenomenon_from.equals(other.phenomenon_from)) {
			return false;
		}
		if (phenomenon_name == null) {
			if (other.phenomenon_name != null) {
				return false;
			}
		} else if (!phenomenon_name.equals(other.phenomenon_name)) {
			return false;
		}
		if (phenomenon_no != other.phenomenon_no) {
			return false;
		}
		if (phenomenon_to == null) {
			if (other.phenomenon_to != null) {
				return false;
			}
		} else if (!phenomenon_to.equals(other.phenomenon_to)) {
			return false;
		}
		if (phenomenon_type == null) {
			if (other.phenomenon_type != null) {
				return false;
			}
		} else if (!phenomenon_type.equals(other.phenomenon_type)) {
			return false;
		}
		return true;
	}
	
}
