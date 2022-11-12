package com.example.demo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class DosService {
	/*
	 * 执行dos命令的方法
	 * @param command 需要执行的dos命令
	 * @param file 指定开始执行的文件目录
	 * 
	 * @return true 转换成功，false 转换失败
	 */
	public static void executeCommand(String address, String fileName) {
//		System.out.println("getCG:" + fileName);
		String command = "dot " + fileName + ".dot -T png -o " + fileName + ".png";
		File file = new File(address);
//		System.out.println(command);
		try {
			Process p1 = Runtime.getRuntime().exec(command, null, file);
			BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			String s;
	        
//	        while ((s = br.readLine()) != null) {
//	        	System.out.println(s);
//	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
