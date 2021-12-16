package com.example.demo.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.bean.CCSLSet;
import com.example.demo.bean.Constraint;
import com.example.demo.bean.CtrlNode;
import com.example.demo.bean.DirectedLine;
import com.example.demo.bean.Line;
import com.example.demo.bean.Node;
import com.example.demo.bean.Ontology;
import com.example.demo.bean.Phenomenon;
import com.example.demo.bean.Project;
import com.example.demo.bean.Reference;
import com.example.demo.bean.RequirementPhenomenon;
import com.example.demo.bean.ScenarioGraph;
import com.example.demo.bean.VersionInfo;
import com.example.demo.bean.VisualizedScenario;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectService {
	@Autowired	//自动装配
	FileService fileService;
	public static Stack<String> stack = new Stack<String>();
	
	public List<CCSLSet> sdToCCSL(String userAdd, Project project) {
		List<CCSLSet> ccslset = new ArrayList<CCSLSet>();
		List<ScenarioGraph> scenarioGraphs = project.getScenarioGraphList();
		int ccslNo = 1;
		int pheNo = 1;
		
		for(ScenarioGraph scenarioGraph: scenarioGraphs) {
			List<String> ccslList = new ArrayList<String>();
			Map<String, String> pheMap = new HashMap<String, String>();
			Map<String, String> unionMap = new HashMap<String, String>();
			List<Line> lineList =  scenarioGraph.getLineList();
			List<CtrlNode> ctrlNodes = scenarioGraph.getCtrlNodeList();
			List<String> ccslCycles = new ArrayList<String>();
			List<String> ccslCyclesCopy = new ArrayList<String>();
			String cycleString = "";
			loop:
			for(int i = 0; i < lineList.size(); i++) {
				Line line = lineList.get(i);
				String line_type = line.getLine_type();
				// Synchronicity(Beh ↔ Exp)    R5
				if(line_type.equals("Synchrony")) {
					continue;
				}
				Node from_node = line.getFromNode();
				Node to_node = line.getToNode();
				String from_node_type = from_node.getNode_type();
				String to_node_type = to_node.getNode_type();
				String from_node_name = getNodeName(from_node, ctrlNodes, userAdd);
				String to_node_name = getNodeName(to_node, ctrlNodes, userAdd);
				// Behavior Order || Expected Order
				if(line_type.equals("BehOrder") || line_type.equals("ExpOrder")) {
					// Basic structure    R3_basic
					if((from_node_type.equals("BehInt") || from_node_type.equals("ConnInt") || from_node_type.equals("ExpInt")) &&
							(to_node_type.equals("BehInt") || to_node_type.equals("ConnInt") || to_node_type.equals("ExpInt"))) {
						// state to phe
						if(from_node_name.contains("+(state)") && !to_node_name.contains("+(state)")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if(from_node_name.equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							String state_name = from_node_name.split("\\+")[0];
							String state_ccsl = state_name + ".s<" + state_name + ".f";
							if(!ccslList.contains(state_ccsl)) {
								ccslList.add(state_ccsl);
							}
							if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
								ccslList.add(state_name + ".f<" + cycleString + "_4");
							} else {
								ccslList.add(state_name + ".f<" + to_node_name);
							}
						// phe to state
						} else if(!from_node_name.contains("+(state)") && to_node_name.contains("+(state)")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if(from_node_name.equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							String state_name = to_node_name.split("\\+")[0];
							if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
								ccslList.add(cycleString + "_4<" + state_name + ".s");
							} else {
								ccslList.add(from_node_name + "<" + state_name + ".s");
							}
							String state_ccsl = state_name + ".s<" + state_name + ".f";
							if(!ccslList.contains(state_ccsl)) {
								ccslList.add(state_ccsl);
							}
						// state to state
						} else if(!from_node_name.contains("+(state)") && to_node_name.contains("+(state)")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if(from_node_name.equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							String state_name1 = from_node_name.split("\\+")[0];
							String state_ccsl1 = state_name1 + ".s<" + state_name1 + ".f";
							if(!ccslList.contains(state_ccsl1)) {
								ccslList.add(state_ccsl1);
							}
							String state_name2 = to_node_name.split("\\+")[0];
							if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
								ccslList.add(state_name1 + ".f<" + state_name2 + "4.s");
							} else {
								ccslList.add(state_name1 + ".f<" + state_name2 + ".s");
							}
							String state_ccsl2 = state_name2 + ".s<" + state_name2 + ".f";
							if(!ccslList.contains(state_ccsl2)) {
								ccslList.add(state_ccsl2);
							}
						// phe to phe
						} else {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if(from_node_name.equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
								ccslList.add(cycleString + "_4<" + to_node_name);
							} else {
								ccslList.add(from_node_name + "<" + to_node_name);
							}
						}
					// ctrlNode to phe
					} else if(!(from_node_type.equals("BehInt") || from_node_type.equals("ConnInt") || from_node_type.equals("ExpInt")) &&
							(to_node_type.equals("BehInt") || to_node_type.equals("ConnInt") || to_node_type.equals("ExpInt"))){
						if(from_node_name.equals("Start")) {
							ccslList.add("B<" + to_node_name);
						} else if(from_node_name.equals("Decision") || from_node_name.equals("Merge") || from_node_name.equals("Branch")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if((from_node_type + from_node.getNode_no()).equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							List<String> unionList = new ArrayList<String>();
							String pheKey = from_node_name + from_node.getNode_no();
							String pheValue = "phe" + pheNo;  
							if(!pheMap.containsKey(pheKey)) {
								pheMap.put(pheKey, pheValue);
								pheNo++;
							} else {
								pheValue = pheMap.get(pheKey);
							}
							
							if(!unionMap.containsValue(pheValue)) {
								if(to_node_name.contains("+(state)")) {
									String state_name = to_node_name.split("\\+")[0];
									unionList.add(state_name + ".s");
								} else {
									unionList.add(to_node_name);
								}
								for(int j = i + 1; j < lineList.size(); j++) {
									Line tempLine = lineList.get(j);
									if(tempLine.getLine_type().equals(line_type)) {
										Node temp_from_node = tempLine.getFromNode();
										String temp_from_node_type = temp_from_node.getNode_type();
										Node temp_to_node = tempLine.getToNode();
										String temp_to_node_name = getNodeName(temp_to_node, ctrlNodes, userAdd);
										if((temp_from_node_type.equals("Decision") && from_node.getNode_no() == temp_from_node.getNode_no()) ||
												(temp_from_node_type.equals("Merge") && from_node.getNode_no() == temp_from_node.getNode_no()) ||
												(temp_from_node_type.equals("Branch") && from_node.getNode_no() == temp_from_node.getNode_no())) {
											if(temp_to_node_name.contains("+(state)")) {
												String state_name = temp_to_node_name.split("\\+")[0];
												unionList.add(state_name + ".s");
											} else {
												unionList.add(temp_to_node_name);
											}
											if(temp_from_node_type.equals("Decision")) {
												break;
											}
										} 
									} else {
										break;
									}
								}
								String unionStr = "";
								if(unionList.size() > 1) {
									for(int m = 0; m < unionList.size(); m++) {
										String decision = unionList.get(m);
										if(m == 0) {
											unionStr += decision;
										} else {
											unionStr += "+" + decision;
										}
										
									}
									if(from_node_type.equals("Branch")) {
										ccslList.add(pheValue + "=" + unionStr);
									} else if(from_node_name.equals("Decision")) {
										ccslList.add(pheValue + "=" + unionStr);
										unionStr = unionStr.replace("+", "#");
										ccslList.add(unionStr);
									} else if(from_node_name.equals("Merge")) {
										unionStr = unionStr.replace("+", "˄");
										ccslList.add(pheValue + "=" + unionStr);
									}
									unionMap.put(unionStr, pheValue);
								} 
							} else {
								String unionKey = getKey(unionMap, pheValue);
//								if(to_node_name.contains("+(state)")) {
//									String stateName = to_node_name.split("\\+")[0];
//									String state_ccsl = to_node_name + ".s<" + to_node_name + ".f";
//									if(!ccslList.contains(state_ccsl)) {
//										ccslList.add(state_ccsl);
//									}
//								}
								if(!unionKey.contains(to_node_name)) {
//									ccslList.add(pheValue + "<" + to_node_name);
									if(to_node_name.contains("+(state)")) {
										to_node_name = to_node_name.split("\\+")[0];
										String unionStr = unionKey + "˄" + to_node_name + ".s";
										ccslList.add(pheValue + "=" + unionStr);
										unionMap.put(unionStr, pheValue);
										String state_ccsl = to_node_name + ".s<" + to_node_name + ".f";
										if(!ccslList.contains(state_ccsl)) {
											ccslList.add(state_ccsl);
										}
									} else {
										ccslList.add(pheValue + "<" + to_node_name);
									}
								} 
							}
						}
					// phe to ctrlNode
					} else if((from_node_type.equals("BehInt") || from_node_type.equals("ConnInt") || from_node_type.equals("ExpInt")) &&
							!(to_node_type.equals("BehInt") || to_node_type.equals("ConnInt") || to_node_type.equals("ExpInt"))){
						if(to_node_name.equals("End")) {
							if(from_node_name.contains("+(state)")) {
								String state_name = from_node_name.split("\\+")[0];
								if(!ccslCyclesCopy.contains(from_node_name)) {
									String state_ccsl = state_name + ".s<" + state_name + ".f";
									if(!ccslList.contains(state_ccsl)) {
										ccslList.add(state_ccsl);
									}
								}
								if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
									ccslList.add(state_name + ".f_4<E");
								} else {
									ccslList.add(state_name + ".f<E");
								}
							} else {
								if(!cycleString.equals("") && cycleString.equals(from_node_name)) {
									ccslList.add(from_node_name + "_" + "4<E");
								} else {
									ccslList.add(from_node_name + "<E");
								}
							}
						} else if(to_node_name.equals("Decision")) {
							String unionKey = to_node_name + to_node.getNode_no();
							String unionValue = "phe" + pheNo;  
							if(!pheMap.containsKey(unionKey)) {
								pheMap.put(unionKey, unionValue);
								pheNo++;
							} else {
								unionValue = pheMap.get(unionKey);
							}
							ccslList.add(from_node_name + "<" + unionValue);
						} else if(to_node_name.equals("Merge") || to_node_name.equals("Branch")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if(from_node_name.equals(ccsl1) && (to_node_type + to_node.getNode_no()).equals(ccsl2)) {
									continue loop;
								}
							}
							List<String> unionList = new ArrayList<String>();
							String pheKey = to_node_name + to_node.getNode_no();
							String pheValue = "phe" + pheNo;  
							if(!pheMap.containsKey(pheKey)) {
								if(from_node_name.contains("+(state)")) {
									String state_name = from_node_name.split("\\+")[0];
									unionList.add(state_name + ".f");
//									String state_ccsl = state_name + ".s<" + state_name + ".f";
//									if(!ccslList.contains(state_ccsl)) {
//										ccslList.add(state_ccsl);
//									}
								} else {
									unionList.add(from_node_name);
								}
								for(int j = i + 1; j < lineList.size(); j++) {
									Line tempLine = lineList.get(j);
									if(tempLine.getLine_type().equals(line_type)) {
										Node temp_from_node = tempLine.getFromNode();
										String temp_from_node_name = getNodeName(temp_from_node, ctrlNodes, userAdd);
										Node temp_to_node = tempLine.getToNode();
										String temp_to_node_type = temp_to_node.getNode_type();
										if((temp_to_node_type.equals("Merge") && to_node.getNode_no() == temp_to_node.getNode_no()) ||
												(temp_to_node_type.equals("Branch") && to_node.getNode_no() == temp_to_node.getNode_no())) {
											if(temp_from_node_name.contains("+(state)")) {
												String state_name = temp_from_node_name.split("\\+")[0];
												unionList.add(state_name + ".f");
//												String state_ccsl = state_name + ".s<" + state_name + ".f";
//												if(!ccslList.contains(state_ccsl)) {
//													ccslList.add(state_ccsl);
//												}
											} else {
												unionList.add(temp_from_node_name);
											}
										} 
									} else {
										break;
									}
								}
								// 多个节点指向branch或者merge节点的处理
								if(unionList.size() > 1) {
									// 首先判断是否存在循环
									String unionStr = "";
									String str = to_node_type + to_node.getNode_no();
									ccslCycles = getCycle(lineList, str);
									if(ccslCycles.size() > 0) {
										ccslCyclesCopy = ccslCycles;
										for(int m = 0; m < ccslCycles.size(); m++) {
											String ccslCycle = ccslCycles.get(m);
											if(ccslCycle.startsWith("Beh") || ccslCycle.startsWith("Exp")) {
												for(Line lineTemp: lineList) {
													String line_type_temp = line.getLine_type();
													if(line_type_temp.equals("BehOrder") || line_type_temp.equals("ExpOrder")) {
														if((lineTemp.getToNode().getNode_type() + lineTemp.getToNode().getNode_no()).equals(ccslCycle)) {
															ccslCycle = getNodeName(lineTemp.getToNode(), ctrlNodes, userAdd);
															ccslCycles.set(m, ccslCycle);
															break;
														}
													}
												}
											}
										}
										
										String cycleStart = ccslCycles.get(1);
										String cycleEnd = ccslCycles.get(ccslCycles.size() - 2);
										String tempCCSL = "";
										if(from_node_name.equals("Start")) {
											tempCCSL = "B<" + cycleStart + "_1";
										} else {
											tempCCSL = from_node_name + "<" + cycleStart + "_1";
										}
										if(!ccslList.contains(tempCCSL)) {
											ccslList.add(tempCCSL);
										}
										if(cycleStart.equals(cycleEnd)) {
											for(int m = 1; m < 4; m++) {
												String cycleCCSL = cycleStart + "_" + m + "<" + cycleStart + "_" + (m + 1);
												if(!ccslList.contains(cycleCCSL)) {
													ccslList.add(cycleCCSL);
												}
											}
										} else {
											for(int m = 1; m <= 4; m++) {
												String cycle1 = cycleStart;
												for(int n = 2; n <= ccslCycles.size() - 2; n++) {
													String cycle2 = ccslCycles.get(n);
													String cycleCCSL = cycle1 + "_" + m + "<" + cycle2 + "_" + m;
													if(!ccslList.contains(cycleCCSL)) {
														ccslList.add(cycleCCSL);
													}
													String cycleCCSL2 = cycle2 + "_" + m + "<" + cycle1 + "_" + (m + 1);
													if(!ccslList.contains(cycleCCSL2) && m != 4) {
														ccslList.add(cycleCCSL2);
													}
													if(n != ccslCycles.size() - 2) {
														cycle1 = ccslCycles.get(n);
													}
												}
											}
										}
										cycleString = cycleEnd;
									} else {
										for(int m = 0; m < unionList.size(); m++) {
											String decision = unionList.get(m);
											if(!cycleString.equals("") && cycleString.equals(decision)) {
												decision += "_4";
											}
											if(m == 0) {
												unionStr += decision;
											} else {
												unionStr += "+" + decision;
											}
										}
										if(unionMap.containsKey(unionStr)) {
											pheMap.put(pheKey, unionMap.get(unionStr));
										} else {
											pheMap.put(pheKey, pheValue);
											if(to_node_type.equals("Branch")) {
												ccslList.add(pheValue + "=" + unionStr);
											} else if(to_node_name.equals("Merge")) {
												unionStr = unionStr.replace("+", "˅");
												ccslList.add(pheValue + "=" + unionStr);
											}
											ccslList.add(pheValue + "=" + unionStr);
											unionMap.put(unionStr, pheValue);
											pheNo++;
										}
									}
								} else {
								// branch或者merge节点指向多个节点的处理
									if(!pheMap.containsKey(pheKey)) {
										pheMap.put(pheKey, pheValue);
										pheNo++;
									} else {
										pheValue = pheMap.get(pheKey);
									}
									ccslList.add(from_node_name + "<" + pheValue);
								}
							}
						} else {
							continue;
						}
					} else {
						// 如果to_node是merge节点
						if(to_node_name.equals("Merge")) {
							List<String> unionList = new ArrayList<String>();
							String pheKey = to_node_name + to_node.getNode_no();
							String pheValue = "phe" + pheNo;  
							if(!pheMap.containsKey(pheKey)) {
								if(from_node_name.contains("+(state)")) {
									String state_name = from_node_name.split("\\+")[0];
									unionList.add(state_name + ".f");
//									String state_ccsl = state_name + ".s<" + state_name + ".f";
//									if(!ccslList.contains(state_ccsl)) {
//										ccslList.add(state_ccsl);
//									}
								} else {
									unionList.add(from_node_name);
								}
								for(int j = i + 1; j < lineList.size(); j++) {
									Line tempLine = lineList.get(j);
									if(tempLine.getLine_type().equals(line_type)) {
										Node temp_from_node = tempLine.getFromNode();
										String temp_from_node_name = getNodeName(temp_from_node, ctrlNodes, userAdd);
										Node temp_to_node = tempLine.getToNode();
										String temp_to_node_type = temp_to_node.getNode_type();
										if(temp_to_node_type.equals("Merge") && to_node.getNode_no() == temp_to_node.getNode_no()) {
											if(temp_from_node_name.contains("+(state)")) {
												String state_name = temp_from_node_name.split("\\+")[0];
												unionList.add(state_name + ".f");
//												String state_ccsl = state_name + ".s<" + state_name + ".f";
//												if(!ccslList.contains(state_ccsl)) {
//													ccslList.add(state_ccsl);
//												}
											} else {
												unionList.add(temp_from_node_name);
											}
										} 
									} else {
										break;
									}
								}
								// 多个节点指向merge节点的处理
								if(unionList.size() > 1) {
									// 首先判断是否存在循环
									String unionStr = "";
									String str = to_node_type + to_node.getNode_no();
									ccslCycles = getCycle(lineList, str);
									if(ccslCycles.size() > 0) {
										ccslCyclesCopy = ccslCycles;
										for(int m = 0; m < ccslCycles.size(); m++) {
											String ccslCycle = ccslCycles.get(m);
											if(ccslCycle.startsWith("Beh") || ccslCycle.startsWith("Exp")) {
												for(Line lineTemp: lineList) {
													String line_type_temp = line.getLine_type();
													if(line_type_temp.equals("BehOrder") || line_type_temp.equals("ExpOrder")) {
														if((lineTemp.getToNode().getNode_type() + lineTemp.getToNode().getNode_no()).equals(ccslCycle)) {
															ccslCycle = getNodeName(lineTemp.getToNode(), ctrlNodes, userAdd);
															ccslCycles.set(m, ccslCycle);
															break;
														}
													}
												}
											}
										}
										
										String cycleStart = ccslCycles.get(1);
										String cycleEnd = ccslCycles.get(ccslCycles.size() - 2);
										String tempCCSL = "";
										if(from_node_name.equals("Start")) {
											tempCCSL = "B<" + cycleStart + "_1";
										} else if(from_node_name.equals("Merge")) {
											if(cycleStart.contains("+(state)")) {
												String state_name = cycleStart.split("\\+")[0];
												unionStr = state_name + "1.s";
//												String state_ccsl = state_name + "1.s<" + state_name + "1.f";
//												if(!ccslList.contains(state_ccsl)) {
//													ccslList.add(state_ccsl);
//												}
											} else {
												unionStr = cycleStart;
											}
											pheKey = from_node_name + from_node.getNode_no();
											if(!pheMap.containsKey(pheKey)) {
												pheMap.put(pheKey, pheValue);
												pheNo++;
											} else {
												pheValue = pheMap.get(pheKey);
											}
											if(!unionMap.containsKey(unionStr)) {
												unionMap.put(unionStr, pheValue);
											}
										}
										else {
											tempCCSL = from_node_name + "<" + cycleStart + "_1";
										}
										if(!ccslList.contains(tempCCSL) && !tempCCSL.equals("")) {
											ccslList.add(tempCCSL);
										}
										if(cycleStart.equals(cycleEnd)) {
											for(int m = 1; m < 4; m++) {
												String cycleCCSL = cycleStart + "_" + m + "<" + cycleStart + "_" + (m + 1);
												if(!ccslList.contains(cycleCCSL)) {
													ccslList.add(cycleCCSL);
												}
											}
											
										} else {
											for(int m = 1; m <= 4; m++) {
												String cycle1 = cycleStart;
												if(cycle1.contains("+(state)")) {
													String stateName = cycle1.split("\\+")[0];
													String stateStr = stateName + m +".s<" + stateName + m + ".f";
													if(!ccslList.contains(stateStr)) {
														ccslList.add(stateStr);
													}
												}
												for(int n = 2; n <= ccslCycles.size() - 2; n++) {
													String cycle2 = ccslCycles.get(n);
													String cycleCCSL = "";
													if(cycle1.contains("+(state)") && !cycle2.contains("+(state)")) {
														cycleCCSL = cycle1.split("\\+")[0] + ".f_" + m + "<" + cycle2 + "_" + m;
													} else if(!cycle1.contains("+(state)") && cycle2.contains("+(state)")) {
														cycleCCSL = cycle1 + "_" + m + "<" + cycle2.split("\\+")[0] + ".s" + "_" + m;
													} else if(cycle1.contains("+(state)") && cycle2.contains("+(state)")) {
														cycleCCSL = cycle1.split("\\+")[0] + ".f_" + m + "<" + cycle2.split("\\+")[0] + ".s" + "_" + m;
													} else {
														cycleCCSL = cycle1 + "_" + m + "<" + cycle2 + "_" + m;
													} 
													if(!ccslList.contains(cycleCCSL)) {
														ccslList.add(cycleCCSL);
													}
													if(n == ccslCycles.size() - 2) {
														if(cycleStart.contains("+(state)") && !cycle2.contains("+(state)")) {
															cycleCCSL = cycle2 + "_" + m + "<" + cycleStart.split("\\+")[0] + ".s" + "_" + (m + 1);
														} else if(!cycle1.contains("+(state)") && cycle2.contains("+(state)")) {
															cycleCCSL = cycle2.split("\\+")[0] + ".f_" + m + "<" + cycleStart + "_" + (m + 1);
														} else if(cycle1.contains("+(state)") && cycle2.contains("+(state)")) {
															cycleCCSL = cycle2.split("\\+")[0] + ".f_" + m + "<" + cycleStart.split("\\+")[0] + ".s" + "_" + (m + 1);
														} else {
															cycleCCSL = cycle2 + "_" + m + "<" + cycleStart + "_" + (m + 1);
														} 
														if(!ccslList.contains(cycleCCSL) && m != 4) {
															ccslList.add(cycleCCSL);
														}
													} else {
														cycle1 = ccslCycles.get(n);
														continue;
													}
												}
											}
										}
										cycleString = cycleEnd;
									} else {
										for(int m = 0; m < unionList.size(); m++) {
											String decision = unionList.get(m);
											if(m == 0) {
												unionStr += decision;
											} else {
												unionStr += "+" + decision;
											}
										}
										if(unionMap.containsKey(unionStr)) {
											pheMap.put(pheKey, unionMap.get(unionStr));
										} else {
											pheMap.put(pheKey, pheValue);
											ccslList.add(pheValue + "=" + unionStr);
											unionMap.put(unionStr, pheValue);
											pheNo++;
										}
									}
								} else {
								// merge节点指向多个节点的处理
									if(!pheMap.containsKey(pheKey)) {
										pheMap.put(pheKey, pheValue);
										pheNo++;
									} else {
										pheValue = pheMap.get(pheKey);
									}
									if(from_node_name.equals("Start")) {
										ccslList.add("B<" + pheValue);
									} else {
										ccslList.add(from_node_name + "<" + pheValue);
									}
								}
							}
						} else if(to_node_name.equals("Branch")) {
							List<String> unionList = new ArrayList<String>();
							String pheKey = to_node_name + to_node.getNode_no();
							String pheValue = "phe" + pheNo;  
							if(!pheMap.containsKey(pheKey)) {
								if(from_node_name.contains("+(state)")) {
									String state_name = from_node_name.split("\\+")[0];
									unionList.add(state_name + ".f");
//									String state_ccsl = state_name + ".s<" + state_name + ".f";
//									if(!ccslList.contains(state_ccsl)) {
//										ccslList.add(state_ccsl);
//									}
								} else {
									unionList.add(from_node_name);
								}
								for(int j = i + 1; j < lineList.size(); j++) {
									Line tempLine = lineList.get(j);
									if(tempLine.getLine_type().equals(line_type)) {
										Node temp_from_node = tempLine.getFromNode();
										String temp_from_node_name = getNodeName(temp_from_node, ctrlNodes, userAdd);
										Node temp_to_node = tempLine.getToNode();
										String temp_to_node_type = temp_to_node.getNode_type();
										if(temp_to_node_type.equals("Branch") && to_node.getNode_no() == temp_to_node.getNode_no()) {
											if(temp_from_node_name.contains("+(state)")) {
												String state_name = temp_from_node_name.split("\\+")[0];
												unionList.add(state_name + ".f");
//												String state_ccsl = state_name + ".s<" + state_name + ".f";
//												if(!ccslList.contains(state_ccsl)) {
//													ccslList.add(state_ccsl);
//												}
											} else {
												unionList.add(temp_from_node_name);
											}
										} 
									} else {
										break;
									}
								}
								// 多个节点指向branch节点的处理
								if(unionList.size() > 1) {
									String unionStr = "";
									for(int m = 0; m < unionList.size(); m++) {
										String decision = unionList.get(m);
										if(m == 0) {
											unionStr += decision;
										} else {
											unionStr += "+" + decision;
										}
									}
									if(unionMap.containsKey(unionStr)) {
										pheMap.put(pheKey, unionMap.get(unionStr));
									} else {
										pheMap.put(pheKey, pheValue);
										ccslList.add(pheValue + "=" + unionStr);
										unionMap.put(unionStr, pheValue);
										pheNo++;
									}
								} else {
								// branch节点指向多个节点的处理
									if(!pheMap.containsKey(pheKey)) {
										pheMap.put(pheKey, pheValue);
										pheNo++;
									} else {
										pheValue = pheMap.get(pheKey);
									}
									if(from_node_name.equals("Start")) {
										ccslList.add("B<" + pheValue);
									} else {
										ccslList.add(from_node_name + "<" + pheValue);
									}
								}
							}
						} else if(from_node_name.equals("Decision") || from_node_name.equals("Merge") || from_node_name.equals("Branch")) {
							for(int j = 0; j < ccslCycles.size() - 1; j++) {
								String ccsl1 = ccslCycles.get(j), ccsl2 = ccslCycles.get(j + 1);
								if((from_node_type + from_node.getNode_no()).equals(ccsl1) && to_node_name.equals(ccsl2)) {
									continue loop;
								}
							}
							List<String> unionList = new ArrayList<String>();
							String pheKey = from_node_name + from_node.getNode_no();
							String pheValue = "phe" + pheNo;  
							if(!pheMap.containsKey(pheKey)) {
								pheMap.put(pheKey, pheValue);
								pheNo++;
							} else {
								pheValue = pheMap.get(pheKey);
							}
							
							if(!unionMap.containsValue(pheValue)) {
								if(to_node_name.contains("+(state)")) {
									String state_name = to_node_name.split("\\+")[0];
									unionList.add(state_name + ".s");
//									String state_ccsl = state_name + ".s<" + state_name + ".f";
//									if(!ccslList.contains(state_ccsl)) {
//										ccslList.add(state_ccsl);
//									}
								} else {
									unionList.add(to_node_name);
								}
								for(int j = i + 1; j < lineList.size(); j++) {
									Line tempLine = lineList.get(j);
									if(tempLine.getLine_type().equals(line_type)) {
										Node temp_from_node = tempLine.getFromNode();
										String temp_from_node_type = temp_from_node.getNode_type();
										Node temp_to_node = tempLine.getToNode();
										String temp_to_node_name = getNodeName(temp_to_node, ctrlNodes, userAdd);
										if((temp_from_node_type.equals("Decision") && from_node.getNode_no() == temp_from_node.getNode_no()) ||
												(temp_from_node_type.equals("Merge") && from_node.getNode_no() == temp_from_node.getNode_no()) ||
												(temp_from_node_type.equals("Branch") && from_node.getNode_no() == temp_from_node.getNode_no())) {
											if(temp_to_node_name.contains("+(state)")) {
												String state_name = temp_to_node_name.split("\\+")[0];
												unionList.add(state_name + ".s");
//												String state_ccsl = state_name + ".s<" + state_name + ".f";
//												if(!ccslList.contains(state_ccsl)) {
//													ccslList.add(state_ccsl);
//												}
											} else {
												unionList.add(temp_to_node_name);
											}
											if(temp_from_node_type.equals("Decision")) {
												break;
											}
										} 
									} else {
										break;
									}
								}
								String unionStr = "";
								if(unionList.size() > 1) {
									for(int m = 0; m < unionList.size(); m++) {
										String decision = unionList.get(m);
										if(m == 0) {
											unionStr += decision;
										} else {
											unionStr += "+" + decision;
										}
										
									}
									if(from_node_type.equals("Branch")) {
										ccslList.add(pheValue + "=" + unionStr);
									} else if(from_node_name.equals("Decision")) {
										ccslList.add(pheValue + "=" + unionStr);
										unionStr = unionStr.replace("+", "#");
										ccslList.add(unionStr);
									} else if(from_node_name.equals("Merge")) {
										unionStr = unionStr.replace("+", "˄");
										ccslList.add(pheValue + "=" + unionStr);
									}
									unionMap.put(unionStr, pheValue);
								} 
							} else {
								String unionKey = getKey(unionMap, pheValue);
								if(to_node_name.contains("+(state)")) {
									to_node_name = to_node_name.split("\\+")[0];
									String state_ccsl = to_node_name + ".s<" + to_node_name + ".f";
									if(!ccslList.contains(state_ccsl)) {
										ccslList.add(state_ccsl);
									}
								}
								if(!unionKey.contains(to_node_name)) {
									if(to_node_name.equals("End")) {
										to_node_name = "E";
									}
									ccslList.add(pheValue + "<" + to_node_name);
								}
							}
						}
						else {
							continue;
						}
					}
				// Behavior Enable(Beh → Exp)    R6
				} 
				else if(line_type.equals("BehEnable")) {
					if(to_node_name.contains("+(state)")) {
						String state_name = to_node_name.split("\\+")[0];
						if(ccslCyclesCopy.contains(from_node_name)) {
							for(int m = 1; m <= 4; m++) {
								ccslList.add(from_node_name + "_" + m + "<" + state_name + ".s_" + m);
								String state_ccsl = state_name + ".s_" + m + "<" + state_name + ".f_" + m;
								if(!ccslList.contains(state_ccsl)) {
									ccslList.add(state_ccsl);
								}
							}
						} else {
							ccslList.add(from_node_name + "<" + state_name + ".s");
							String state_ccsl = state_name + ".s<" + state_name + ".f";
							if(!ccslList.contains(state_ccsl)) {
								ccslList.add(state_ccsl);
							}
						}
					} else {
						ccslList.add(from_node_name + "<" + to_node_name);
					}
				} else if(line_type.equals("ExpEnable")) {
					ccslList.add(from_node_name + "<" + to_node_name);
				}
			}
			// 去重
			ccslList.add("B~E");
			ccslList = removeRedundantString(ccslList);
			CCSLSet ccsl = new CCSLSet("CCSL-" + ccslNo, ccslList, "B", "E");
			ccslset.add(ccsl);
			
			// 生成.dot图
			String addressPng = userAdd + "ClockGraphs/";
			File file = new File(addressPng);
			file.mkdirs();
			String dot_address = addressPng + "CG-" + ccslNo;
			writeDot(addressPng + "CG-" + ccslNo + ".dot", ccslList);
			DosService.executeCommand(addressPng, dot_address);
			ccslNo++;
		}
		return ccslset;
	}
	
	private List<String> getCycle(List<Line> lineList, String string) {
		// TODO Auto-generated method stub
		List<DirectedLine> directedLineList = new ArrayList<>();
		for(Line line: lineList) {
			String line_type = line.getLine_type();
			String fromStr = line.getFromNode().getNode_type() + line.getFromNode().getNode_no();
			String toStr = line.getToNode().getNode_type() + line.getToNode().getNode_no();
			if(line.getFromNode().getNode_type().equals("Start")) {
				fromStr = "B";
			}
			if(line.getToNode().getNode_type().equals("End")) {
				toStr = "E";
			}
			if(line_type.equals("BehOrder") || line_type.equals("ExpOrder")) {
				directedLineList.add(new DirectedLine(fromStr, toStr));
			}
		}
		// 当前路径
		List<String> nowPath = new ArrayList<>();
		// 所有环路
		List<List<String>> cyclePaths = new ArrayList<List<String>>();
		cyclePaths = findAllCycles(directedLineList, "B", "E", nowPath, cyclePaths);
		List<String> cycleStrs = new ArrayList<String>();
		for(List<String> cyclePath: cyclePaths) {
			if(cyclePath.contains(string)) {
				int index = cyclePath.indexOf(string);
				for(int i = index; i < cyclePath.size(); i++) {
					String str = cyclePath.get(i);
					if(str.equals(string) && i != index) {
						cycleStrs.add(str);
						break;
					}
					cycleStrs.add(str);
				}
			}
		}
		return cycleStrs;
	}

	public String getNodeName(Node node, List<CtrlNode> ctrlNodes, String address) {
		List<Phenomenon> phenomenonList = getPhenomenonList(address);
		String node_type = node.getNode_type();
		int node_no = node.getNode_no();
		if(node_type.equals("BehInt") || node_type.equals("ConnInt") || node_type.equals("ExpInt")) {
			for(Phenomenon phenomenon: phenomenonList) {
				if(phenomenon.getPhenomenon_no() == node_no) {
					if(phenomenon.getPhenomenon_type().equals("state")) {
						return phenomenon.getPhenomenon_name() + "+(state)";
					} else {
						return phenomenon.getPhenomenon_name();
					}
				}
			}
		} else {
			for(CtrlNode ctrlNode: ctrlNodes) {
				if(ctrlNode.getNode_no() == node_no && ctrlNode.getNode_type().equals(node_type)) {
					return ctrlNode.getNode_type();
				}
			}
		}
		return "";
	}
	
	private List<Phenomenon> getPhenomenonList(String address){
		List<Phenomenon> phenomenonList = new ArrayList<Phenomenon>();
		SAXReader saxReader = new SAXReader();
		SAXReader saxReader1 = new SAXReader();
		try {
			File contextDiagramFile = new File(address + "ContextDiagram.xml");
			if (!contextDiagramFile.exists()) {
				return null;
			}
			File problemDiagramFile = new File(address + "ProblemDiagram.xml");
			if (!problemDiagramFile.exists()) {
				return null;
			}
			Document document = saxReader.read(contextDiagramFile);
			Document document1 = saxReader1.read(problemDiagramFile);
			Element contextDiagramElement = document.getRootElement();
			
			Element interfaceListElement = contextDiagramElement.element("Interface");
			List<?> interfaceElementList = interfaceListElement.elements("Element");
			for (Object object : interfaceElementList) {
				Element interfaceElement = (Element) object;
				List<?> phenomenonElementList = interfaceElement.elements("Phenomenon");
				phenomenonList.addAll(fileService.getPhenomenonList(phenomenonElementList));
			}
			
			Element problemDiagramElement = document1.getRootElement();
			Element constraintListElement = problemDiagramElement.element("Constraint");
			List<Constraint> constraintList = fileService.getConstraintList(constraintListElement);
			for(Constraint constraint: constraintList) {
				List<RequirementPhenomenon> consPhenomenonList = constraint.getPhenomenonList();
				for(RequirementPhenomenon phenomenon: consPhenomenonList) {
					if(!phenomenonList.contains(phenomenon)) {
						phenomenonList.add(phenomenon);
					}
				}
			}
			Element referenceListElement = problemDiagramElement.element("Reference");
			List<Reference> referenceList = fileService.getReferenceList(referenceListElement);
			for(Reference reference: referenceList) {
				List<RequirementPhenomenon> refPhenomenonList = reference.getPhenomenonList();
				for(RequirementPhenomenon phenomenon: refPhenomenonList) {
					if(!phenomenonList.contains(phenomenon)) {
						phenomenonList.add(phenomenon);
					}
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return phenomenonList;
	}
	
	public String getKey(Map<String,String> map,String value){
        String key="";
        //遍历map
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //如果value和key对应的value相同
            if(value.equals(entry.getValue())){
                key=entry.getKey();
                break;
            }
        }
        return key;
    }

	
	public CCSLSet CCSLComposition(String userAdd, Project project) {
		List<String> ccslList = new ArrayList<String>();
		int stateNum = 1;
		Ontology ontology = project.getOntology();
		List<String> forbidEvents = ontology.getForbidEvents();
		List<String> excludeStates = ontology.getExcludeStates();
	
		List<ScenarioGraph> scenarioGraphs = project.getScenarioGraphList();
		List<CCSLSet> ccslSetList = project.getCcslSetList();
		// 寻找情景中共享的现象列表
		// 共享的现象列表
		List<String> commonPhenomenon = new ArrayList<String>();
		// 全部现象列表
		List<String> allPhenomenon = new ArrayList<String>();
		List<String> removePhenomenon = new ArrayList<String>();
		for(int i = 0; i < scenarioGraphs.size(); i++) {
			ScenarioGraph scenarioGraph = scenarioGraphs.get(i);
			List<Line> lineList = scenarioGraph.getLineList();
			List<CtrlNode> ctrlNodes = scenarioGraph.getCtrlNodeList();
			List<String> pheNameList = new ArrayList<String>();
			for(Line line: lineList) {
				Node toNode = line.getToNode();
				String nodeType = toNode.getNode_type();
				String nodeName = getNodeName(toNode, ctrlNodes, userAdd);
				if(nodeName.contains("+(state)")) {
					nodeName = nodeName.split("\\+")[0];
				}
				if(!nodeType.equals("Merge") && !nodeName.equals("Branch") && !nodeName.equals("Decision") && !nodeName.equals("End")) {
					pheNameList.add(nodeName);
					if(line.getLine_type().equals("BehOrder") || line.getLine_type().equals("ExpOrder")) {
						allPhenomenon.add(nodeName);
					} else if(line.getLine_type().equals("Synchrony")) {
						removePhenomenon.add(nodeName);
					}
//					if(!allPhenomenon.contains(nodeName)) {
//						allPhenomenon.add(nodeName);
//					} else {
////						if(!commonPhenomenon.contains(nodeName)) {
//						if(line.getLine_type().equals("BehOrder") || line.getLine_type().equals("ExpOrder")) {
//							commonPhenomenon.add(nodeName);
//						}
//					}
				}
				
//				if(line.getLine_type().equals("Synchrony") || line.getLine_type().equals("ExpEnable")) {
//					removePhenomenon.add(nodeName);
//				}
			}
			
		}
//		for(String removePhe: removePhenomenon) {
//			if(commonPhenomenon.contains(removePhe)) {
//				commonPhenomenon.remove(removePhe);
//			}
//		}
		allPhenomenon.removeAll(removePhenomenon);
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String commonPhe: allPhenomenon) {
			if(!map.containsKey(commonPhe)) {
				map.put(commonPhe, 1);
			} else {
				int val = map.get(commonPhe);
				map.replace(commonPhe, val + 1);
			}
		}
		for (Map.Entry<String, Integer> entry : map.entrySet()) {  
			if(entry.getValue() >= 2) {
				commonPhenomenon.add(entry.getKey());
			} else {
				removePhenomenon.add(entry.getKey());
			}
		}  
//		commonPhenomenon.removeAll(removePhenomenon);
//		commonPhenomenon = removeRedundantString(commonPhenomenon);
		boolean cycleEvent = false, cycleState = false;
		loop:
		for(int j = 0; j < ccslSetList.size() - 1; j++) {
			CCSLSet ccslset1 = ccslSetList.get(j);
			CCSLSet ccslset2 = ccslSetList.get(j + 1);
			List<String> ccslList1 = ccslset1.getCcslList();
			List<String> ccslList2 = ccslset2.getCcslList();
			for(int i = 0; i < forbidEvents.size(); i++) {
				String forbidStr = forbidEvents.get(i);
				String excludeStr = excludeStates.get(i);
				String leftEvent = forbidStr.split(" forbid ")[0];
				String leftState = excludeStr.split(" exclude ")[0];
				String cycleStr = leftEvent + "_4<" + leftState + ".s_4";
				if(ccslList1.contains(cycleStr) && ccslList2.contains(cycleStr)) {
					cycleEvent = true;
					cycleState = true;
					break loop;
				}
			}
		}
		for(int i = 0; i < scenarioGraphs.size(); i++) {
			// 将环境本体中有关互斥事件对的CCSL约束添加进来
			for(String forbidStr: forbidEvents) {
				String leftEvent = forbidStr.split(" forbid ")[0];
				String rightEvent = forbidStr.split(" forbid ")[1];
				if(commonPhenomenon.contains(leftEvent) && commonPhenomenon.contains(rightEvent)) {
					if(cycleEvent == true && cycleState == true) {
						for(int m = 1; m <= 4; m++) {
							ccslList.add(leftEvent + "_" + m + (i + 1) + "#" + rightEvent + "_" + m + (i + 1));
						}
					} else {
						ccslList.add(leftEvent + (i + 1) + "#" + rightEvent + (i + 1));
					}
				}
			}
			// 将环境本体中有关互斥状态对的CCSL约束添加进来
			for(String excludeStr: excludeStates) {
				String leftState = excludeStr.split(" exclude ")[0];
				String rightState = excludeStr.split(" exclude ")[1];
				if(commonPhenomenon.contains(leftState) && commonPhenomenon.contains(rightState)) {
					if(cycleEvent == true && cycleState == true) {
						for(int m = 1; m <= 4; m++) {
							String state1S = leftState + (i + 1) + ".s_" + m;
							String state1F = leftState + (i + 1) + ".f_" + m;
							String state2S = rightState + (i + 1) + ".s_" + m;
							String state2F = rightState + (i + 1) + ".f_" + m;
							String stateS = "State" + stateNum + ".s";
							String stateF = "State" + stateNum + ".f";
							ccslList.add(stateS + "=" + state1S + "+" + state2S);
							ccslList.add(stateF + "=" + state1F + "+" + state2F);
							ccslList.add(state1S + "#" + state2S);
							ccslList.add(state1F + "#" + state2F);
							ccslList.add(stateS + "~" + stateF);
							ccslList.add(state1S + "~" + state1F);
							ccslList.add(state2S + "~"+ state2F);
							stateNum++;
						}
					} else {
						String state1S = leftState + (i + 1) + ".s";
						String state1F = leftState + (i + 1) + ".f";
						String state2S = rightState + (i + 1) + ".s";
						String state2F = rightState + (i + 1) + ".f";
						String stateS = "State" + stateNum + ".s";
						String stateF = "State" + stateNum + ".f";
						ccslList.add(stateS + "=" + state1S + "+" + state2S);
						ccslList.add(stateF + "=" + state1F + "+" + state2F);
						ccslList.add(state1S + "#" + state2S);
						ccslList.add(state1F + "#" + state2F);
						ccslList.add(stateS + "~" + stateF);
						ccslList.add(state1S + "~" + state1F);
						ccslList.add(state2S + "~"+ state2F);
						stateNum++;
					}
					
				}
			}
		}
		
		// 处理临时变量
		for(int i = 0; i < ccslSetList.size() - 1; i++) {
			CCSLSet ccslset1 = ccslSetList.get(i);
			CCSLSet ccslset2 = ccslSetList.get(i + 1);
			List<String> ccslList1 = ccslset1.getCcslList();
			List<String> ccslList2 = ccslset2.getCcslList();
			for(int m = 0; m < ccslList1.size(); m++) {
				String ccsl1 = ccslList1.get(m);
				if(ccsl1.contains("=")) {
					String equalLeft1 = ccsl1.split("=")[0];
					String equalRight1 = ccsl1.split("=")[1];
					for(int n = 0; n < ccslList2.size(); n++) {
						String ccsl2 = ccslList2.get(n);
						if(ccsl2.contains("=")) {
							String equalLeft2 = ccsl2.split("=")[0];
							String equalRight2 = ccsl2.split("=")[1];
							if(equalRight1.equals(equalRight2)) {
								ccsl2 = equalLeft1 + "=" + equalRight2;
								ccslList2.set(n, ccsl2);
								for(int j = 0; j < ccslList2.size(); j++) {
									String ccsl = ccslList2.get(j);
									if(ccsl.indexOf(equalLeft2) != -1) {
										ccslList2.set(j, ccsl.replace(equalLeft2, equalLeft1));
									}
								}
							}
						}
					}
				}
			}
		}
		List<List<String>> ccslListNew = new ArrayList<List<String>>();
		
		List<String> beginList = new ArrayList<String>();
		for(int i = 0; i < ccslSetList.size(); i++) {
			CCSLSet CCSL = ccslSetList.get(i);
			List<String> ccslListTmp = CCSL.getCcslList();
			List<String> ccslListCopy = copyList(ccslListTmp);
			for(int j = 0; j < ccslListCopy.size(); j++) {
				String ccsl = ccslListCopy.get(j);
				String leftStr = null, rightStr = null;
				if(ccsl.contains("<")){
					leftStr = ccsl.split("<")[0];
					rightStr = ccsl.split("<")[1];
					// 找到情景图中的第一个现象
					if(leftStr.equals("B")) {
						if(!beginList.contains(rightStr)) {
							if(commonPhenomenon.contains(rightStr)) {
								beginList.add(rightStr + (i + 1));
							} else {
								beginList.add(rightStr);
							}
							ccslListCopy.remove(ccsl);
//							j--;
							break;
						}
					}
				}
			}
//			ccslListCopy.addAll(ccslListTmp2);
			ccslListNew.add(ccslListCopy);
		}
		// 为组合时钟图开头添加union关系
		if(beginList.size() > 1) {
			ccslList.add("B<U");
			String unionStr = "U=";
			for(int i = 0; i < beginList.size(); i++) {
				String beginStr = beginList.get(i);
				if(i == 0) {
					unionStr += beginStr;
				} else {
					unionStr += "+" + beginStr;
				}
			}
			ccslList.add(unionStr);
		}
		// 将共有消息用不同的消息表示
		for(int i = 0; i < ccslListNew.size(); i++) {
			List<String> ccslListTmp = ccslListNew.get(i);
			String id = (i + 1) +"";
			for(int j = 0; j < ccslListTmp.size(); j++) {
				String ccsl = ccslListTmp.get(j);
				String leftStr = null, rightStr = null, expression = null;
				String leftState = null, rightState = null;
				String equalLeft = null;
				if(ccsl.contains("<")){
					leftStr = ccsl.split("<")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					} else if(leftStr.contains("_")) {
						leftState = leftStr;
						leftStr = leftStr.split("_")[0];
					}
					rightStr = ccsl.split("<")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					} else if(rightStr.contains("_")) {
						rightState = rightStr;
						rightStr = rightStr.split("_")[0];
					}
					expression = "<";
				} else if(ccsl.contains("~")) {
					leftStr = ccsl.split("~")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					}
					rightStr = ccsl.split("~")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					}
					expression = "~";
				} else if(ccsl.contains("+")) {
					equalLeft = ccsl.split("=")[0];
					leftStr = ccsl.split("=")[1].split("\\+")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					}
					rightStr = ccsl.split("=")[1].split("\\+")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					}
					expression = "+";
				} else if(ccsl.contains("#")) {
					leftStr = ccsl.split("#")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					}
					rightStr = ccsl.split("#")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					}
					expression = "#";
				} else if(ccsl.contains("˄")) {
					equalLeft = ccsl.split("=")[0];
					leftStr = ccsl.split("=")[1].split("\\˄")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					}
					rightStr = ccsl.split("=")[1].split("\\˄")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					}
					expression = "˄";
				} else if(ccsl.contains("˅")) {
					equalLeft = ccsl.split("=")[0];
					leftStr = ccsl.split("=")[1].split("\\˅")[0];
					if(leftStr.contains(".")) {
						leftState = leftStr.split("\\.")[1];
						leftStr = leftStr.split("\\.")[0];
					}
					rightStr = ccsl.split("=")[1].split("\\˅")[1];
					if(rightStr.contains(".")) {
						rightState = rightStr.split("\\.")[1];
						rightStr = rightStr.split("\\.")[0];
					}
					expression = "˅";
				} 
				boolean leftFlag = false, rightFlag = false;
//				for(String comMessage: commonPhenomenon) {
				if(commonPhenomenon.contains(leftStr)) {
//					if(leftStr.equals(comMessage)) {
					if(leftState != null) {
						if(leftState.contains("_") && (!leftState.startsWith("s") && !leftState.startsWith("f"))) {
							leftStr = leftState + id;
						} else {
							leftStr += id + "." + leftState;
						}
					} else {
						leftStr += id; 
					}
					leftFlag = true;
				}
				if(commonPhenomenon.contains(rightStr)) {
//					if(rightStr.equals(comMessage)) {
					if(rightState != null) {
						if(rightState.contains("_") && (!rightState.startsWith("s") && !rightState.startsWith("f"))) {
							rightStr = rightState + id;
						} else {
							rightStr += id + "." + rightState;
						}
					} else {
						rightStr += id;
					}
					rightFlag = true;
				}
//				}
				String ccslTmp = null;
				if(leftFlag && rightFlag) {	
					if(expression.equals("<") || expression.equals("~") || expression.equals("#")) {
						ccslTmp = leftStr + expression + rightStr;
					} else if(expression.equals("+") || expression.equals("˄") || expression.equals("˅")) {
						ccslTmp = equalLeft + "=" + leftStr + expression + rightStr;
					}
					if(!ccslList.contains(ccslTmp)) {
						ccslList.add(ccslTmp);
					}
				} else if(leftFlag && !rightFlag) {	
					if(rightState != null) {
						if(rightState.contains("_")) {
							rightStr = rightState;
						} else {
							rightStr += "." + rightState;
						}
					} 
					if(expression.equals("<") || expression.equals("~") || expression.equals("#")) {
						ccslTmp = leftStr + expression + rightStr;
					} else if(expression.equals("+") || expression.equals("˄") || expression.equals("˅")) {
						ccslTmp = equalLeft + "=" + leftStr + expression + rightStr;
					}
					if(!ccslList.contains(ccslTmp)) {
						ccslList.add(ccslTmp);
					}
				} else if(!leftFlag && rightFlag) {	
					if(leftState != null) {
						if(leftState.contains("_")) {
							leftStr = leftState;
						} else {
							leftStr += "." + leftState;
						}
					} 
					if(expression.equals("<") || expression.equals("~") || expression.equals("#")) {
						ccslTmp = leftStr + expression + rightStr;
					} else if(expression.equals("+") || expression.equals("˄") || expression.equals("˅")) {
						ccslTmp = equalLeft + "=" + leftStr + expression + rightStr;
					}
					if(!ccslList.contains(ccslTmp)) {
						ccslList.add(ccslTmp);
					}
				} else {
					if(!ccslList.contains(ccsl)) {
						ccslList.add(ccsl);
					}
				}
			}
		}
		
		// TODO 为共有消息添加union关系
		for(int i = 0; i < commonPhenomenon.size(); i++) {
			String commonMsg = commonPhenomenon.get(i);
			for(String excludeState: excludeStates) {
				if(excludeState.contains(commonMsg)) {
					ccslList.add(commonMsg + ".s=" + commonMsg + "1.s+" + commonMsg + "2.s");
					ccslList.add(commonMsg + ".f=" + commonMsg + "1.f+" + commonMsg + "2.f");
					break;
				} else {
					ccslList.add(commonMsg + "=" + commonMsg + "1+" + commonMsg + "2");
					break;
				}
			}
		}
		// 生成中间结果的.dot图
		String addressPng = userAdd + "ClockGraphs/";
		File file = new File(addressPng);
		file.mkdirs();
		writeDot(addressPng + "ComposedCG.dot", ccslList);
		DosService.executeCommand(addressPng, addressPng + "ComposedCG");
		
		CCSLSet ccsl = new CCSLSet("ComposedCCSL", ccslList, "B", "E");
		return ccsl;
	}

	// 时钟图的简化
	public CCSLSet CCSLSimplify(String userAdd, Project project) {
		List<String> composedCCSLList = project.getComposedCcslSet().getCcslList();
		List<String> ccslListTmp = copyList(composedCCSLList);
		Ontology ontology = project.getOntology();
		List<ScenarioGraph> scenarioGraphs = project.getScenarioGraphList();
//		int ontologyLength = ontology.getCcslSet().getCcslList().size() * 2;
		int ontologyLength = 0;
		for(String ccsl: ccslListTmp) {
			if(!ccsl.equals("B<U")) {
				ontologyLength++;
			} else {
				break;
			}
		}
		
		List<String> keyEvents = new ArrayList<String>();
		List<String> unimportantEvents = new ArrayList<String>();
		List<String> generalEvents = new ArrayList<String>();
		List<String> removeCCSLs = new ArrayList<String>();
		// 1.识别关键事件和不重要的事件
		List<String> forbidEvents = ontology.getForbidEvents();
		List<String> excludeStates = ontology.getExcludeStates();
		if(forbidEvents.size() > 0) {
			for(String forbidEvent: forbidEvents) {
				String[] forbidStrs = forbidEvent.split(" forbid ");
				for(String forbidStr: forbidStrs) {
					keyEvents.add(forbidStr);
				}
				// 3. 在环境本体的同一状态机中的不同通用关键事件的子时钟之间添加Exclude关系
				String exclude1 = "", exclude2 = "";
				loop:
				for(String ccsl1: ccslListTmp) {
					for(String ccsl2: ccslListTmp) {
						if(!ccsl1.equals(ccsl2)) {
							if(ccsl1.contains("_") && ccsl2.contains("_")) {
								if(ccsl1.contains(forbidStrs[0] + "_11") && ccsl2.contains(forbidStrs[1] + "_12")){
									exclude1 = forbidStrs[0] +"_11#" + forbidStrs[1] + "_12";
//									break loop;
								}
								if(ccsl1.contains(forbidStrs[0] + "_12") && ccsl2.contains(forbidStrs[1] + "_11")) {
									exclude2 = forbidStrs[0] +"_12#" + forbidStrs[1] + "_11";
									break loop;
								}
							} else {
								if(ccsl1.contains(forbidStrs[0] + "1") && ccsl2.contains(forbidStrs[1] + "2")){
									exclude1 = forbidStrs[0] +"1#" + forbidStrs[1] + "2";
//									break loop;
								}
								if(ccsl1.contains(forbidStrs[0] + "2") && ccsl2.contains(forbidStrs[1] + "1")) {
									exclude2 = forbidStrs[0] +"2#" + forbidStrs[1] + "1";
									break loop;
								}
							}
						}
					}
				}
				if(!exclude1.equals("")) {
					ccslListTmp.add(exclude1);
				}
				if(!exclude2.equals("")) {
					ccslListTmp.add(exclude2);
				}
				// 4.2 删除一些不必要的Exclude关系
				removeCCSLs.add(forbidStrs[0] +"1#" + forbidStrs[1] + "1");
				removeCCSLs.add(forbidStrs[0] +"2#" + forbidStrs[1] + "2");
				if(ontologyLength > 0) {
					ontologyLength -= 2;
				}
			}
		} else {
			// 全部现象列表
			List<String> allPhenomenon = new ArrayList<String>();
			List<String> removePhenomenon = new ArrayList<String>();
			for(int i = 0; i < scenarioGraphs.size(); i++) {
				ScenarioGraph scenarioGraph = scenarioGraphs.get(i);
				List<Line> lineList = scenarioGraph.getLineList();
				List<CtrlNode> ctrlNodes = scenarioGraph.getCtrlNodeList();
				for(Line line: lineList) {
					Node toNode = line.getToNode();
					String nodeType = toNode.getNode_type();
					String nodeName = getNodeName(toNode, ctrlNodes, userAdd);
					if(nodeName.contains("+(state)")) {
//						nodeName = nodeName.split("\\+")[0];
						break;
					}
					if(!nodeType.equals("Merge") && !nodeName.equals("Branch") && !nodeName.equals("Decision") && !nodeName.equals("End")) {
						if(!allPhenomenon.contains(nodeName)) {
							allPhenomenon.add(nodeName);
						} else {
							if(!keyEvents.contains(nodeName)) {
								keyEvents.add(nodeName);
							}
						}
					}
					
					if(line.getLine_type().equals("Synchrony")) {
						removePhenomenon.add(nodeName);
					}
				}
			}
			for(String removePhe: removePhenomenon) {
				if(keyEvents.contains(removePhe)) {
					keyEvents.remove(removePhe);
				}
			}
		}
		
		for(String excludeState: excludeStates) {
			String[] excludeStrs = excludeState.split(" exclude ");
			for(String excludeStr: excludeStrs) {
				unimportantEvents.add(excludeStr + ".s");
				unimportantEvents.add(excludeStr + ".f");
			}
		}
		// 通用时钟就是关键事件和不重要事件的集合
		generalEvents.addAll(keyEvents);
		generalEvents.addAll(unimportantEvents);
		
		// 2.删除通用时钟和连接它们的union关系
		for(int i = ontologyLength; i < ccslListTmp.size(); i++) {
			String ccsl = ccslListTmp.get(i);
			if(ccsl.contains("=")) {
				String unionLeft = ccsl.split("=")[0];
				for(String generalEvent: generalEvents) {
					if(unionLeft.equals(generalEvent)) {
						removeCCSLs.add(ccsl);
					}
				}
			}
		}
		ccslListTmp.removeAll(removeCCSLs);
		removeCCSLs.removeAll(removeCCSLs);
		// 4.删除一些不必要的时钟，以及StrictPre和Exclude关系
		for(int i = ontologyLength; i < ccslListTmp.size(); i++) {
			String ccsl = ccslListTmp.get(i);
			// 4.1 删除冗余的StrictPre关系
			if(ccsl.contains("<")) {
				String left = ccsl.split("<")[0];
				String right = ccsl.split("<")[1];
				String removeStr = isRedundant(ccslListTmp, left, right, ontologyLength);
				if(removeStr != "") {
					removeCCSLs.add(removeStr);
				}
			}
		}
		ccslListTmp.removeAll(removeCCSLs);
		removeCCSLs.removeAll(removeCCSLs);
		
		// 4.3 一些虚拟构造的时钟和它们之间的关系
		for(int i = 0; i < ontologyLength; i++) {
			String ccsl = ccslListTmp.get(i);
			removeCCSLs.add(ccsl);
		}
		ccslListTmp.removeAll(removeCCSLs);
		
		CCSLSet simplifiedCCSLSet = new CCSLSet("SimplifiedCCSL", ccslListTmp, "B", "E");
		// 生成.dot图
		String addressPng = userAdd + "/ClockGraphs/";
		File file = new File(addressPng);
		file.mkdirs();
		writeDot(addressPng + "SimplifiedCG.dot", ccslListTmp);
		DosService.executeCommand(addressPng, addressPng + "SimplifiedCG");
		return simplifiedCCSLSet;
	}
	
	private String isRedundant(List<String> ccslList, String left, String right, int ontologyLength) {
		String ccsl = left + "<" + right;
		String tempLeft = left, tempStr = "";
		boolean flag = false;
		int count = 0;
		for(int i = ontologyLength; i < ccslList.size(); i++) {
			if(tempLeft.equals(right)) {
				flag = true;
				break;
			}
			String temp = ccslList.get(i);
			if(temp.contains("<")) {
				String leftTemp = temp.split("<")[0];
				if(leftTemp.equals(tempLeft) && !temp.equals(ccsl)) {
					tempLeft = temp.split("<")[1];
					if(count == 0) {
						tempStr = tempLeft;
					}
					count++;
					if(tempLeft.equals("E") && !right.equals("E")) {
						tempLeft = tempStr;
					}
				} else if(!tempStr.equals("") && temp.split("<")[1].equals("E")) {
					tempLeft = tempStr;
				}
			}
		}
		if(tempLeft.equals(right)) {
			flag = true;
		}
		if(flag) {
			return ccsl;
		} 

		return "";
	}
	
	// 不一致场景的编排
	public CCSLSet CCSLOrchestrate(String userAdd, Project project) {
		List<String> simplifiedCCSLList = project.getSimplifiedCcslSet().getCcslList();
		List<String> ccslListTmp = copyList(simplifiedCCSLList);
		Ontology ontology = project.getOntology();
		List<String> forbidEvents = ontology.getForbidEvents();
		loop:
		for(String forbidEvent: forbidEvents) {
			String[] forbidStrs = forbidEvent.split(" forbid ");
			for(String ccsl1: ccslListTmp) {
				for(String ccsl2: ccslListTmp) {
					if(!ccsl1.equals(ccsl2)) {
						if(ccsl1.contains("_") && ccsl2.contains("_")) {
							if(ccsl1.contains(forbidStrs[0] + "_11") && ccsl2.contains(forbidStrs[1] + "_12")){
								ccslListTmp.add(forbidStrs[0] +"_11=" + forbidStrs[1] + "_12");
								ccslListTmp.remove(forbidStrs[0] +"_12=" + forbidStrs[1] + "_11");
								String testStr1 = forbidStrs[0] + "_11<" + forbidStrs[0] + "_12";
								ccslListTmp.add(testStr1);
								List<List<String>> ccslCycles = getCycles(ccslListTmp, "B", "E");
								if(ccslCycles.size() > 3) {
									ccslListTmp.remove(testStr1);
									testStr1 = forbidStrs[0] + "_12<" + forbidStrs[0] + "_11";
									ccslListTmp.add(testStr1);
								}
								String testStr2 = forbidStrs[1] + "_11<" + forbidStrs[1] + "_12";
								ccslListTmp.add(testStr2);
								List<List<String>> ccslCycles2 = getCycles(ccslListTmp, "B", "E");
								if(ccslCycles2.size() > 3) {
									ccslListTmp.remove(testStr2);
									testStr2 = forbidStrs[1] + "_12<" + forbidStrs[1] + "_11";
									ccslListTmp.add(testStr2);
								}
								break loop;
							}
						} else {
							if(ccsl1.contains(forbidStrs[0] + "1") && ccsl2.contains(forbidStrs[1] + "2")){
								// 1. 添加一个同步关系
								ccslListTmp.add(forbidStrs[0] +"1=" + forbidStrs[1] + "2");
								// 2. 删除另一个exclude关系
								ccslListTmp.remove(forbidStrs[0] +"2#" + forbidStrs[1] + "1");
								// 3. 为子时钟之间添加先于关系
								String testStr1 = forbidStrs[0] + "1<" + forbidStrs[0] + "2";
								ccslListTmp.add(testStr1);
								List<List<String>> ccslCycles = getCycles(ccslListTmp, "B", "E");
//								如果存在环路则添加反向的先于关系
								if(ccslCycles.size() > 3) {
									ccslListTmp.remove(testStr1);
									testStr1 = forbidStrs[0] + "2<" + forbidStrs[0] + "1";
									ccslListTmp.add(testStr1);
								}
								String testStr2 = forbidStrs[1] + "1<" + forbidStrs[1] + "2";
								ccslListTmp.add(testStr2);
								List<List<String>> ccslCycles2 = getCycles(ccslListTmp, "B", "E");
								if(ccslCycles2.size() > 3) {
									ccslListTmp.remove(testStr2);
									testStr2 = forbidStrs[1] + "2<" + forbidStrs[1] + "1";
									ccslListTmp.add(testStr2);
								}
								break loop;
							} else if(ccsl1.contains(forbidStrs[0] + "2") && ccsl2.contains(forbidStrs[1] + "1")) {
								ccslListTmp.add(forbidStrs[0] +"2=" + forbidStrs[1] + "1");
								ccslListTmp.remove(forbidStrs[0] +"1#" + forbidStrs[1] + "2");
								String testStr1 = forbidStrs[0] + "1<" + forbidStrs[0] + "2";
								ccslListTmp.add(testStr1);
								List<List<String>> ccslCycles = getCycles(ccslListTmp, "B", "E");
								if(ccslCycles.size() > 3) {
									ccslListTmp.remove(testStr1);
									testStr1 = forbidStrs[0] + "2<" + forbidStrs[0] + "1";
									ccslListTmp.add(testStr1);
								}
								String testStr2 = forbidStrs[1] + "1<" + forbidStrs[1] + "2";
								ccslListTmp.add(testStr2);
								List<List<String>> ccslCycles2 = getCycles(ccslListTmp, "B", "E");
								if(ccslCycles2.size() > 3) {
									ccslListTmp.remove(testStr2);
									testStr2 = forbidStrs[1] + "2<" + forbidStrs[1] + "1";
									ccslListTmp.add(testStr2);
								}
								break loop;
							}
						}
					}
				}
			}
		}
		CCSLSet orchestratedCCSLSet = new CCSLSet("OrchestratedCCSL", ccslListTmp, "B", "E");
		// 生成.dot图
		String addressPng = userAdd + "/ClockGraphs/";
		File file = new File(addressPng);
		file.mkdirs();
		writeDot(addressPng + "OrchestratedCG.dot", ccslListTmp);
		DosService.executeCommand(addressPng, addressPng + "OrchestratedCG");
		return orchestratedCCSLSet;
	}
	
    //不一致场景的可视化
	public VisualizedScenario visualizeScenario(String userAdd, Project project) {
		List<String> simplifiedCCSLList = project.getSimplifiedCcslSet().getCcslList();
		// 1. 根据简化时钟图获得所有路径
		List<List<String>> ccslPaths = getPath(simplifiedCCSLList, "B", "E");
		return null;
	}

	private List<List<String>> getCycles(List<String> composedCCSLListTmp, String startStr, String endStr) {
		List<DirectedLine> directedLineList = getDirectedLineList(composedCCSLListTmp);
		// 当前路径
		List<String> nowPath = new ArrayList<>();
		// 所有环路
		List<List<String>> cyclePaths = new ArrayList<List<String>>();
		cyclePaths = findAllCycles(directedLineList, startStr, endStr, nowPath, cyclePaths);
		return cyclePaths;
	}
	
	private List<DirectedLine> getDirectedLineList(List<String> CCSLList) {
		List<DirectedLine> directedLineList = new ArrayList<>();
		for(String ccsl: CCSLList) {
			if(ccsl.contains("<")) {
				String leftStr = ccsl.split("<")[0];
				String rightStr = ccsl.split("<")[1];
				if(!leftStr.equals("B")) {
					directedLineList.add(new DirectedLine(leftStr, rightStr));
				}
			} else if(ccsl.contains("=") && ccsl.contains("+")) {
				String leftStr = ccsl.split("=")[0];
				if(leftStr.equals("U")) {
					String rigthStr = ccsl.split("=")[1];
					String event1 = rigthStr.split("\\+")[0];
					String event2 = rigthStr.split("\\+")[1];
					directedLineList.add(new DirectedLine("B", event1));
					directedLineList.add(new DirectedLine("B", event2));
				}
			} else if(ccsl.contains("=") && !ccsl.contains("+")) {
				String event1 = ccsl.split("=")[0];
				String event2 = ccsl.split("=")[1];
				directedLineList.add(new DirectedLine(event1, event2));
				directedLineList.add(new DirectedLine(event2, event1));
			}
		}
		return directedLineList;
	}

	private List<List<String>> findAllCycles(List<DirectedLine> directedLineList, String startName, String endName,
			List<String> nowPath, List<List<String>> cyclePaths) {
		if(nowPath.contains(startName)){
            List<String> cyclePath = copyList(nowPath);
            cyclePath.add(startName);
//            System.out.println("这是一个环：:" + cyclePath);
            cyclePaths.add(cyclePath);
            nowPath.remove(nowPath.size() - 1);
            return null;
        }
		
        for(int i = 0; i < directedLineList.size(); i++){
        	DirectedLine line = directedLineList.get(i);
            if(line.getSource().equals(startName)){
                nowPath.add(line.getSource());
                if(line.getTarget().equals(endName)){
                    nowPath.add(line.getTarget());
                    // System.out.println("这是一条路径：:" + nowPath);
                    // 因为添加了终点路径,所以要返回两次
                    nowPath.remove(nowPath.size() - 1);
                    nowPath.remove(nowPath.size() - 1);
                    // 已经找到路径,返回上层找其他路径
                    continue;
                }
                findAllCycles(directedLineList, line.getTarget(), endName, nowPath, cyclePaths);
            }
        }
        // 如果找不到下个节点,返回上层
        if(nowPath.size() > 0){
            nowPath.remove(nowPath.size() - 1);
        }
		return cyclePaths;
	}
	
	private List<List<String>> getPath(List<String> composedCCSLListTmp, String startStr, String endStr) {
		List<DirectedLine> directedLineList = getDirectedLineList(composedCCSLListTmp);
		// 当前路径
		List<String> nowPath = new ArrayList<>();
		// 所有环路
		List<List<String>> allPaths = new ArrayList<List<String>>();
		allPaths = findAllPath(directedLineList, startStr, endStr, nowPath, allPaths);
		return allPaths;
	}
	
	private List<List<String>> findAllPath(List<DirectedLine> directedLineList, String startName, String endName,
			List<String> nowPath, List<List<String>> allPath) {
		List<String> newPath = new ArrayList<>();
		if(nowPath.contains(startName)){
            System.out.println("这是一个环：:" + nowPath);
            List<String> cyclePath = copyList(nowPath);
            String str1 = cyclePath.get(0);
            String str2 = cyclePath.get(1);
            cyclePath.add(str1);
            cyclePath.add(str2);
            allPath.add(cyclePath);
            nowPath.remove(nowPath.size() - 1);
            return null;
        }
		
        for(int i = 0; i < directedLineList.size(); i++){
        	DirectedLine line = directedLineList.get(i);
            if(line.getSource().equals(startName)){
                nowPath.add(line.getSource());
                if(line.getTarget().equals(endName)){
                    nowPath.add(line.getTarget());
                    System.out.println("这是一条路径：:" + nowPath);
                    newPath = copyPath(newPath, nowPath);
                    allPath.add(newPath);
                    // 因为添加了终点路径,所以要返回两次
                    nowPath.remove(nowPath.size() - 1);
                    nowPath.remove(nowPath.size() - 1);
                    // 已经找到路径,返回上层找其他路径
                    continue;
                }
                findAllPath(directedLineList, line.getTarget(), endName, nowPath, allPath);
            }
        }
        // 如果找不到下个节点,返回上层
        if(nowPath.size() > 0){
            nowPath.remove(nowPath.size() - 1);
        }
		return allPath;
	}

	private List<String> copyPath(List<String> newPath, List<String> nowPath) {
		for(int i = 0; i < nowPath.size(); i++) {
			String element = nowPath.get(i);
			newPath.add(element);
		}
		return newPath;
	}
	
	private List<String> copyList(List<String> ccslListTmp) {
		List<String> newList = new ArrayList<String>();
		for(String ccsl: ccslListTmp) {
			newList.add(ccsl);
		}
		return newList;
	}

	private List<String> removeRedundantString(List<String> ccslList) {
		for(int i = ccslList.size() - 1; i >= 0; i--) {
			String ccsl1 = ccslList.get(i);
			for(int j = i - 1; j >= 0; j--) {
				String ccsl2 = ccslList.get(j);
				if(ccsl1.equals(ccsl2)) {
					ccslList.remove(i);
					break;
				}
			}
		}
		return ccslList;
	}
	
	public void writeDot(String fileName, List<String> ccslList) {
//		int index = 1;
		String fileContent = "digraph {\n";
		for(String ccsl : ccslList) {
			if(ccsl.contains("<")) {
				String left = ccsl.split("<")[0];
				String right = ccsl.split("<")[1];
				if(left.contains(".") || left.contains("(") || left.contains("+")) {
					left = "\"" + left + "\"";
				} 
				if(right.contains(".") || right.contains("(") || right.contains("+")) {
					right = "\"" + right + "\"";
				}
				fileContent = fileContent + left + " -> " + right + "\n";
			} else if(ccsl.contains("~")) {
				String left = ccsl.split("~")[0];
				String right = ccsl.split("~")[1];
				if(left.contains(".") || left.contains("(") || left.contains("+")) {
					left = "\"" + left + "\"";
				}
				if(right.contains(".") || right.contains("(") || right.contains("+")) {
					right = "\"" + right + "\"";
				}
				fileContent = fileContent + left + " -> " + right + "[\"style\"=\"dashed\"]\n";
			} else if(ccsl.contains("#")) {
				String left = ccsl.split("#")[0];
				String right = ccsl.split("#")[1];
				if(left.contains(".") || left.contains("(") || left.contains("+")) {
					left = "\"" + left + "\"";
				}
				if(right.contains(".") || right.contains("(") || right.contains("+")) {
					right = "\"" + right + "\"";
				}
				fileContent = fileContent + left + " -> " + right + "[\"color\"=\"red\",\"style\"=\"normal\",\"dir\"=\"both\",\"arrowtail\"=\"diamond\",\"arrowhead\"=\"diamond\"]\n";
			} else if(ccsl.contains("+")) {
				String leftStr = ccsl.split("=")[0];
				if(leftStr.contains(".") || leftStr.contains("(") || leftStr.contains("+")) {
					leftStr = "\"" + leftStr + "\"";
				}
				String rightStr = ccsl.split("=")[1];
				String unionStr1 = rightStr.split("\\+")[0];
				String unionStr2 = rightStr.split("\\+")[1];
				if(unionStr1.contains(".") || unionStr1.contains("(") || unionStr1.contains("+")) {
					unionStr1 = "\"" + unionStr1 + "\"";
				}
				if(unionStr2.contains(".") || unionStr2.contains("(") || unionStr2.contains("+")) {
					unionStr2 = "\"" + unionStr2 + "\"";
				}
				fileContent = fileContent + unionStr1 + " -> " + leftStr + "[\"style\"=\"dashed\",\"color\"=\"green\"]\n";
				fileContent = fileContent + unionStr2 + " -> " + leftStr + "[\"style\"=\"dashed\"\"color\"=\"green\"]\n";
			} else if(ccsl.contains("⊆")) {
				String left = ccsl.split("⊆")[0];
				String right = ccsl.split("⊆")[1];
				if(left.contains(".") || left.contains("(") || left.contains("+")) {
					left = "\"" + left + "\"";
				}
				if(right.contains(".") || right.contains("(") || right.contains("+")) {
					right = "\"" + right + "\"";
				}
				fileContent = fileContent + left + " -> " + right + "[\"color\"=\"purple\",\"arrowhead\"=\"diamond\"]\n";
			} else if(ccsl.contains("$")) {
				String leftStr = ccsl.split("=")[0];
				String rightStr = ccsl.split("=")[1];
				String tmpStr = rightStr.split("\\$")[0];
				if(tmpStr.contains(".") || tmpStr.contains("(") || tmpStr.contains("+")) {
					tmpStr = "\"" + tmpStr + "\"";
				}
				if(leftStr.contains(".") || leftStr.contains("(") || leftStr.contains("+")) {
					leftStr = "\"" + leftStr + "\"";
				}
				fileContent = fileContent + tmpStr + "->" + leftStr + "[\"style\"=\"normal\",\"color\"=\"blue\"]\n";
//				continue;
			} else if(ccsl.contains("˄")) {
				String leftStr = ccsl.split("=")[0];
				if(leftStr.contains(".") || leftStr.contains("(") || leftStr.contains("+")) {
					leftStr = "\"" + leftStr + "\"";
				}
				String rightStr = ccsl.split("=")[1];
				String unionStr1 = rightStr.split("\\˄")[0];
				String unionStr2 = rightStr.split("\\˄")[1];
				if(unionStr1.contains(".") || unionStr1.contains("(") || unionStr1.contains("+")) {
					unionStr1 = "\"" + unionStr1 + "\"";
				}
				if(unionStr2.contains(".") || unionStr2.contains("(") || unionStr2.contains("+")) {
					unionStr2 = "\"" + unionStr2 + "\"";
				}
				fileContent = fileContent + unionStr1 + " -> " + leftStr + "[\"style\"=\"normal\",\"color\"=\"orange\"]\n";
				fileContent = fileContent + unionStr2 + " -> " + leftStr + "[\"style\"=\"normal\"\"color\"=\"orange\"]\n";
			} else if(ccsl.contains("˅")) {
				String leftStr = ccsl.split("=")[0];
				if(leftStr.contains(".") || leftStr.contains("(") || leftStr.contains("+")) {
					leftStr = "\"" + leftStr + "\"";
				}
				String rightStr = ccsl.split("=")[1];
				String unionStr1 = rightStr.split("\\˅")[0];
				String unionStr2 = rightStr.split("\\˅")[1];
				if(unionStr1.contains(".") || unionStr1.contains("(") || unionStr1.contains("+")) {
					unionStr1 = "\"" + unionStr1 + "\"";
				}
				if(unionStr2.contains(".") || unionStr2.contains("(") || unionStr2.contains("+")) {
					unionStr2 = "\"" + unionStr2 + "\"";
				}
				fileContent = fileContent + unionStr1 + " -> " + leftStr + "[\"style\"=\"dashed\",\"color\"=\"orange\"]\n";
				fileContent = fileContent + unionStr2 + " -> " + leftStr + "[\"style\"=\"dashed\"\"color\"=\"orange\"]\n";
			} else if(ccsl.contains("=") && !ccsl.contains("+")) {
				String left = ccsl.split("=")[0];
				String right = ccsl.split("=")[1];
				if(left.contains(".") || left.contains("(") || left.contains("+")) {
					left = "\"" + left + "\"";
				}
				if(right.contains(".") || right.contains("(") || right.contains("+")) {
					right = "\"" + right + "\"";
				}
				fileContent = fileContent + left + " -> " + right + "[\"style\"=\"normal\",\"color\"=\"green\",\"arrowhead\"=\"none\"]\n";
			} 
		}
		fileContent = fileContent + "}";
		fileService.writeFile(fileName, fileContent);
	}
	
	public void ToPng(String userName, String projectName, String version, String fileName, HttpServletResponse response) {
		String address;

		if(userName.equals("")) {
			address = AddressService.rootAddress;
		}else {
			address = AddressService.userAddress + userName + "/";
		}
		List<VersionInfo> versions = fileService.searchVersionInfo(userName, projectName);
		try {
			if(!GitUtil.currentBranch(address).equals(projectName)) {
				GitUtil.gitCheckout(projectName, address);
			}
			GitUtil.rollback(projectName, address, projectName, version, versions);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileInputStream is = null;
		String filePath = address;
//		if(fileName.startsWith("CG")) {
			filePath += "ClockGraphs/"  + fileName + ".png";
//		} 
//		else if(fileName.startsWith("Composed")) {
//			filePath += "ComposedClockGraph/"  + fileName + ".png";
//		} else if(fileName.startsWith("Sub")) {
//			filePath += "SubClockGraphs/"  + fileName + ".png";
//		}
		File filePic = new File(filePath);
		if(filePic.exists()) {
			try {
				is = new FileInputStream(filePic);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			response.setContentType("image/png");
			if(is != null) {
				try {
					int i = is.available();
					byte data[] = new byte[i];
					is.read(data);
					is.close();
					response.setContentType("image/png");
					OutputStream toClient = response.getOutputStream();
					toClient.write(data);
					toClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	

}
