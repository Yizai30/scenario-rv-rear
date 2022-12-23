package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.bean.Ontology;
import com.example.demo.bean.Project;
import com.example.demo.util.ScenarioRVConstants;
import com.example.demo.service.FileService;

@RestController
@CrossOrigin
@RequestMapping("/file")
@Slf4j
public class FileController {
	@Autowired	// 自动装配
	FileService fileService;
	
	private float time;
	
	private String rootAddress = ScenarioRVConstants.rootAddress;
	private String userAddress = ScenarioRVConstants.userAddress;
	
	@RequestMapping(value = "/searchProject", method = RequestMethod.GET)
	@ResponseBody
	public List<String> searchProject(@RequestParam String username) {
		List<String> projects = fileService.searchProject(getUserAdd(username));
		return projects;
	}
	
	@RequestMapping(value = "/setProject/{projectName}", method = RequestMethod.POST)
	@ResponseBody
	public boolean setProject(@RequestParam String username, @PathVariable("projectName") String projectName) {
		String branch = projectName;
		String userAdd = getUserAdd(username);
		boolean result = fileService.setProject(userAdd, branch);
		return result;
	}
	
	@RequestMapping("/upload/{projectName}")
	public boolean upload(@RequestParam String username, @RequestParam("xmlFile") MultipartFile uploadFile,
			@PathVariable("projectName") String projectName)
			throws IOException {
		String branch = projectName;
		if (uploadFile == null) {
			System.out.println("上传失败，无法找到文件！");
		}
		String fileName = uploadFile.getOriginalFilename();
//		if (!fileName.endsWith(".xml") || !fileName.endsWith(".owl")) {
//			System.out.println("上传失败，请选择正确的文件！");
//		}
		fileService.addFile(uploadFile, getUserAdd(username), branch);
		System.out.println(fileName + "上传成功");
		return true;
	}
	
	// 解析环境本体owl文件，获取环境本体对象
	@RequestMapping(value = "/getOntology/{fileName}", method = RequestMethod.GET)
	@ResponseBody
	public Ontology getOntology(@RequestParam String username, @PathVariable("fileName") String fileName) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		Ontology ontology = fileService.getOntology(fileName, getUserAdd(username));
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("解析owl文件时间：" + time + "ms");    //输出程序运行时间
		return ontology;
	}
	
	// 解析情景图xml文件，获取情景图对象
	@RequestMapping(value = "/getScenarioDiagrams", method = RequestMethod.GET)
	@ResponseBody
	public Project getScenarioDiagrams(@RequestParam String username) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		Project project = fileService.getScenarioDiagrams(getUserAdd(username));
		long endTime = System.currentTimeMillis();    //获取结束时间
		time = endTime - startTime;    //输出程序运行时间
		System.out.println("解析xml文件时间：" + time + "ms");    //输出程序运行时间
		return project;
	}
	
	@RequestMapping(value = "/searchVersion/{project}", method = RequestMethod.GET)
	@ResponseBody
	public List<String> searchVersion(@RequestParam String username, @PathVariable("project") String project) {
		String branch = project;
		List<String> versions = fileService.searchVersion(getUserAdd(username), branch);
		return versions;
	}
	
	@RequestMapping(value = "/saveProject/{projectName}", method = RequestMethod.POST)
	@ResponseBody
	public boolean saveProject(@RequestParam String username, @PathVariable("projectName") String projectName,
			@RequestBody Project project) {
		return fileService.saveProject(getUserAdd(username), project, projectName);
	}
	
	@RequestMapping(value = "/getProject/{projectName}/{version}", method = RequestMethod.GET)
	@ResponseBody
	public Project getProject(@RequestParam String username, @PathVariable("projectName") String projectName,
			@PathVariable("version") String version) {
		String userAdd = getUserAdd(username);
		String branch = projectName;
		Project project = fileService.getProject(userAdd, branch, version);
		return project;
	}
	
	public String getUserAdd(String username) {
		String userAdd;
		if (username == null || username == "") {
			userAdd = rootAddress;
		} else {
			userAdd = userAddress + username + "/";
		}
		return userAdd;
	}

}
