package com.example.demo.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.bean.CCSLSet;
import com.example.demo.bean.Project;
import com.example.demo.bean.VisualizedScenario;
import com.example.demo.service.AddressService;
import com.example.demo.service.DosService;
import com.example.demo.service.ProjectService;

@RestController
@CrossOrigin
@RequestMapping("/project")
public class ProjectController {
	@Autowired	// 自动装配
	ProjectService projectService;
	private float time;
	
	private String rootAddress = AddressService.rootAddress;
	private String userAddress = AddressService.userAddress;
	
	@RequestMapping(value = "/sdToCCSL", method = RequestMethod.POST)
	@ResponseBody
	public List<CCSLSet> sdToCCSL(@RequestParam String username, @RequestBody Project project) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		List<CCSLSet> ccslset = projectService.sdToCCSL(getUserAdd(username), project);
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("顺序图转换成CCSL的时间：" + time + "ms");    //输出程序运行时间
		return ccslset;
	}
	
	@RequestMapping(value = "/CCSLComposition", method = RequestMethod.POST)
	@ResponseBody
	public CCSLSet CCSLComposition(@RequestParam String username, @RequestBody Project project) {
		String userAdd = getUserAdd(username);
		long startTime = System.currentTimeMillis();    //获取开始时间
		CCSLSet ccsl = null;
		List<CCSLSet> ccslSet = project.getCcslSetList();
		if(ccslSet.size() == 1) {
			ccsl = ccslSet.get(0);
			List<String> ccslList = ccsl.getCcslList();
			// 生成.dot图
			String addressPng = userAdd + "/";
			File file = new File(addressPng);
			file.mkdirs();
			projectService.writeDot(addressPng + "ComposedCG.dot", ccslList);
			DosService.executeCommand(addressPng, addressPng + "ComposedCG");
		} else if(ccslSet.size() > 1) {
			ccsl = projectService.CCSLComposition(userAdd, project);
		}
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("组合时间：" + time + "ms");    //输出程序运行时间
		return ccsl;
	}
	
	@RequestMapping(value = "/CCSLSimplify", method = RequestMethod.POST)
	@ResponseBody
	public CCSLSet CCSLSimplify(@RequestParam String username, @RequestBody Project project) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		CCSLSet ccslset = projectService.CCSLSimplify(getUserAdd(username), project);
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("简化时间：" + time + "ms");    //输出程序运行时间
		return ccslset;
	}
	
	@RequestMapping(value = "/CCSLOrchestrate", method = RequestMethod.POST)
	@ResponseBody
	public CCSLSet CCSLOrchestrate(@RequestParam String username, @RequestBody Project project) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		CCSLSet ccslset = projectService.CCSLOrchestrate(getUserAdd(username), project);
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("编排时间：" + time + "ms");    //输出程序运行时间
		return ccslset;
	}
	
	@RequestMapping(value = "/visualizeScenario", method = RequestMethod.POST)
	@ResponseBody
	public VisualizedScenario visualizeScenario(@RequestParam String username, @RequestBody Project project) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		VisualizedScenario visualizedScenario = projectService.visualizeScenario(getUserAdd(username), project);
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("可视化时间：" + time + "ms");    //输出程序运行时间
		return visualizedScenario;
	}
	
	//图片显示
	@RequestMapping(value="/display",method = RequestMethod.GET)
	@ResponseBody
	public void diaplay(String userName, String projectName, String version, String fileName, HttpServletResponse response) {
		System.out.println("display:" + projectName + " " + fileName);
		projectService.ToPng(userName, projectName, version, fileName, response);
	}
	
	public String getUserAdd(String username) {
		String userAdd;
		if (username == null || username == "")
			userAdd = rootAddress;
		else
			userAdd = userAddress + username + "/";
		return userAdd;
	}
}
