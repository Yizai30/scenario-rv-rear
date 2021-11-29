package com.example.demo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.bean.CCSLSet;
import com.example.demo.bean.Constraint;
import com.example.demo.bean.ContextDiagram;
import com.example.demo.bean.CtrlNode;
import com.example.demo.bean.EnvEntity;
import com.example.demo.bean.Interface;
import com.example.demo.bean.Line;
import com.example.demo.bean.Machine;
import com.example.demo.bean.MyOntClass;
import com.example.demo.bean.Node;
import com.example.demo.bean.Ontology;
import com.example.demo.bean.Phenomenon;
import com.example.demo.bean.ProblemDiagram;
import com.example.demo.bean.ProblemDomain;
import com.example.demo.bean.Project;
import com.example.demo.bean.Reference;
import com.example.demo.bean.Requirement;
import com.example.demo.bean.RequirementPhenomenon;
import com.example.demo.bean.ScenarioGraph;
import com.example.demo.bean.VersionInfo;

@Service
public class FileService {
	// ==================查找项目（owl）及版本====================
	public List<String> searchProject(String userAdd) {
		return searchbranch(userAdd);
	}
	
	public List<String> searchbranch(String userAdd) {
		Map<String, String> dicLits = new HashMap<String, String>();
		try {
			dicLits = GitUtil.gitAllBranch(userAdd);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		List<String> projectNames = new ArrayList<String>();
		// key
		for (String key : dicLits.keySet()) {
			if (key.equals("master")) {
				continue;
			}
			projectNames.add(key);
		}
		return projectNames;
	}
	
	// ===================创建版本库=================
	// 创建项目、版本库及分支
	public boolean setProject(String proAddress, String branch) {
		boolean flag = set(proAddress, branch);
		return flag;
	}
	
	public boolean set(String userAdd, String branch) {
		boolean res = false;
		File file = new File(userAdd);
		if (!file.exists()) {
			try {
				// 创建版本库
				Repository repository = GitUtil.createRepository(userAdd);
				// commit
				GitUtil.RecordUploadProjAt("set", userAdd, userAdd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if (!GitUtil.branchNameExist(branch, userAdd)) {	// 创建分支
				GitUtil.createBranch(branch, userAdd);
			}
			GitUtil.gitCheckout(branch, userAdd);  // 切换分支
			GitUtil.currentBranch(userAdd);  // 返回userAdd目录下的分支
			res = true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return res;
	}
	
	public void addFile(MultipartFile file, String userAdd, String branch) {
		try {
			GitUtil.gitCheckout(branch, userAdd);  // 切换分支
			GitUtil.currentBranch(userAdd);   // 返回userAdd目录下的分支
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 保存文件
		saveUploadFile(file, userAdd);

		try {
			GitUtil.RecordUploadProjAt("addfile", userAdd, ".");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}// 执行git add，git commit命令
	}
	
	// 保存xml文件到userAdd
	public void saveUploadFile(MultipartFile mf, String userAdd) {
		String filePath = userAdd + mf.getOriginalFilename();
		File file = new File(filePath);
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			mf.transferTo(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 解析owl文件得到环境本体对象
	public Ontology getOntology(String fileName, String userAdd) {
		Ontology ontology = new Ontology();
		List<String> forbidEvents = new ArrayList<String>();    // 互斥的事件对
		List<String> excludeStates = new ArrayList<String>();   // 互斥的状态对
		String path = userAdd + fileName + ".owl";
		EnvEntity envEntity = new EnvEntity(path);
		// 获取环境实体子类对应的实例集合
		@SuppressWarnings("static-access")
		ArrayList<MyOntClass> EntityClass = envEntity.getOntClasses(path);
		for(MyOntClass entityClass: EntityClass) {
			// 如果实例中包含状态机，获取状态机的状态集合和操作集合
			if(entityClass.getIsdynamic()) {
				ArrayList<String> opts = entityClass.getOpts();   // 状态机操作集合
				ArrayList<String> states = entityClass.getStates();  // 状态机状态集合
				String forbidStr = "";
				String excludeStr = "";
				if(opts.size() > 1) {
					for(int i = 0; i < opts.size(); i++) {
						String option = opts.get(i);
						if(i == 0) {
							forbidStr += option;
						} else {
							forbidStr += " forbid " + option;
						}
					}
				}
				if(states.size() > 1) {
					for(int i = 0; i < states.size(); i++) {
						String state = states.get(i);
						if(i == 0) {
							excludeStr += state;
						} else {
							excludeStr += " exclude " + state;
						}
					}
				}
				if(!forbidStr.equals("")) {
					forbidEvents.add(forbidStr);
				}
				if(!excludeStr.equals("")) {
					excludeStates.add(excludeStr);
				}
			}
		}
		CCSLSet ccslSet = ontologyToCCSL(forbidEvents, excludeStates);
		ontology.setForbidEvents(forbidEvents);
		ontology.setExcludeStates(excludeStates);
		ontology.setCcslSet(ccslSet);
		return ontology;
	}
	
	// 将环境本体中的事件对和状态对转换成CCSL约束
	private CCSLSet ontologyToCCSL(List<String> forbidEvents, List<String> excludeStates) {
		CCSLSet ccslSet = new CCSLSet();
		List<String> ccslList = new ArrayList<String>();
		// 将互斥事件对转换成CCSL约束
		for(String forbidStr: forbidEvents) {
			String[] events = forbidStr.split(" forbid ");
//			System.out.println(events.length);
			if(events.length == 2) {
				String event1 = events[0];
				String event2 = events[1];
				ccslList.add(event1 + "#" + event2);
			}
		}
		// 将互斥状态对转换成CCSL约束
		int stateNum = 1;
		for(String excludeStr: excludeStates) {
			String[] states = excludeStr.split(" exclude ");
			if(states.length == 2) {
				String state1 = states[0];
				String state2 = states[1];
				String state1S = state1 + ".s";
				String state1F = state1 + ".f";
				String state2S = state2 + ".s";
				String state2F = state2 + ".f";
				String stateS = "State" + stateNum + ".s";
				String stateF = "State" + stateNum + ".f";
				ccslList.add(stateS + "=" + state1S + "+" + state2S);
				ccslList.add(stateF + "=" + state1F + "+" + state2F);
				ccslList.add(state1S + "#" + state1F);
				ccslList.add(state2S + "#" + state2F);
				ccslList.add(stateS + "~" + stateF);
				ccslList.add(state1S + "~" + state1F);
				ccslList.add(state2S + "~"+ state2F);
				stateNum++;
			}
		}
		ccslSet.setId("OntologyCCSL");
		ccslSet.setCcslList(ccslList);
		return ccslSet;
	}

	// 解析xml文件得到顺序图对象
	public Project getScenarioDiagrams(String userAdd) {
		Project project = new Project();
		SAXReader saxReader = new SAXReader();
		try {
			File xmlFile = new File(userAdd + "Project.xml");
			Document document = saxReader.read(xmlFile);
			
			Element projectElement = document.getRootElement();
			Element titleElement = projectElement.element("title");
			Element fileListElement = projectElement.element("fileList");
			Element contextDiagramElement = fileListElement.element("ContextDiagram");
			Element problemDiagramElement = fileListElement.element("ProblemDiagram");
			Element senarioGraphListElement = fileListElement.element("SenarioGraphList");
			
			String title = titleElement.getText();
			String contextDiagramName = contextDiagramElement.getText();
			String problemDiagramName = problemDiagramElement.getText();
			ContextDiagram contextDiagram = getContextDiagram(userAdd, contextDiagramName);
			ProblemDiagram problemDiagram = getProblemDiagram(userAdd, problemDiagramName);

			project.setTitle(title);
			project.setContextDiagram(contextDiagram);
			project.setProblemDiagram(problemDiagram);
			
			if(senarioGraphListElement != null) {
				List<?> senarioGraphElementList = senarioGraphListElement.elements("SenarioGraph");
				List<ScenarioGraph> scenarioGraphList = getScenarioGraphList(userAdd, senarioGraphElementList, "haveRequirement");
//				scenarioGraphList = updateScenarioGraphList(scenarioGraphList);
				project.setScenarioGraphList(scenarioGraphList);
			} else {
				List<ScenarioGraph> sgList = new ArrayList<ScenarioGraph>();
				project.setScenarioGraphList(sgList);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return project;
	}
	
	private List<ScenarioGraph> updateScenarioGraphList(List<ScenarioGraph> scenarioGraphList){
		for(ScenarioGraph scenarioGraph: scenarioGraphList) {
			List<Node> intNodeList = scenarioGraph.getIntNodeList();
			for(Node intNode: intNodeList) {
				List<Node> intFromNodeList = getIntFromNodeList(intNode, scenarioGraph);
				List<Node> intToNodeList = getIntToNodeList(intNode, scenarioGraph);
			}
		}
		return scenarioGraphList;
	}
	
	private ContextDiagram getContextDiagram(String userAdd, String contextDiagramName) {

		ContextDiagram contextDiagram = new ContextDiagram();

		System.out.println(userAdd + contextDiagramName + ".xml");
		SAXReader saxReader = new SAXReader();
		try {
			File contextDiagramFile = new File(userAdd + contextDiagramName + ".xml");
			if (!contextDiagramFile.exists()) {
				System.out.println("文件不存在");
				return null;
			}
			Document document = saxReader.read(contextDiagramFile);

			Element contextDiagramElement = document.getRootElement();
			Element titleElement = contextDiagramElement.element("title");
			Element machineElement = contextDiagramElement.element("Machine");
			Element problemDomainListElement = contextDiagramElement.element("ProblemDomain");
			Element interfaceListElement = contextDiagramElement.element("Interface");

			String title = titleElement.getText();
			Machine machine = getMachine(machineElement);
			List<ProblemDomain> problemDomainList = getProblemDomainList(problemDomainListElement);
			List<Interface> interfaceList = getInterfaceList(interfaceListElement);

			contextDiagram.setTitle(title);
			contextDiagram.setMachine(machine);
			contextDiagram.setProblemDomainList(problemDomainList);
			contextDiagram.setInterfaceList(interfaceList);
		} catch (DocumentException e) {

			e.printStackTrace();
		}
		return contextDiagram;
	}

	private ProblemDiagram getProblemDiagram(String userAdd, String problemDiagramName) {

		ProblemDiagram problemDiagram = new ProblemDiagram();
		SAXReader saxReader = new SAXReader();
		try {
			File problemDiagramFile = new File(userAdd + problemDiagramName + ".xml");
			if (!problemDiagramFile.exists()) {
				return null;
			}
			Document document = saxReader.read(problemDiagramFile);

			Element problemDiagramElement = document.getRootElement();
			Element titleElement = problemDiagramElement.element("title");
			Element contextDiagramElement = problemDiagramElement.element("ContextDiagram");
			Element requirementListElement = problemDiagramElement.element("Requirement");
			Element constraintListElement = problemDiagramElement.element("Constraint");
			Element referenceListElement = problemDiagramElement.element("Reference");

			String title = titleElement.getText();
			String contextDiagramName = contextDiagramElement.getText();
			ContextDiagram contextDiagram = getContextDiagram(userAdd, contextDiagramName);
			List<Requirement> requirementList = getRequirementList(requirementListElement);
			List<Constraint> constraintList = getConstraintList(constraintListElement);
			List<Reference> referenceList = getReferenceList(referenceListElement);

			problemDiagram.setTitle(title);
			problemDiagram.setContextDiagram(contextDiagram);
			problemDiagram.setRequirementList(requirementList);
			problemDiagram.setConstraintList(constraintList);
			problemDiagram.setReferenceList(referenceList);
		} catch (DocumentException e) {

			e.printStackTrace();
		}
		return problemDiagram;
	}
	
	private List<ScenarioGraph> getScenarioGraphList(String address, List<?> senarioGraphElementList, String requirementFlag) {
		List<ScenarioGraph> scenarioGraphList = new ArrayList<ScenarioGraph> ();
		for(Object sge : senarioGraphElementList) {
			Element sgEle = (Element) sge;
			String senarioGraphName = sgEle.getText();
			SAXReader saxReader = new SAXReader();
			try {
//				File senarioGraphFile = new File(rootAddress + projectAddress + "\\"+ version + "\\" + senarioGraphName + ".xml");
				File senarioGraphFile = new File(address + senarioGraphName + ".xml");
				if(!senarioGraphFile.exists()) {
					return null;
				}
				Document document = saxReader.read(senarioGraphFile);
				
				Element senarioGraphElement = document.getRootElement();
				Element titleElement = senarioGraphElement.element("title");
				Element requirementElement = null;
				if(requirementFlag != null) {
					requirementElement = senarioGraphElement.element("Requirement");
				}
				
				Element nodeListElement = senarioGraphElement.element("NodeList");
				Element lineListElement = senarioGraphElement.element("LineList");
				Element intNodeListElement = nodeListElement.element("IntNode");
				Element ctrlNodeListElement = nodeListElement.element("ControlNode");
				
				String title = titleElement.getText();
				String requirement = null;
				if(requirementElement != null) {
					requirement = requirementElement.getText().replaceAll("&#x000A", "\n");
				}
				List<Node> intNodeList = getIntNodeList(intNodeListElement);
				List<CtrlNode> controlNodeList = getControlNodeList(ctrlNodeListElement);
				List<Line> lineList = getLineList(lineListElement, address, senarioGraphElementList);
				
				ScenarioGraph scenarioGraph = new ScenarioGraph();
				scenarioGraph.setTitle(title);
				scenarioGraph.setRequirement(requirement);
				scenarioGraph.setIntNodeList(intNodeList);
				scenarioGraph.setCtrlNodeList(controlNodeList);
				scenarioGraph.setLineList(lineList);
				
				scenarioGraphList.add(scenarioGraph);
			}catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		return scenarioGraphList;
	}
	
	private Machine getMachine(Element machineElement) {
		// if(machineElement==null)
		// return null;
		Machine machine = new Machine();
		String machine_name = machineElement.attributeValue("machine_name");
		// if(machine_name!=null)
		machine_name = machine_name.replaceAll("&#x000A", "\n");
		String machine_shortName = machineElement.attributeValue("machine_shortname");
		String machine_locality = machineElement.attributeValue("machine_locality");
		String[] locality = machine_locality.split(",");
		int machine_x = Integer.parseInt(locality[0]);
		int machine_y = Integer.parseInt(locality[1]);
		int machine_h = Integer.parseInt(locality[2]);
		int machine_w = Integer.parseInt(locality[3]);

		machine.setMachine_name(machine_name);
		machine.setMachine_shortName(machine_shortName);
		machine.setMachine_h(machine_h);
		machine.setMachine_w(machine_w);
		machine.setMachine_x(machine_x);
		machine.setMachine_y(machine_y);

		return machine;
	}
	
	private List<ProblemDomain> getProblemDomainList(Element problemDomainListElement) {

		List<ProblemDomain> problemDomainList = new ArrayList<ProblemDomain>();

		Element givenDomainListElement = problemDomainListElement.element("GivenDomain");
		Element designDomainListElement = problemDomainListElement.element("DesignDomain");
		List<?> givenDomainElementList = givenDomainListElement.elements("Element");
		List<?> designDomainElementList = designDomainListElement.elements("Element");

		for (Object object : givenDomainElementList) {
			ProblemDomain problemDomain = new ProblemDomain();
			Element givenDomainElement = (Element) object;

			int problemdomain_no = Integer.parseInt(givenDomainElement.attributeValue("problemdomain_no"));
			String problemdomain_name = givenDomainElement.attributeValue("problemdomain_name");
			problemdomain_name = problemdomain_name.replaceAll("&#x000A", "\n");
			String problemdomain_shortname = givenDomainElement.attributeValue("problemdomain_shortname");
			String problemdomain_type = givenDomainElement.attributeValue("problemdomain_type");
			String problemdomain_property = "GivenDomain";
			String problemdomain_locality = givenDomainElement.attributeValue("problemdomain_locality");
			String[] locality = problemdomain_locality.split(",");
			int problemdomain_x = Integer.parseInt(locality[0]);
			int problemdomain_y = Integer.parseInt(locality[1]);
			int problemdomain_h = Integer.parseInt(locality[2]);
			int problemdomain_w = Integer.parseInt(locality[3]);

			problemDomain.setProblemdomain_no(problemdomain_no);
			problemDomain.setProblemdomain_name(problemdomain_name);
			problemDomain.setProblemdomain_shortname(problemdomain_shortname);
			problemDomain.setProblemdomain_type(problemdomain_type);
			problemDomain.setProblemdomain_property(problemdomain_property);
			problemDomain.setProblemdomain_x(problemdomain_x);
			problemDomain.setProblemdomain_y(problemdomain_y);
			problemDomain.setProblemdomain_h(problemdomain_h);
			problemDomain.setProblemdomain_w(problemdomain_w);

			problemDomainList.add(problemDomain);
		}
		for (Object object : designDomainElementList) {
			Element designDomainElement = (Element) object;

			int problemdomain_no = Integer.parseInt(designDomainElement.attributeValue("problemdomain_no"));
			String problemdomain_name = designDomainElement.attributeValue("problemdomain_name");
			String problemdomain_shortname = designDomainElement.attributeValue("problemdomain_shortname");
			String problemdomain_type = designDomainElement.attributeValue("problemdomain_type");
			String problemdomain_property = "DesignDomain";
			String problemdomain_locality = designDomainElement.attributeValue("problemdomain_locality");
			String[] locality = problemdomain_locality.split(",");
			int problemdomain_x = Integer.parseInt(locality[0]);
			int problemdomain_y = Integer.parseInt(locality[1]);
			int problemdomain_h = Integer.parseInt(locality[2]);
			int problemdomain_w = Integer.parseInt(locality[3]);

			ProblemDomain problemDomain = new ProblemDomain();
			problemDomain.setProblemdomain_no(problemdomain_no);
			problemDomain.setProblemdomain_name(problemdomain_name);
			problemDomain.setProblemdomain_shortname(problemdomain_shortname);
			problemDomain.setProblemdomain_type(problemdomain_type);
			problemDomain.setProblemdomain_property(problemdomain_property);
			problemDomain.setProblemdomain_x(problemdomain_x);
			problemDomain.setProblemdomain_y(problemdomain_y);
			problemDomain.setProblemdomain_h(problemdomain_h);
			problemDomain.setProblemdomain_w(problemdomain_w);

			problemDomainList.add(problemDomain);
		}

		return problemDomainList;
	}

	private List<Interface> getInterfaceList(Element interfaceListElement) {

		List<Interface> interfaceList = new ArrayList<Interface>();
		List<?> interfaceElementList = interfaceListElement.elements("Element");

		for (Object object : interfaceElementList) {
			Element interfaceElement = (Element) object;
			List<?> phenomenonElementList = interfaceElement.elements("Phenomenon");

			int interface_no = Integer.parseInt(interfaceElement.attributeValue("interface_no"));
			String interface_name = interfaceElement.attributeValue("interface_name");
			String interface_description = interfaceElement.attributeValue("interface_description");
			String interface_from = interfaceElement.attributeValue("interface_from").replaceAll("&#x000A", "\n");
			;
			String interface_to = interfaceElement.attributeValue("interface_to").replaceAll("&#x000A", "\n");
			;
			String interface_locality = interfaceElement.attributeValue("interface_locality");
			List<Phenomenon> phenomenonList = getPhenomenonList(phenomenonElementList);
			String[] locality = interface_locality.split(",");
			int interface_x1 = Integer.parseInt(locality[0]);
			int interface_x2 = Integer.parseInt(locality[1]);
			int interface_y1 = Integer.parseInt(locality[2]);
			int interface_y2 = Integer.parseInt(locality[3]);

			Interface inte = new Interface();
			inte.setInterface_no(interface_no);
			inte.setInterface_name(interface_name);
			inte.setInterface_description(interface_description);
			inte.setInterface_from(interface_from);
			inte.setInterface_to(interface_to);
			inte.setPhenomenonList(phenomenonList);
			inte.setInterface_x1(interface_x1);
			inte.setInterface_y1(interface_y1);
			inte.setInterface_x2(interface_x2);
			inte.setInterface_y2(interface_y2);

			interfaceList.add(inte);
		}

		return interfaceList;
	}
	
	public List<Phenomenon> getPhenomenonList(List<?> phenomenonElementList) {

		List<Phenomenon> phenomenonList = new ArrayList<Phenomenon>();
		for (Object object : phenomenonElementList) {
			Element phenomenonElement = (Element) object;

			int phenomenon_no = Integer.parseInt(phenomenonElement.attributeValue("phenomenon_no"));
			String phenomenon_name = phenomenonElement.attributeValue("phenomenon_name");
			String phenomenon_type = phenomenonElement.attributeValue("phenomenon_type");
			String phenomenon_from = phenomenonElement.attributeValue("phenomenon_from").replaceAll("&#x000A", "\n");
			;
			String phenomenon_to = phenomenonElement.attributeValue("phenomenon_to").replaceAll("&#x000A", "\n");
			;

			Phenomenon phenomenon = new Phenomenon();
			phenomenon.setPhenomenon_no(phenomenon_no);
			phenomenon.setPhenomenon_name(phenomenon_name);
			phenomenon.setPhenomenon_type(phenomenon_type);
			phenomenon.setPhenomenon_from(phenomenon_from);
			phenomenon.setPhenomenon_to(phenomenon_to);

			phenomenonList.add(phenomenon);
		}
		return phenomenonList;
	}

	private List<Requirement> getRequirementList(Element requirementListElement) {

		List<?> requirementElementList = requirementListElement.elements("Element");
		List<Requirement> requirementList = new ArrayList<Requirement>();
		for (Object object : requirementElementList) {
			Element requirementElement = (Element) object;

			Requirement requirement = getRequirement(requirementElement);

			requirementList.add(requirement);
		}
		return requirementList;
	}

	private Requirement getRequirement(Element requirementElement) {

		Requirement requirement = new Requirement();

		int requirement_no = Integer.parseInt(requirementElement.attributeValue("requirement_no"));
		String requirement_context = requirementElement.attributeValue("requirement_context").replaceAll("&#x000A",
				"\n");
		;
		String requirement_locality = requirementElement.attributeValue("requirement_locality");
		String[] locality = requirement_locality.split(",");
		int requirement_x = Integer.parseInt(locality[0]);
		int requirement_y = Integer.parseInt(locality[1]);
		int requirement_h = Integer.parseInt(locality[2]);
		int requirement_w = Integer.parseInt(locality[3]);

		requirement.setRequirement_no(requirement_no);
		requirement.setRequirement_context(requirement_context);
		requirement.setRequirement_x(requirement_x);
		requirement.setRequirement_y(requirement_y);
		requirement.setRequirement_h(requirement_h);
		requirement.setRequirement_w(requirement_w);

		return requirement;
	}

	public List<Constraint> getConstraintList(Element constraintListElement) {

		List<Constraint> constraintList = new ArrayList<Constraint>();
		List<?> constraintElementList = constraintListElement.elements("Element");
		for (Object object : constraintElementList) {
			Element constraintElement = (Element) object;
			List<?> phenomenonElementList = constraintElement.elements("Phenomenon");

			int constraint_no = Integer.parseInt(constraintElement.attributeValue("constraint_no"));
			String constraint_name = constraintElement.attributeValue("constraint_name");
			String constraint_description = constraintElement.attributeValue("constraint_description");
			String constraint_from = constraintElement.attributeValue("constraint_from").replaceAll("&#x000A", "\n");
			;
			String constraint_to = constraintElement.attributeValue("constraint_to").replaceAll("&#x000A", "\n");
			;
			List<RequirementPhenomenon> phenomenonList = getRequirementPhenomenonList(phenomenonElementList);
			String constraint_locality = constraintElement.attributeValue("constraint_locality");
			String[] locality = constraint_locality.split(",");
			int constraint_x1 = Integer.parseInt(locality[0]);
			int constraint_x2 = Integer.parseInt(locality[1]);
			int constraint_y1 = Integer.parseInt(locality[2]);
			int constraint_y2 = Integer.parseInt(locality[3]);

			Constraint constraint = new Constraint();
			constraint.setConstraint_no(constraint_no);
			constraint.setConstraint_name(constraint_name);
			constraint.setConstraint_description(constraint_description);
			constraint.setConstraint_from(constraint_from);
			constraint.setConstraint_to(constraint_to);
			constraint.setPhenomenonList(phenomenonList);
			constraint.setConstraint_x1(constraint_x1);
			constraint.setConstraint_x2(constraint_x2);
			constraint.setConstraint_y1(constraint_y1);
			constraint.setConstraint_y2(constraint_y2);

			constraintList.add(constraint);
		}
		return constraintList;
	}
	
	public List<Reference> getReferenceList(Element referenceListElement) {
		List<Reference> referenceList = new ArrayList<Reference>();
		List<?> referenceElementList = referenceListElement.elements("Element");
		for (Object object : referenceElementList) {
			Element referenceElement = (Element) object;
			List<?> phenomenonElementList = referenceElement.elements("Phenomenon");

			int reference_no = Integer.parseInt(referenceElement.attributeValue("reference_no"));
			String reference_name = referenceElement.attributeValue("reference_name");
			String reference_description = referenceElement.attributeValue("reference_description");
			String reference_from = referenceElement.attributeValue("reference_from").replaceAll("&#x000A", "\n");
			;
			String reference_to = referenceElement.attributeValue("reference_to").replaceAll("&#x000A", "\n");
			;
			List<RequirementPhenomenon> phenomenonList = getRequirementPhenomenonList(phenomenonElementList);
			String reference_locality = referenceElement.attributeValue("reference_locality");
			String[] locality = reference_locality.split(",");
			int reference_x1 = Integer.parseInt(locality[0]);
			int reference_x2 = Integer.parseInt(locality[1]);
			int reference_y1 = Integer.parseInt(locality[2]);
			int reference_y2 = Integer.parseInt(locality[3]);

			Reference reference = new Reference();
			reference.setReference_no(reference_no);
			reference.setReference_name(reference_name);
			reference.setReference_description(reference_description);
			reference.setReference_from(reference_from);
			reference.setReference_to(reference_to);
			reference.setPhenomenonList(phenomenonList);
			reference.setReference_x1(reference_x1);
			reference.setReference_x2(reference_x2);
			reference.setReference_y1(reference_y1);
			reference.setReference_y2(reference_y2);

			referenceList.add(reference);
		}
		return referenceList;
	}

	private List<RequirementPhenomenon> getRequirementPhenomenonList(List<?> phenomenonElementList) {

		List<RequirementPhenomenon> phenomenonList = new ArrayList<RequirementPhenomenon>();
		for (Object object : phenomenonElementList) {
			Element phenomenonElement = (Element) object;

			int phenomenon_no = Integer.parseInt(phenomenonElement.attributeValue("phenomenon_no"));
			String phenomenon_name = phenomenonElement.attributeValue("phenomenon_name");
			String phenomenon_type = phenomenonElement.attributeValue("phenomenon_type");
			String phenomenon_from = phenomenonElement.attributeValue("phenomenon_from").replaceAll("&#x000A", "\n");
			;
			String phenomenon_to = phenomenonElement.attributeValue("phenomenon_to").replaceAll("&#x000A", "\n");
			;
			String phenomenon_constraint = phenomenonElement.attributeValue("phenomenon_constraint");
			int phenomenon_requirement = Integer.parseInt(phenomenonElement.attributeValue("phenomenon_requirement"));

			RequirementPhenomenon phenomenon = new RequirementPhenomenon();
			phenomenon.setPhenomenon_no(phenomenon_no);
			phenomenon.setPhenomenon_name(phenomenon_name);
			phenomenon.setPhenomenon_type(phenomenon_type);
			phenomenon.setPhenomenon_constraint(phenomenon_constraint);
			phenomenon.setPhenomenon_requirement(phenomenon_requirement);
			phenomenon.setPhenomenon_from(phenomenon_from);
			phenomenon.setPhenomenon_to(phenomenon_to);

			phenomenonList.add(phenomenon);
		}
		return phenomenonList;
	}
	
	private List<Node> getIntNodeList(Element intNodeListElement) {
		List<Node> intNodeList = new ArrayList<Node>();
		
		Element behIntNodeListElement = intNodeListElement.element("BehIntNode");
		Element connIntNodeListElement = intNodeListElement.element("ConnIntNode");
		Element expIntNodeListElement = intNodeListElement.element("ExpIntNode");
		
		if(behIntNodeListElement != null) {
			List<?> behIntNodeElementList = behIntNodeListElement.elements("Element");
			for(Object object : behIntNodeElementList) {
				Element behIntNodeElement = (Element)object;
				Node behIntNode = getIntNode(behIntNodeElement,"BehInt");
				intNodeList.add(behIntNode);
			}
		}
		if(connIntNodeListElement != null) {
			List<?> connexpIntNodeElementList = connIntNodeListElement.elements("Element");
			for(Object object : connexpIntNodeElementList) {
				Element connexpIntNodeElement = (Element)object;
				Node connIntNode = getIntNode(connexpIntNodeElement,"ConnInt");
				intNodeList.add(connIntNode);
			}
		}
		if(expIntNodeListElement != null) {
			List<?> expIntNodeElementList = expIntNodeListElement.elements("Element");
			for(Object object : expIntNodeElementList) {
				Element expIntNodeElement = (Element)object;
				Node expIntNode = getIntNode(expIntNodeElement,"ExpInt");
				intNodeList.add(expIntNode);
			}
		}
					
		return intNodeList;
	}
	
	private Node getIntNode(Element intNodeElement, String node_type) {
		List<?> fromNodeListElement = intNodeElement.elements("from");
		List<?> toNodeListElement = intNodeElement.elements("to");
		int node_no = Integer.parseInt(intNodeElement.attributeValue("node_no"));
		String node_locality = intNodeElement.attributeValue("node_locality");
		String[] locality= node_locality.split(",");
		int node_x = Integer.parseInt(locality[0]);
		int node_y = Integer.parseInt(locality[1]);
		List<Node> node_fromList = getNodeList(fromNodeListElement);
		List<Node> node_toList = getNodeList(toNodeListElement);
		
		Node intNode = new Node();
		intNode.setNode_no(node_no);
		intNode.setNode_type(node_type);
		intNode.setNode_x(node_x);
		intNode.setNode_y(node_y);
		intNode.setNode_fromList(node_fromList);
		intNode.setNode_toList(node_toList);
		
		Element pre_conditionElement = intNodeElement.element("pre_condition");
		Element post_conditionElement = intNodeElement.element("post_condition");
		if(pre_conditionElement != null) {
			String phenomenon_name1 = pre_conditionElement.attributeValue("phenomenon_name");
			if(phenomenon_name1 != null) {
				Phenomenon pre_condition = getPhenomenon(pre_conditionElement);
				intNode.setPre_condition((Phenomenon) pre_condition);
			}
		}
		if(post_conditionElement != null) {
			String phenomenon_name2 = post_conditionElement.attributeValue("phenomenon_name");
			if(phenomenon_name2 != null) {
				Phenomenon post_condition = getPhenomenon(post_conditionElement);
				intNode.setPost_condition((Phenomenon) post_condition);
			}
		}
	
		return intNode;
	}
	
	private Phenomenon getPhenomenon(Element phenomenonElement) {
		Phenomenon phenomenon = new Phenomenon();
		int phenomenon_no = Integer.parseInt(phenomenonElement.attributeValue("phenomenon_no"));
		String phenomenon_name = phenomenonElement.attributeValue("phenomenon_name");
		String phenomenon_type = phenomenonElement.attributeValue("phenomenon_type");
		String phenomenon_from = phenomenonElement.attributeValue("phenomenon_from").replaceAll("&#x000A", "\n");
		String phenomenon_to = phenomenonElement.attributeValue("phenomenon_to").replaceAll("&#x000A", "\n");

		phenomenon.setPhenomenon_no(phenomenon_no);
		phenomenon.setPhenomenon_name(phenomenon_name);
		phenomenon.setPhenomenon_type(phenomenon_type);
		phenomenon.setPhenomenon_from(phenomenon_from);
		phenomenon.setPhenomenon_to(phenomenon_to);

		return phenomenon;
	}
	
	private List<Node> getNodeList(List<?> nodeListElement) {
		List<Node> nodeList = new ArrayList<Node>();
		for(Object object : nodeListElement) {
			Element nodeElement = (Element) object;
			if(nodeElement.attributes().size() == 0) {
				continue;
			}
			int node_no = Integer.parseInt(nodeElement.attributeValue("node_no"));
			String node_type = nodeElement.attributeValue("node_type");
			String node_locality = nodeElement.attributeValue("node_locality");
			String[] locality= node_locality.split(",");
			int node_x = Integer.parseInt(locality[0]);
			int node_y = Integer.parseInt(locality[1]);
			
			Node node = new Node();
			node.setNode_no(node_no);
			node.setNode_type(node_type);
			node.setNode_x(node_x);
			node.setNode_y(node_y);
			nodeList.add(node);
		}
		return nodeList;
	}
	
	private List<Line> getLineList(Element lineListElement, String address, List<?> senarioGraphElementList) {
		List<Line> lineList = new ArrayList<Line>();
		
		Element behOrderListElement = lineListElement.element("BehOrder");
		Element behEnableListElement = lineListElement.element("BehEnable");
		Element synchronyListElement = lineListElement.element("Synchrony");
		Element expOrderListElement = lineListElement.element("ExpOrder");
		Element expEnableListElement = lineListElement.element("ExpEnable");
		
		lineList.addAll(getLineList(behOrderListElement, "BehOrder"));
		lineList.addAll(getLineList(behEnableListElement, "BehEnable"));
		lineList.addAll(getLineList(synchronyListElement, "Synchrony"));
		lineList.addAll(getLineList(expOrderListElement, "ExpOrder"));
		lineList.addAll(getLineList(expEnableListElement, "ExpEnable"));
		
		return lineList;
	}
	
	private List<CtrlNode> getControlNodeList(Element controlNodeListElement) {
		List<CtrlNode> controlNodeList = new ArrayList<CtrlNode>();
		
		Element startNodeListElement = controlNodeListElement.element("StartNode");
		Element endNodeListElement = controlNodeListElement.element("EndNode");
		Element decisionNodeListElement = controlNodeListElement.element("DecisionNode");
		Element mergeNodeListElement = controlNodeListElement.element("MergeNode");
		Element branchNodeListElement = controlNodeListElement.element("BranchNode");
		Element delayNodeListElement = controlNodeListElement.element("DelayNode");
		
		List<?> startNodeElementList = startNodeListElement.elements("Element");
		List<?> endNodeElementList = endNodeListElement.elements("Element");
		List<?> decisionNodeElementList = decisionNodeListElement.elements("Element");
		List<?> mergeNodeElementList = mergeNodeListElement.elements("Element");
		List<?> branchNodeElementList = branchNodeListElement.elements("Element");
		List<?> delayNodeElementList = null;
		if(delayNodeListElement != null)
			delayNodeElementList = delayNodeListElement.elements("Element");
		
		controlNodeList.addAll(getControlNodeList(startNodeElementList,"Start"));
		controlNodeList.addAll(getControlNodeList(endNodeElementList,"End"));
		controlNodeList.addAll(getControlNodeList(decisionNodeElementList,"Decision"));
		controlNodeList.addAll(getControlNodeList(mergeNodeElementList,"Merge"));
		controlNodeList.addAll(getControlNodeList(branchNodeElementList,"Branch"));
		if(delayNodeElementList != null)
			controlNodeList.addAll(getControlNodeList(delayNodeElementList,"Delay"));
		
		return controlNodeList;
	}
	
	private List<CtrlNode> getControlNodeList(List<?> controllNodeElementList,String nodeType){
		List<CtrlNode> controlNodeList = new ArrayList<CtrlNode>();
		for(Object object : controllNodeElementList) {
			Element controlNodeElement = (Element) object;
			List<?> fromNodeListElement = controlNodeElement.elements("from");
			List<?> toNodeListElement = controlNodeElement.elements("to");
			
			int node_no = Integer.parseInt(controlNodeElement.attributeValue("node_no"));
			String node_type = nodeType;
			String node_locality =controlNodeElement.attributeValue("node_locality");
			String[] locality= node_locality.split(",");
			int node_x = Integer.parseInt(locality[0]);
			int node_y = Integer.parseInt(locality[1]);
			List<Node> node_fromList = getNodeList(fromNodeListElement);
			List<Node> node_toList = getNodeList(toNodeListElement);
			
			CtrlNode controlNode = new CtrlNode();
			controlNode.setNode_no(node_no);
			controlNode.setNode_type(node_type);
			controlNode.setNode_x(node_x);
			controlNode.setNode_y(node_y);
			controlNode.setNode_fromList(node_fromList);
			controlNode.setNode_toList(node_toList);
			
			if(nodeType.equals("Decision") || nodeType.equals("Delay")) {
				String node_text = controlNodeElement.attributeValue("node_text");
//				String node_consition1 = controlNodeElement.attributeValue("node_consition1");
//				String node_consition2 = controlNodeElement.attributeValue("node_consition2");
				controlNode.setNode_text(node_text);
//				controlNode.setNode_consition1(node_consition1);
//				controlNode.setNode_consition2(node_consition2);
			}
			
			if(nodeType.equals("Delay")) {
				String delay_type = controlNodeElement.attributeValue("delay_type");
				controlNode.setDelay_type(delay_type);
			}
			
			controlNodeList.add(controlNode);
		}
		return controlNodeList;
	}

	private List<Line> getLineList(Element lineListElement, String lineType) {
//		List<Node> intNodeList = getIntNodeList(address, senarioGraphElementList);
//		List<Phenomenon> phenomenonList = getPhenomenonList(address);
		List<Line> lineList = new ArrayList<Line>();
		List<?> lineElementList = lineListElement.elements("Element");
		for(Object object : lineElementList) {
			Element lineElement = (Element) object;
			String line_no = lineElement.attributeValue("line_no");
			String from_no = lineElement.attributeValue("from_no");
			String from_type = lineElement.attributeValue("from_type");
			String from_locality = lineElement.attributeValue("from_locality");
			String to_no = lineElement.attributeValue("to_no");
			String to_type = lineElement.attributeValue("to_type");
			String to_locality = lineElement.attributeValue("to_locality");
			String turnings = lineElement.attributeValue("turnings");
			String condition = lineElement.attributeValue("line_condition");
			String[] fromLocality= from_locality.split(",");
			int from_x = Integer.parseInt(fromLocality[0]);
			int from_y = Integer.parseInt(fromLocality[1]);
			String[] toLocality= to_locality.split(",");
			int to_x = Integer.parseInt(toLocality[0]);
			int to_y = Integer.parseInt(toLocality[1]);

			Node fromNode = new Node();
			Node toNode = new Node();
			fromNode.setNode_no(Integer.parseInt(from_no));
			fromNode.setNode_type(from_type);
			fromNode.setNode_x(from_x);
			fromNode.setNode_y(from_y);

			toNode.setNode_no(Integer.parseInt(to_no));
			toNode.setNode_type(to_type);
			toNode.setNode_x(to_x);
			toNode.setNode_y(to_y);
			
			Line line = new Line();
			line.setLine_no(Integer.parseInt(line_no));
			line.setLine_type(lineType);
			line.setFromNode(fromNode);
			line.setToNode(toNode);
			line.setTurnings(turnings);
			line.setCondition(condition);
			lineList.add(line);
		}
		return lineList;
	}
	
	// ================点击特定项目时，查找所有版本 =========================
	public List<String> searchVersion(String userAdd, String branch) {
		List<String> projectVersions = new ArrayList<String>();
		List<VersionInfo> versions = searchVersionInfo(userAdd, branch);
		if (versions != null) {
			for (VersionInfo version : versions) {
				projectVersions.add(version.getTime());
			}
		}
		return projectVersions;
	}
	
	// ==================保存项目===================
	// 切换分支，保存，并commit
	public boolean saveProject(String userAdd, Project project, String branch) {
		setProject(userAdd, branch);// 创建并切换分支
		saveProject(userAdd, branch, project);
		try {// commit
			GitUtil.RecordUploadProjAt("save", userAdd, ".");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// 只保存文件，不涉及git操作,保存在rootAddress目录下（传参确定rootAddress）
	public boolean saveProject(String userAdd, String branch, Project project) {
		VersionInfo current_version = null;
		boolean result = true;
		Document document = DocumentHelper.createDocument();
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		
		Element projectElement = document.addElement("project");
		Element titleElement = projectElement.addElement("title");
		Element fileListElement = projectElement.addElement("fileList");
		Element contextDiagramElement = fileListElement.addElement("ContextDiagram");
		Element problemDiagramElement = fileListElement.addElement("ProblemDiagram");
		
		String title = project.getTitle();
		titleElement.setText(title);
		ContextDiagram tmp_CD = project.getContextDiagram();
		ProblemDiagram tmp_PD = project.getProblemDiagram();
		if (tmp_CD != null) {
			contextDiagramElement.setText("ContextDiagram");
		}
		if (tmp_PD != null) {
			problemDiagramElement.setText("ProblemDiagram");
		}
		List<ScenarioGraph> senarioGraphList = project.getScenarioGraphList();	
		if(senarioGraphList != null) {
			Element senarioGraphListElement = fileListElement.addElement("SenarioGraphList");
			for(ScenarioGraph sg: senarioGraphList) {
				Element senarioGraphElement = senarioGraphListElement.addElement("SenarioGraph");
				senarioGraphElement.addText(sg.getTitle());
			}
		}
		List<CCSLSet> CCSLConstraintsList = project.getCcslSetList();
		System.out.println("CCSLConstraintsList:" + CCSLConstraintsList);
		if(CCSLConstraintsList != null) {
			Element CCSLConstraintsListElement = fileListElement.addElement("CCSLSetList");
			for(CCSLSet ccslSet: CCSLConstraintsList) {
				Element CCSLConstraintsElement = CCSLConstraintsListElement.addElement("CCSLSet");
				CCSLConstraintsElement.addText(ccslSet.getId());
				saveCCSLConstraints(userAdd, ccslSet);
			}
		}
		
		Ontology ontology = project.getOntology();
		if(ontology != null) {
			Element ontologyElement = fileListElement.addElement("Ontology");
			ontologyElement.addText("Ontology");
			saveOntology(userAdd, ontology);
		}
		
		CCSLSet composedCCSLConstraints = project.getComposedCcslSet();
		if(composedCCSLConstraints != null && composedCCSLConstraints.getId() != null) {
			Element composedCCSLConstraintsElement = fileListElement.addElement("ComposedCcslSet");
			Element CCSLConstraintsElement = composedCCSLConstraintsElement.addElement("CCSLSet");
			CCSLConstraintsElement.addText(composedCCSLConstraints.getId());
			saveCCSLConstraints(userAdd, composedCCSLConstraints);
		}
		
		CCSLSet simplifiedCcslSet = project.getSimplifiedCcslSet();
		if(simplifiedCcslSet != null && simplifiedCcslSet.getId() != null) {
			Element CCSLConstraintsListElement = fileListElement.addElement("SimplifiedCcslSet");
			Element CCSLConstraintsElement = CCSLConstraintsListElement.addElement("CCSLSet");
			CCSLConstraintsElement.addText(simplifiedCcslSet.getId());
			saveCCSLConstraints(userAdd, simplifiedCcslSet);
		}
		
		CCSLSet orchestratedCCSLSet = project.getOrchestrateCcslSet();
		if(orchestratedCCSLSet != null && orchestratedCCSLSet.getId() != null) {
			Element CCSLConstraintsListElement = fileListElement.addElement("OrchestratedCcslSet");
			Element CCSLConstraintsElement = CCSLConstraintsListElement.addElement("CCSLSet");
			CCSLConstraintsElement.addText(orchestratedCCSLSet.getId());
			saveCCSLConstraints(userAdd, orchestratedCCSLSet);
		}
		
		StringWriter strWtr = new StringWriter();
		File xmlFile = new File(userAdd + "Project.xml");
		if(xmlFile.exists() == true) {
			xmlFile.delete();
		}
		XMLWriter xmlWriter = new XMLWriter(strWtr, format);
		try {
			xmlWriter.write(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			xmlFile.createNewFile();
			XMLWriter out = new XMLWriter(new FileWriter(xmlFile));
			// XMLWriter out = new XMLWriter(new FileWriter(file), format);
			out.write(document);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		
		if(result) {
			try {
				current_version = GitUtil.currentVersion(userAdd);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private boolean saveOntology(String userAdd, Ontology ontology) {
		boolean res = false;
		//创建一个xml文档
		Document doc = DocumentHelper.createDocument();
		//用于格式化xml内容和设置头部标签
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		
		Element root = doc.addElement("Ontology");	//创建根节点
		Element titleElement = root.addElement("title");	//在root节点下创建一个名为title的节点
		titleElement.setText("Ontology");
		
		Element forbidEventsElement = root.addElement("ForbidEvents");	//在root节点下创建一个名为ForbidEvents的节点
		List<String> forbidEvents = ontology.getForbidEvents();
		if(forbidEvents.size() > 0) {
			for(int i = 0; i < forbidEvents.size(); i++) {
				String forEvent = forbidEvents.get(i);
				Element forbidEventElement = forbidEventsElement.addElement("ForbidEvent");
				forbidEventElement.setText(forEvent);
			}
		}
		
		Element excludeStatesElement = root.addElement("ExcludeStates");	//在root节点下创建一个名为ExcludeStates的节点
		List<String> excludeStates = ontology.getExcludeStates();
		if(excludeStates.size() > 0) {
			for(int i = 0; i < excludeStates.size(); i++) {
				String excludeState = excludeStates.get(i);
				Element excludeStateElement = excludeStatesElement.addElement("ExcludeState");
				excludeStateElement.setText(excludeState);
			}
		}
		
		Element CCSLConstraintsListElement = root.addElement("CCSLConstraintList");	//在root节点下创建一个名为CCSLConstraintList的节点
		List<String> CcslList = ontology.getCcslSet().getCcslList();
		if(CcslList.size() > 0) {
			for(int i = 0; i < CcslList.size(); i++) {
				String ccsl = CcslList.get(i);
				Element CCSLConstraintElement = CCSLConstraintsListElement.addElement("CCSLConstraint");
				CCSLConstraintElement.setText(ccsl);
			}
		}
		
		File xmlFile = new File(userAdd + "/ontology.xml");
		if(xmlFile.exists() == true) {
			xmlFile.delete();
		}
		try {
			xmlFile.createNewFile();
			//创建一个输出流对象
			Writer out = new FileWriter(xmlFile);
			//创建一个dom4j创建xml的对象
			XMLWriter writer = new XMLWriter(out, format);
			//调用write方法将doc文档写到指定路径
			writer.write(doc);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public List<Node> getIntFromNodeList(Node intNode, ScenarioGraph SG) {
		List<Node> nodeList = new ArrayList<Node>();
		List<Line> lineList = SG.getLineList();
		if (lineList == null) {
			return nodeList;
		}
		for (int i = 0; i < lineList.size(); i++) {
			Line line = lineList.get(i);
			if(line.getLine_type().equals("BehOrder") || line.getLine_type().equals("ExpOrder")) {
				if (line.getToNode().getNode_type().equals(intNode.getNode_type())
						&& line.getToNode().getNode_no() == intNode.getNode_no()) {
					nodeList.add(line.getFromNode());
				}
			}
		}
		return nodeList;
	}

	public List<Node> getIntToNodeList(Node intNode, ScenarioGraph SG) {
		List<Node> nodeList = new ArrayList<Node>();
		List<Line> lineList = SG.getLineList();
		if (lineList == null) {
			return nodeList;
		}
		for (int i = 0; i < lineList.size(); i++) {
			Line line = lineList.get(i);
			if(line.getLine_type().equals("BehOrder") || line.getLine_type().equals("ExpOrder")) {
				if (line.getFromNode().getNode_type().equals(intNode.getNode_type())
						&& line.getFromNode().getNode_no() == intNode.getNode_no()) {
					nodeList.add(line.getToNode());
				}
			}
		}
		return nodeList;
	}

	public List<Node> getFromNodeList(CtrlNode ctrlNode, ScenarioGraph SG) {
		List<Node> nodeList = new ArrayList<Node>();
		List<Line> lineList = SG.getLineList();
		if (lineList == null) {
			return nodeList;
		}
		for (int i = 0; i < lineList.size(); i++) {
			Line line = lineList.get(i);
			if (line.getToNode().getNode_type().equals(ctrlNode.getNode_type())
					&& line.getToNode().getNode_no() == ctrlNode.getNode_no()) {
				nodeList.add(line.getFromNode());
			}
		}
		return nodeList;
	}

	public List<Node> getToNodeList(CtrlNode ctrlNode, ScenarioGraph SG) {
		List<Node> nodeList = new ArrayList<Node>();
		List<Line> lineList = SG.getLineList();
		if (lineList == null) {
			return nodeList;
		}
		for (int i = 0; i < lineList.size(); i++) {
			Line line = lineList.get(i);
			if (line.getFromNode().getNode_type().equals(ctrlNode.getNode_type())
					&& line.getFromNode().getNode_no() == ctrlNode.getNode_no()) {
				nodeList.add(line.getToNode());
			}
		}
		return nodeList;
	}
	
	public boolean saveCCSLConstraints(String address, CCSLSet ccslSet) {
		boolean res = false;
		//创建一个xml文档
		Document doc = DocumentHelper.createDocument();
		//用于格式化xml内容和设置头部标签
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		
		Element root = doc.addElement("CCSLSet");	//创建根节点
		
		Element titleElement = root.addElement("title");	//在root节点下创建一个名为title的节点
		titleElement.setText(ccslSet.getId());
		
		Element beginNodeElement = root.addElement("begin_node");	//在root节点下创建一个名为beginNode的节点
		Element endNodeElement = root.addElement("end_node");	//在root节点下创建一个名为endNode的节点
		beginNodeElement.setText(ccslSet.getBegin());
		endNodeElement.setText(ccslSet.getEnd());
		
		Element CCSLConstraintsListElement = root.addElement("CCSLConstraintList");	//在root节点下创建一个名为CCSLConstraintList的节点
		List<String> CcslList = ccslSet.getCcslList();
		if(CcslList.size() > 0) {
			for(int i = 0; i < CcslList.size(); i++) {
				String ccsl = CcslList.get(i);
				Element CCSLConstraintElement = CCSLConstraintsListElement.addElement("CCSLConstraint");
				CCSLConstraintElement.setText(ccsl);
			}
		}
		
//		File xmlFile = new File(rootAddress + projectAddress + "/" + SG.getTitle() + ".xml");
//		File xmlFile = new File(address + fileName + "/" + ccslSet.getId() + ".xml");
		File xmlFile = new File(address + "/" + ccslSet.getId() + ".xml");
		if(xmlFile.exists() == true) {
			xmlFile.delete();
		}
		try {
			xmlFile.createNewFile();
			//创建一个输出流对象
			Writer out = new FileWriter(xmlFile);
			//创建一个dom4j创建xml的对象
			XMLWriter writer = new XMLWriter(out, format);
			//调用write方法将doc文档写到指定路径
			writer.write(doc);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	// 获取分支下的所有版本
	public List<VersionInfo> searchVersionInfo(String userAdd, String branch) {
		setProject(userAdd, branch);
		List<VersionInfo> versions = new ArrayList<VersionInfo>();
		try {
			GitUtil.gitCheckout(branch, userAdd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String command = "git reflog " + branch;
		File check = new File(userAdd);
		List<String> vs = new ArrayList<String>();
		String commitVersion = null;
		try {
			Process p1 = Runtime.getRuntime().exec(command, null, check);
			BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			String s;
			while ((s = br.readLine()) != null) {
				if (s.indexOf("commit") != -1) {
					commitVersion = s.split(" ")[0];
					vs.add(commitVersion);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String v : vs) {
			String versionCommand = "git show " + v;
			try {
				Process p2 = Runtime.getRuntime().exec(versionCommand, null, check);
				BufferedReader br = new BufferedReader(new InputStreamReader(p2.getInputStream()));
				String str = null;
				String time = null;
				String versionId = null;

				while ((str = br.readLine()) != null) {
					if (str.startsWith("commit")) {
						versionId = str.split(" ")[1].substring(0, 7);
					}
					if (str.startsWith("Date:")) {
						str = str.substring(8);
						time = str.substring(0, str.length() - 6);
						str = br.readLine();
						String value = br.readLine().split("0")[0];
						if (value.indexOf("upload") != -1) {
							if (value.indexOf("uploadproject") != -1) {
								continue;
							} else if (value.indexOf("uploadfile") != -1) {
								if (versions.size() > 0) {
									if (versions.get(versions.size() - 1).getCommand().indexOf("uploadfile") != -1) {
										continue;
									}
								}
							} else {
								continue;
							}
						}
						if (value.indexOf("download") != -1) {
							continue;
						}
						VersionInfo version = new VersionInfo();
						version.setVersionId(versionId);
						version.setTime(time);
						version.setCommand(value);
						versions.add(version);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return versions;
	}
	
	// ===================获取项目=====================
	// ==================从xml文件中读取Project====================
	public Project getProject(String userAdd, String branch, String version) {
		Project project = new Project();
		Project scenarioProject = getScenarioDiagrams(userAdd);
		if(scenarioProject.getTitle() != null) {
			project.setContextDiagram(scenarioProject.getContextDiagram());
			project.setProblemDiagram(scenarioProject.getProblemDiagram());
			project.setScenarioGraphList(scenarioProject.getScenarioGraphList());
		}
		
		SAXReader saxReader = new SAXReader();
		List<VersionInfo> versions = searchVersionInfo(userAdd, branch);
		try {
			GitUtil.gitCheckout(branch, userAdd);  // 切换分支
			GitUtil.rollback(branch, userAdd, branch, version, versions);  // 回滚到特定的版本
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			File xmlFile = new File(userAdd + "Project.xml");
			if(xmlFile.exists()) {
				Document document = saxReader.read(xmlFile);

				Element projectElement = document.getRootElement();
				Element titleElement = projectElement.element("title");
				Element fileListElement = projectElement.element("fileList");
				Element ontologyElement = fileListElement.element("Ontology");
				Element CCSLSetListElement = fileListElement.element("CCSLSetList");
				Element ComposedCcslSetElement = fileListElement.element("ComposedCcslSet");
				Element SimplifiedCCSLSetElement = fileListElement.element("SimplifiedCcslSet");
				Element OrchestratedCCSLSetElement = fileListElement.element("OrchestratedCcslSet");
				
				String title = titleElement.getText();			
				project.setTitle(title);
				
				if(ontologyElement != null) {
					Ontology ontology = getOntology(userAdd);
					project.setOntology(ontology);
				} else {
					Ontology ontology = new Ontology();
					project.setOntology(ontology);
				}
				if(CCSLSetListElement != null) {
					List<?> CCSLSetElementList = CCSLSetListElement.elements("CCSLSet");
					List<CCSLSet> ccslSetList = new ArrayList<CCSLSet>();
					for(Object ccslSet : CCSLSetElementList) {
						ccslSetList.add(getCCSLSetList(userAdd, ccslSet));
					}
					project.setCcslSetList(ccslSetList);
				} else {
					List<CCSLSet> ccslSetList = new ArrayList<CCSLSet>();
					project.setCcslSetList(ccslSetList);
				}
				
				if(ComposedCcslSetElement != null) {
					Element composedCCSLSetElement = ComposedCcslSetElement.element("CCSLSet");
					Object ccslSet = (Object)composedCCSLSetElement;
					CCSLSet CcslSet = new CCSLSet();
					CcslSet = getCCSLSetList(userAdd, ccslSet);
					project.setComposedCcslSet(CcslSet);
				} else {
					CCSLSet ccslSet = new CCSLSet();
					project.setComposedCcslSet(ccslSet);
				}
				
				if(SimplifiedCCSLSetElement != null) {
					Element simplifiedCCSLSetElement = SimplifiedCCSLSetElement.element("CCSLSet");
					Object ccslSet = (Object)simplifiedCCSLSetElement;
					CCSLSet CcslSet = new CCSLSet();
					CcslSet = getCCSLSetList(userAdd, ccslSet);
					project.setSimplifiedCcslSet(CcslSet);
				} else {
					CCSLSet ccslSet = new CCSLSet();
					project.setSimplifiedCcslSet(ccslSet);
				}
				
				if(OrchestratedCCSLSetElement != null) {
					Element orchestratedCCSLSetElement = OrchestratedCCSLSetElement.element("CCSLSet");
					Object ccslSet = (Object)orchestratedCCSLSetElement;
					CCSLSet CcslSet = new CCSLSet();
					CcslSet = getCCSLSetList(userAdd, ccslSet);
					project.setOrchestrateCcslSet(CcslSet);
				} else {
					CCSLSet ccslSet = new CCSLSet();
					project.setOrchestrateCcslSet(ccslSet);
				}
			} else {
				System.out.println("project.xml does not exist.");
				project.setTitle(branch);
			}
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		System.out.println("project:" + project.toString());
		return project;
	}
	
	private Ontology getOntology(String userAdd) {
		Ontology ontology = new Ontology();
		SAXReader saxReader = new SAXReader();
		try {
			File ontologyFile = new File(userAdd + "/ontology.xml");
			if(!ontologyFile.exists()) {
				return null;
			}
			Document document = saxReader.read(ontologyFile);
			
			Element ontologyElement = document.getRootElement();
			
			Element forbidEventsElement = ontologyElement.element("ForbidEvents");
			List<?> forbidEventElementList = forbidEventsElement.elements("ForbidEvent");
			List<String> forbidEvents = new ArrayList<String>();
			
			if(forbidEventElementList != null) {
				for(Object object : forbidEventElementList) {
					Element forbidEventElement = (Element)object;
					String forbidEvent  = forbidEventElement.getText();
					forbidEvents.add(forbidEvent);
				}
			}
			
			Element excludeSatesElement = ontologyElement.element("ExcludeStates");
			List<?> excludeStateElementList = excludeSatesElement.elements("ExcludeState");
			List<String> excludeStates = new ArrayList<String>();
			
			if(excludeStateElementList != null) {
				for(Object object : excludeStateElementList) {
					Element excludeStateElement = (Element)object;
					String excludeState  = excludeStateElement.getText();
					excludeStates.add(excludeState);
				}
			}
			
			Element CCSLConstraintListElement = ontologyElement.element("CCSLConstraintList");
			List<?> CCSLConstraintElementList = CCSLConstraintListElement.elements("CCSLConstraint");
			List<String> ccslList = new ArrayList<String>();
			
			if(CCSLConstraintElementList != null) {
				for(Object object : CCSLConstraintElementList) {
					Element CCSLConstraintElement = (Element)object;
					String ccsl  = CCSLConstraintElement.getText();
					ccslList.add(ccsl);
				}
			}
			CCSLSet ccslSet = new CCSLSet("OntologyCCSL", ccslList, null, null);
			
			ontology.setForbidEvents(forbidEvents);
			ontology.setExcludeStates(excludeStates);
			ontology.setCcslSet(ccslSet);
		}catch (DocumentException e) {
			e.printStackTrace();
		}
		return ontology;
	}
	
	private CCSLSet getCCSLSetList(String userAdd, Object ccslSet) {
		CCSLSet CcslSet = new CCSLSet();
		Element sgEle = (Element) ccslSet;
		String ccslSetName = sgEle.getText();
		SAXReader saxReader = new SAXReader();
		try {
//			File senarioGraphFile = new File(userAdd + fileName + "/" + ccslSetName + ".xml");
			File senarioGraphFile = new File(userAdd + "/" + ccslSetName + ".xml");
			if(!senarioGraphFile.exists()) {
				return null;
			}
			Document document = saxReader.read(senarioGraphFile);
			
			Element ccslSetElement = document.getRootElement();
			Element titleElement = ccslSetElement.element("title");
			Element beginNodeElement = ccslSetElement.element("begin_node");
			Element endNodeElement = ccslSetElement.element("end_node");
			
			String title = titleElement.getText();
			String beginNode = beginNodeElement.getText();
			String endNode = endNodeElement.getText();
			
			Element CCSLConstraintListElement = ccslSetElement.element("CCSLConstraintList");
			List<?> CCSLConstraintElementList = CCSLConstraintListElement.elements("CCSLConstraint");
			List<String> ccslList = new ArrayList<String>();
			
			if(CCSLConstraintElementList != null) {
				for(Object object : CCSLConstraintElementList) {
					Element CCSLConstraintElement = (Element)object;
					String ccsl  = CCSLConstraintElement.getText();
					ccslList.add(ccsl);
				}
			}
			
			CcslSet.setId(title);
			CcslSet.setBegin(beginNode);
			CcslSet.setEnd(endNode);
			CcslSet.setCcslList(ccslList);
		}catch (DocumentException e) {
			e.printStackTrace();
		}
		return CcslSet;
	}
	
	public void writeFile(String filename, String fileContent) {
//		System.out.println("write:" + filename);
		File file = new File(filename);
		try {
            if (file.exists()) {
            	file.delete();
            }
            file.createNewFile();
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(fileContent);
            writer.close();
        } catch (Exception e) {
            System.out.println("写文件内容操作出错");
            e.printStackTrace();
        }
	}
}
