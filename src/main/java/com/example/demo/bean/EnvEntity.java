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
		mc = this.getOntClasses(filepath);
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
			if (o.getName().equals(name))
				break;
		}
		return o;
	}

	/**
	 * 获取环境实体子类对应的实例集合
	 * 
	 * @param filepath
	 *                 owl文件集合
	 * @param ems
	 *                 环境实体
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
						} else
							System.out.println("Class" + o.getName() + "的虚拟机个数大于1个");
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
			if (c.getLocalName() != null)
				if (c.getLocalName().equals(name)) {
					return c;
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
					if (prop.equals(property))
						res.add(values);
				}
			}
		}
		return res;
	}

	/**
	 * 当前OntClass的父类是否在ems中
	 * 
	 * @param c
	 * @param ems
	 *            环境实体集合
	 * @return
	 */
	public static String isSuperC(OntClass c) {
		// 遍历c的所有父类
		for (Iterator it = c.listSuperClasses(); it.hasNext();) {
			OntClass sp = (OntClass) it.next();
			// 父类是否在ems中
			if (ems.contains(sp.getLocalName()))
				return sp.getLocalName();
		}
		return "No";
	}

	// private void deel(DefaultMutableTreeNode dn, String name) {
	public ArrayList<String> deel(String name) {
		// LinkedList ll = this.ont.getSubClass(name);
		// for (int i = 0; i <= ll.size() - 1; i++) {
		// String n = ll.get(i).toString();
		// n = n.substring(n.indexOf("#") + 1);
		// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(n);
		// dn.add(tmp);
		// deel(tmp, ll.get(i).toString());
		// }
		ArrayList<String> re = new ArrayList<String>();
		if (name.equals("Thing")) {
			String n1 = "Environment Entity";
			// DefaultMutableTreeNode tmp1 = new DefaultMutableTreeNode(n1);
			re.add(n1);
			// deel(tmp1, n1);

			String n2 = "Attribute";
			// DefaultMutableTreeNode tmp2 = new DefaultMutableTreeNode(n2);
			re.add(n2);
			// deel(tmp2, n2);

			String n3 = "Value";
			// DefaultMutableTreeNode tmp3 = new DefaultMutableTreeNode(n3);
			re.add(n3);
			// deel(tmp3, n3);

			String n4 = "State Machine";
			// DefaultMutableTreeNode tmp4 = new DefaultMutableTreeNode(n4);
			re.add(n4);
			// deel(tmp4, n4);

			String n5 = "State";
			// DefaultMutableTreeNode tmp5 = new DefaultMutableTreeNode(n5);
			re.add(n5);
			// deel(tmp5, n5);

			String n6 = "Transition";
			// DefaultMutableTreeNode tmp6 = new DefaultMutableTreeNode(n6);
			re.add(n6);
			// deel(tmp6, n6);

			String n7 = "Event";
			// DefaultMutableTreeNode tmp7 = new DefaultMutableTreeNode(n7);
			re.add(n7);
			// deel(tmp7, n7);

		} else if (name.equals("Environment Entity")) {
			for (int i = 0; i < ems.size(); i++) {
				String n = ems.get(i);
				// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(n);
				re.add(n);
				// deel(tmp,ems.get(i));
			}
		} else if (ems.contains(name)) {
			for (int i = 0; i < mc.size(); i++) {
				MyOntClass tmp_mc = mc.get(i);
				if (tmp_mc.getType().equals(name)) {
					// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(tmp_mc.getName());
					re.add(tmp_mc.getName());

				}
			}
		} else if (name.equals("Attribute")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("Attribute")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
						}

				}
			}
		} else if (name.equals("Value")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("Value")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
						}

				}
			}
		} else if (name.equals("State Machine")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("StateMachine")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
						}

				}
				// if(c.getLocalName()!=null)
				// if(c.getLocalName().equals("State Machine"))
				// {
				// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
				// re.add(c.getLocalName());
				// }
			}
		} else if (name.equals("State")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("State")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
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

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("Transition")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
						}

				}
			}
		} else if (name.equals("Event")) {

			for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
				OntClass c = i.next();
				System.out.println(c.getLocalName());

				for (Iterator it = c.listSuperClasses(); it.hasNext();) {
					OntClass sp = (OntClass) it.next();

					if (sp.getLocalName() != null)
						if (sp.getLocalName().equals("Event")) {
							// DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(c.getLocalName());
							re.add(c.getLocalName());
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
