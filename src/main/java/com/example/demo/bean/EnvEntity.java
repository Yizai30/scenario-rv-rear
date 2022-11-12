package com.example.demo.bean;

import java.util.ArrayList;
import java.util.Iterator;

// import javax.swing.JDialog;
// import javax.swing.JLabel;
// import javax.swing.JScrollPane;
// import javax.swing.JTree;
// import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * 环境实体
 */
public class EnvEntity {
	private String filepath;
	String filename;
	static OntModel model;
	static ArrayList<String> ems = new ArrayList<String>() {
		{
			add("CausalEntity");
			add("LexicalEntity");
			add("BidableEntity");
		}
	};
	ArrayList<MyOntClass> mc;
	ArrayList<String> mc_name;

	public EnvEntity(String fileAdd) {
		String filepath = fileAdd;
		mc = new ArrayList<MyOntClass>();
		mc = getOntClasses(filepath);
		setMcName();
	}

	private void setMcName() {
		mc_name = new ArrayList<String>();
		for (int i = 0; i < mc.size(); i++) {
			mc_name.add(mc.get(i).getName());
		}
	}

	public ArrayList<MyOntClass> getProblemDomains() {
		return mc;
	}

	public MyOntClass getProblemDomain(String name) {
		MyOntClass o = new MyOntClass();
		for (int i = 0; i < mc.size(); i++) {
			o = mc.get(i);
			if (o.getName().equals(name)) {
				break;
			}
		}
		return o;
	}

	/**
	 * 获取环境实体子类对应的实例集合
	 * 
	 * @param filepath owl文件集合
	 * @return
	 */
	public static ArrayList<MyOntClass> getOntClasses(String filepath) {
		model = ModelFactory.createOntologyModel();
		// 读取owl文件
		model.read(filepath);
		// 结果集合
		ArrayList<MyOntClass> res = new ArrayList<MyOntClass>();
		int id = 1;
		for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass c = i.next();
			// 当前c的父类类型
			String type = null;
			// 每个OntClass的id
			// 判断当前c是否是环境实体的实例
			if (!(type = isSuperC(c)).equals("No")) {
				MyOntClass o = new MyOntClass();
				o.setId(id++);
				o.setName(c.getLocalName());
				o.setType(type);
				ArrayList<String> svalue = getRestrictionValue(c, "has_static");
				for (int ha = 0; ha < svalue.size(); ha++) {
					// System.out.println(svalue.get(ha));
					o.setValues(svalue);

				}
				if (type.equals("CausalEntity") || type.equals("BidableEntity")) {
					o.addOpts(getRestrictionValue(c, "issue"));
				}

				// 只有CausalEntity有状态机
				if (type.equals("CausalEntity")) {
					// 有状态机的话，取状态机名称
					ArrayList<String> value = getRestrictionValue(c, "has_dynamic");
					if (value.size() > 0) {
						o.setIsdynamic(true);
						if (value.size() == 1) {
							o.setSM_name(value.get(0));
							// 根据状态机名称获取状态机对象
							OntClass sm = getOntClass(filepath, value.get(0));
							// 获取状态机的状态
							o.setStates(getRestrictionValue(sm, "has_state"));
							// 获取状态机的操作
							o.addOpts(getRestrictionValue(sm, "has_inout"));
						} else {
							System.out.println("Class" + o.getName() + "的虚拟机个数大于1个");
						}
						// 根据value获取对应的状态机OntClass对象
					}
				}
				res.add(o);
			}
		}
		return res;
	}

	/**
	 * 根据name值返回对应的OntClass对象
	 * 
	 * @param filepath
	 *                 owl文件的绝对路径
	 * @param name
	 *                 Class的名称
	 * @return
	 */
	public static OntClass getOntClass(String filepath, String name) {
		model = ModelFactory.createOntologyModel();
		// 读取owl文件
		model.read(filepath);
		// 遍历OntClass
		for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass c = i.next();
			if (c.getLocalName() != null) {
				if (c.getLocalName().equals(name)) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * 根据property的值，返回对应约束值
	 * 
	 * @param c
	 * @param property
	 * @return
	 */
	public static ArrayList<String> getRestrictionValue(OntClass c, String property) {
		// 结果集合
		ArrayList<String> res = new ArrayList<String>();
		// c的父类集合
		ExtendedIterator<OntClass> eitr = ((OntClass) c.as(OntClass.class)).listSuperClasses(true);
		while (eitr.hasNext()) {
			OntClass cls = eitr.next();
			// 当前父类对象是否是约束类型
			if (cls.isRestriction()) {
				// 当前父类能否转换成约束值类型
				if (cls.canAs(AllValuesFromRestriction.class)) {
					String values = ((AllValuesFromRestriction) cls.as(AllValuesFromRestriction.class))
							.getAllValuesFrom().getLocalName();
					String prop = ((AllValuesFromRestriction) cls.as(AllValuesFromRestriction.class)).getOnProperty()
							.getLocalName();
					if (prop.equals(property)) {
						res.add(values);
					}
				}
			}
		}
		return res;
	}

	/**
	 * 当前OntClass的父类是否在ems中
	 * 
	 * @param c
	 * @return
	 */
	public static String isSuperC(OntClass c) {
		// 遍历c的所有父类
		for (Iterator it = c.listSuperClasses(); it.hasNext();) {
			OntClass sp = (OntClass) it.next();
			// 父类是否在ems中
			if (ems.contains(sp.getLocalName())) {
				return sp.getLocalName();
			}
		}
		return "No";
	}

	public ArrayList<String> deel(String name) {
		ArrayList<String> re = new ArrayList<String>();
		if (name.equals("Thing")) {
			String n1 = "Environment Entity";
			re.add(n1);
			String n2 = "Attribute";
			re.add(n2);
			String n3 = "Value";
			re.add(n3);
			String n4 = "State Machine";
			re.add(n4);
			String n5 = "State";
			re.add(n5);
			String n6 = "Transition";
			re.add(n6);
			String n7 = "Event";
			re.add(n7);
		} else if (name.equals("Environment Entity")) {
			for (int i = 0; i < ems.size(); i++) {
				String n = ems.get(i);
				re.add(n);
			}
		} else if (ems.contains(name)) {
			for (int i = 0; i < mc.size(); i++) {
				MyOntClass tmp_mc = mc.get(i);
				if (tmp_mc.getType().equals(name)) {
					re.add(tmp_mc.getName());

				}
			}
		} else if (name.equals("Attribute")) {
			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("Attribute")) {
							re.add(c.getLocalName());
						}
					}
				}
			}
		} else if (name.equals("Value")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("Value")) {
							re.add(c.getLocalName());
						}
					}
				}
			}
		} else if (name.equals("State Machine")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("StateMachine")) {
							re.add(c.getLocalName());
						}
					}
				}

			}
		} else if (name.equals("State")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("State")) {
							re.add(c.getLocalName());
						}
					}
				}
			}
		}

		else if (name.equals("Transition")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("Transition")) {
							re.add(c.getLocalName());
						}
					}
				}
			}
		} else if (name.equals("Event")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null) {
						if (sp.getLocalName().equals("Event")) {
							re.add(c.getLocalName());
						}
					}
				}
			}
		}
		return re;
	}
	
	public String getFilepath() {
		return filepath;
	}

}
