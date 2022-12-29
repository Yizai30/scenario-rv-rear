package com.example.demo.controller;

import java.util.*;

import com.example.demo.bean.*;
import com.example.demo.service.CheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.Z3Util;

@CrossOrigin
@RestController
@RequestMapping("/check")
public class CheckController {

    //storage location
    private final String rootAddress = "/home/ekay/backend/test/";
    //private final String rootAddress = "Consistent/test/";

    @Autowired
    CheckService checkService;

    private final Hashtable<String, SMTCheckRes> checkRes = new Hashtable<>();
    private final Hashtable<String, Z3Util> z3UtilMap = new Hashtable<>();


    //SMT-based Check
    @RequestMapping(value="/smt",method = RequestMethod.GET)
    @ResponseBody
    public SMTRes z3Check(String projectName, int timeout, int bound, boolean period, int pv, boolean deadlock) throws Exception{
        String filename = rootAddress + projectName + "/example.myccsl";

        this.z3UtilMap.put(projectName, new Z3Util(timeout, bound, period, pv, deadlock));
        List<CCSLSet> CCSLSetList = checkService.getCCSLSetList(filename);

        SMTRes result = checkService.smtCheck_user(projectName, CCSLSetList, this.z3UtilMap.get(projectName));

        System.out.println("results:" + result.getName() + "  " + result.getRes());
        return result;
    }

    //SMT-Check locate the unsat sentences
    @RequestMapping(value="/minUnsat",method = RequestMethod.GET)
    @ResponseBody
    public SMTCheckRes minUnsat(String projectName) {
        String filename = rootAddress + projectName + "/example.myccsl";
        this.checkRes.remove(projectName);
        SMTCheckRes unsatRes;
        List<CCSLSet> CCSLSetList = checkService.getCCSLSetList(filename);

        unsatRes = checkService.minUnsat(projectName, CCSLSetList, this.z3UtilMap.get(projectName));
        unsatRes.setName(projectName + "_" + unsatRes.getName());
        this.checkRes.put(projectName, unsatRes);
        return unsatRes;
    }

    @RequestMapping(value = "/getMinUnsatResult", method = RequestMethod.GET)
    @ResponseBody
    public SMTCheckRes getMinUnsatResult(String projectName) {
        System.out.println("这是一次minUnsat结果查询：" + projectName);
        if(!this.checkRes.containsKey(projectName)) {
            SMTCheckRes tmp = new SMTCheckRes();
            tmp.setName("Unfinished");
            System.out.println("还未计算出");
            return tmp;
        }
        else {
            return this.checkRes.get(projectName);
        }
    }

    //Cycle Consistency Check
    @RequestMapping(value="/getConsistent",method = RequestMethod.GET)
    @ResponseBody
    public List<ConsistentRes> getConsistent(String projectName) {
        List<ConsistentRes> consistentList;

        //build the string of project location path
        String filename = rootAddress + projectName + "/example.myccsl";
        List<CCSLSet> CCSLSetList = checkService.getCCSLSetList(filename);

        ClockGraph CG, CCG;

        List<MyConstraint> myConstraintList = new ArrayList<>();
        List<String> ccsls = CCSLSetList.get(0).getCcslList();
        for(int i = 0; i < ccsls.size(); i++) {
            String ccsl = ccsls.get(i);
            MyConstraint myConstraint = new MyConstraint(ccsl);
            myConstraint.setNo(i);
            myConstraintList.add(myConstraint);
        }
        CG = checkService.getCG(myConstraintList);
        CCG = checkService.transToPre(myConstraintList);
        System.out.println("Nodes: " + CG.getClocks().size());
        consistentList = checkService.check_user(projectName, CG, CCG, myConstraintList);

        return consistentList;
    }

}
