package com.example.demo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CheckService {

    @Autowired
    ProjectService projectService;
    //FileService newFileService;

    //Part 0: storage location
    private final String rootAddress = "/home/ekay/backend/test/";
    private final String address = "/home/ekay/backend/";
    //private final String address = "Consistent/test/";

    private List<MyConstraint> PreMyccslList;
    private final List<List<String>> ccsl_circles = new ArrayList<>();
    private final String[] ranges = {"=", "<", "≤", ">", "≥"};


    public List<CCSLSet> getCCSLSetList(String filename) {

        List<CCSLSet> CCSLSetList = new ArrayList<>();

        List<String> ccslList = new ArrayList<>();
        //read '.mycssl' file here to transform into ccsl_set_list format
        //format e.g. -> FileService.writeFile
        try {
            File file = new File(filename);
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(read);
            String strLine = null;
            while (null != (strLine = bufferedReader.readLine())) {
                if (!(strLine.startsWith("//") || strLine.equals(""))) {
                    ccslList.add(strLine);
                    //System.out.println(strLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CCSLSet ccslset = new CCSLSet("CCSL_0", ccslList, null, null);
        CCSLSetList.add(ccslset);
//        System.out.println(JSON.toJSONString(CCSLSetList));
        return CCSLSetList;
    }

    //Part I: SMT-based-check service
    public SMTRes smtCheck_user(String projectName, List<CCSLSet> CCSLSetList, Z3Util z3Util) {
        SMTRes result = new SMTRes();
        for (int i = 0; i < CCSLSetList.size(); i++) {
            CCSLSet ccslset = CCSLSetList.get(i);
            SMTRes smtRes = new SMTRes();
            z3Util.exportSMT(rootAddress + projectName + "/" , ccslset.getCcslList(), ccslset.getId());
            JSONObject resultJson = new JSONObject();
            String command = rootAddress + projectName + "/constraints_" + ccslset.getId() + ".smt2";
            command = "\"" + command + "\"";
            //command = "/usr/local/bin/z3 " + command;
            command = address + "z3/bin/z3 " + command;
            System.out.println("command:" + command);
            StringBuilder resStr = new StringBuilder();

            try {
                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
//                Process process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
                BufferedInputStream bis = new BufferedInputStream(process.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(bis));
                String line;
                while ((line = br.readLine()) != null) {
                    resStr.append(line).append(" ");
                }
                process.waitFor();
                if (process.exitValue() != 0) {
                    System.out.println("z3 error!");
                }
                System.out.println("resStr:" + resStr);
                bis.close();
                br.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            smtRes.setName(projectName + "-SMT-" + i);
            smtRes.setRes(resStr.toString());
            result = smtRes;
        }

        return result;
    }

    public SMTCheckRes minUnsat(String projectName, List<CCSLSet> ccslSets, Z3Util z3Util) {
        SMTCheckRes unsatRes = new SMTCheckRes();

        for (CCSLSet ccslSet : ccslSets) {
            List<String> ccsllist = new ArrayList<>(ccslSet.getCcslList());

            List<String> ccsls = new ArrayList<>();
            int length = ccsllist.size();
            while (true) {
                ccsls.clear();
                String tmpPath = projectName + "/";
                String[] arguments = new String[]{"python3", address + "mstc/mstc.py", rootAddress + tmpPath + "constraints_" + ccslSet.getId() + ".smt2", "-v", "-l", "10"};
                System.out.println("arguments:" + Arrays.toString(arguments));
                try {
                    Process process = Runtime.getRuntime().exec(arguments);
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("================")) continue;
                        ccsls.add(line);
                    }
                    in.close();
                    //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
                    //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
                    int re = process.waitFor();
                    System.out.println("re:" + re);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ccsllist.retainAll(ccsls);
                if (ccsllist.size() == length) {
                    System.out.println(JSON.toJSONString(ccsls));
                    unsatRes.setRes(ccsls);
                    if (!ccsls.isEmpty()) {
                        //读取owl并得到ccsl
//                        Ontology ontology = newFileService.getOntology(projectName, rootAddress + projectName + '/');
//                        CCSLSet owlset = ontology.getCcslSet();
//                        List<String> tmp_con = new ArrayList<>(owlset.getCcslList());
//                        tmp_con.retainAll(ccsls);
//                        if (!tmp_con.isEmpty()) {
//                            unsatRes.setResListEnv(tmp_con);
//                        }

                        //读取情景图并获得ccsl
//                        Project project = newFileService.getScenarioDiagrams(projectName);
//                        List<CCSLSet> ccslset = projectService.sdToCCSL(rootAddress + projectName + '/', project);
//                        for (CCSLSet oneset : ccslset) {
//                            tmp_con = new ArrayList<>(oneset.getCcslList());
//                            tmp_con.retainAll(ccsls);
//                            if (!tmp_con.isEmpty()) {
//                                JSONObject res = new JSONObject();
//                                res.put("id", oneset.getId());
//                                res.put("constrainsts", tmp_con);
//                                unsatRes.setResListScene(res);
//                            }
//                        }
                    }
                    break;
                } else {
                    length = ccsllist.size();
                    z3Util.exportSMT(rootAddress + projectName + "/", ccsllist, ccslSet.getId());
                }

            }
        }

        unsatRes.setName("current_version");
        System.out.println("----------------------minUnsat_user结束----------------------");
        return unsatRes;
    }

    //Part II: CSSL-check service
    public List<ConsistentRes> check_user(String projectName, ClockGraph CG, ClockGraph CCG, List<MyConstraint> myConstraintList) {

        List<ConsistentRes> consistentList = new ArrayList<>();
        String pngPath = rootAddress + projectName + "/";
        //String address = this.address + "/";
        File file = new File(pngPath);
        file.mkdirs();
        writeDot(pngPath + "ClockGraphs/" + projectName + "-CG.dot", CG);
        writeDot(pngPath + "ClockGraphs/" + projectName + "-CCG.dot", CCG);
        int error_no = 1;

        List<ClockGraph> tmp_cCCGs = checkCircle(CCG);

        for (int j = 0; j < tmp_cCCGs.size(); j++) {
            ClockGraph cCCG = tmp_cCCGs.get(j);

            List<MyConstraint> pre_My_constraints = getPreCircle(cCCG);
            ConsistentRes consistent = get_ccsl_circle(projectName, myConstraintList, error_no, pre_My_constraints);
            consistentList.add(consistent);
            error_no += 1;

            ClockGraph RECCG = getRE(cCCG, CCG);
            writeDot(pngPath + "ClockGraphs/" + projectName + "-RECCG-" + j + ".dot", RECCG);
        }

        return consistentList;
    }

    public ConsistentRes get_ccsl_circle(String projectName, List<MyConstraint> myConstraintList, int k,
                                         List<MyConstraint> preMyConstraints) {
        ConsistentRes consistent = new ConsistentRes();
        List<Integer> sources = new ArrayList<>();
        List<String> ccsl_circle = new ArrayList<>();

        for (MyConstraint myConstraint : preMyConstraints) {
            sources.addAll(myConstraint.getSourceList());
        }

        for (MyConstraint myConstraint : myConstraintList) {
            Integer ccsl_no = myConstraint.getNo();
            if (sources.contains(ccsl_no)) {
                ccsl_circle.add(myConstraint.getMyccsl());
            }
        }

        ccsl_circles.add(ccsl_circle);
        for (String ccsl : ccsl_circle) {
            System.out.println(ccsl);
        }


        if (!ccsl_circle.isEmpty()) {
            //读取owl并得到ccsl
//            Ontology ontology = newFileService.getOntology(projectName, rootAddress + projectName + '/');
//            CCSLSet owlset = ontology.getCcslSet();
//            List<String> tmp_con = new ArrayList<>(owlset.getCcslList());
//            tmp_con.retainAll(ccsl_circle);
//            if (!tmp_con.isEmpty()) {
//                consistent.setResListEnv(tmp_con);
//            }

            //读取情景图并获得ccsl
//            Project project = newFileService.getScenarioDiagrams(address + projectName + '/');
//            List<ProjectCCSL> ccslset = projectService.sdToCCSL(address + projectName + '/', project);
//            for (ProjectCCSL oneset : ccslset) {
//                List<String> tmp_con = new ArrayList<>(oneset.getCcsl());
//                tmp_con.retainAll(ccsl_circle);
//                if (!tmp_con.isEmpty()) {
//                    consistent.getResListScene().add(oneset);
//                }
//            }
        }

        consistent.setName(projectName + "-CircleConsistent-" + k);
        consistent.setCcslList(ccsl_circle);
        return consistent;
    }

    public ClockGraph getRE(ClockGraph cCCG, ClockGraph CCG) {
        List<Clock> reClocks = cCCG.getClocks();
        List<Edge> reEdges = cCCG.getEdges();
        List<Clock> tmpClocks = null;
        try {
            tmpClocks = deepCopy(reClocks);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        while (!tmpClocks.isEmpty()) {
            Clock clock = tmpClocks.get(0);
            //补全最小集中的状态
            Clock stateClock = getStatePair(clock, CCG.getClocks());            //从CCG中找到对应的状态节点
            if (stateClock != null && !reClocks.contains(stateClock)) {
                reClocks.add(stateClock);
                tmpClocks.add(stateClock);
                addEdges(stateClock, reClocks, reEdges);
            }
            List<List<Edge>> chains = new ArrayList<>();
            findForeClock(clock, clock, reClocks, reEdges, CCG, null, null, chains);    //寻找限制节点（前节点）
            for (List<Edge> chain : chains) {
                for (Edge edge : chain) {
                    if (!reClocks.contains(edge.getFrom())) {
                        tmpClocks.add(edge.getFrom());
                        reClocks.add(edge.getFrom());
                        addEdges(edge.getFrom(), reClocks, reEdges);
                    }
                    addEdges(edge.getFrom(), reClocks, reEdges);
                }
            }
            tmpClocks.remove(0);
        }

        ClockGraph reCCG = new ClockGraph();
        reCCG.setClocks(reClocks);
        reCCG.setEdges(reEdges);
//		writeDot(rootAddress + filename, reCCG);
        return reCCG;
    }

    public List<ClockGraph> checkCircle(ClockGraph CCG) {
        return getFromIandO(CCG);
    }

    public List<ClockGraph> getFromIandO(ClockGraph CCG) {
        ClockGraph minCCG = new ClockGraph();

        List<Clock> minclocks = null;
        List<Edge> minedges = null;
        List<Clock> tmpC = new ArrayList<>();

        try {
            minclocks = deepCopy(CCG.getClocks());
            minedges = deepCopy(CCG.getEdges());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        assert minclocks != null;
        for (Clock clock : minclocks) {
            if (clock.getFromList().isEmpty() || clock.getToList().isEmpty()) {
                tmpC.add(clock);
            }
        }

        while (!tmpC.isEmpty()) {
            Clock clock = tmpC.get(0);
            List<Edge> fromList = clock.getFromList();
            List<Edge> toList = clock.getToList();
            if (!fromList.isEmpty()) {
                for (int i = fromList.size() - 1; i >= 0; i--) {
                    Edge edge = fromList.get(i);
                    assert minedges != null;
                    minedges.remove(edge);
                    Clock fromClock = edge.getFrom();
                    fromClock.getToList().remove(edge);
                    if (fromClock.getToList().isEmpty()) {
                        tmpC.add(fromClock);
                    }
                }
            } else if (!toList.isEmpty()) {
                for (int i = toList.size() - 1; i >= 0; i--) {
                    Edge edge = toList.get(i);
                    assert minedges != null;
                    minedges.remove(edge);
                    Clock toClock = edge.getTo();
                    toClock.getFromList().remove(edge);
                    if (toClock.getFromList().isEmpty()) {
                        tmpC.add(toClock);
                    }
                }
            }
            minclocks.remove(clock);
            tmpC.remove(0);
        }
        minCCG.setClocks(minclocks);
        minCCG.setEdges(minedges);

        return getCircles(minCCG);
    }

    private ClockGraph getCircle(Clock clock, Clock target, ClockGraph CCG) {
        if (clock.equals(target) && !CCG.getEdges().isEmpty()) {    //如果找到了环
            for (Edge edge : CCG.getEdges()) {
                if (edge.getType().equals("precedence")) {
                    return CCG;
                }
            }
            CCG.getEdges().remove(CCG.getEdges().size() - 1);
            return null;
        }
        CCG.getClocks().add(clock);
        for (Edge edge : clock.getToList()) {
            if (!CCG.getEdges().contains(edge)) {
                CCG.getEdges().add(edge);
                ClockGraph tmp = getCircle(edge.getTo(), target, CCG);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        if (!CCG.getEdges().isEmpty()) {
            CCG.getEdges().remove(CCG.getEdges().size() - 1);
        }
        CCG.getClocks().remove(CCG.getClocks().size() - 1);
        return null;
    }

    private static void findForeClock(Clock target, Clock clock, List<Clock> reClocks, List<Edge> reEdges, ClockGraph CCG, List<Clock> tmpClocks, List<Edge> chain, List<List<Edge>> chains) {
        List<Edge> fromList = new ArrayList<>();
        for (Clock tmpClock : CCG.getClocks()) {
            if (tmpClock.equals(clock)) {
                fromList = tmpClock.getFromList();
            }
        }
        if (tmpClocks == null) {
            chain = new ArrayList<Edge>();
            tmpClocks = new ArrayList<Clock>();
            tmpClocks.add(clock);
        }
        if (fromList.isEmpty()) {
            if (!chain.isEmpty()) {
                List<Edge> tmp = new ArrayList<>();
                tmp.addAll(chain);
                chains.add(tmp);
                chain.remove(chain.size() - 1);
                tmpClocks.remove(tmpClocks.size() - 1);
            }
            return;
        }
        for (Edge edge : fromList) {
            if (reClocks.contains(edge.getFrom()) && hasRoute(edge.getFrom(), target, reEdges, new ArrayList<Edge>())) {
                continue;
            } else if (tmpClocks.contains(edge.getFrom())) {
                if (fromList.size() == 1) {
                    List<Edge> tmp = new ArrayList<Edge>();
                    tmp.addAll(chain);
                    chains.add(tmp);
                }
            } else {
                chain.add(edge);
                tmpClocks.add(edge.getFrom());
                findForeClock(target, edge.getFrom(), reClocks, reEdges, CCG, tmpClocks, chain, chains);
            }
        }
        if (!chain.isEmpty()) {
            chain.remove(chain.size() - 1);
            tmpClocks.remove(tmpClocks.size() - 1);
        }
    }

    private static boolean hasRoute(Clock clock, Clock target, List<Edge> reEdges, List<Edge> route) {
        if (clock.equals(target)) {    //当前节点等于目标节点，则表示已找到其他路径
            return true;
        }
        for (Edge edge : clock.getToList()) {
            if (!route.contains(edge) && reEdges.contains(edge)) {
                route.add(edge);
                if (hasRoute(edge.getTo(), target, reEdges, route)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addEdges(Clock clock, List<Clock> reClocks, List<Edge> reEdges) {
        for (Edge edge : clock.getFromList()) {
            if (reClocks.contains(edge.getFrom())) {
                if (!reEdges.contains(edge)) {
                    reEdges.add(edge);
                }
            }
        }
        for (Edge edge : clock.getToList()) {
            if (reClocks.contains(edge.getTo())) {
                if (!reEdges.contains(edge)) {
                    reEdges.add(edge);
                }
            }
        }
    }

    public void writeDot(String fileName, ClockGraph clockGraph) {
        int index = 1;
        StringBuilder fileContent = new StringBuilder("digraph {\n");
        int i = 1;
        Map<String, String> node_map = new HashMap<String, String>();
        for (Clock clock : clockGraph.getClocks()) {
            fileContent.append("Node_").append(index).append("[label = ").append("\"").append(clock.getClockName()).append("\"]\n");
            node_map.put(clock.getClockName(), "Node_" + index);
            index += 1;
        }
        for (Edge edge : clockGraph.getEdges()) {
            String type = edge.getType();
            switch (type) {
                case "precedence":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("\n");
                    break;
                case "causality":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"blue\",\"style\"=\"dashed\"]\n");
                    break;
                case "exclusion":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"red\",\"style\"=\"dashed\",\"dir\"=\"both\",\"arrowtail\"=\"diamond\",\"arrowhead\"=\"diamond\"]\n");
                    break;
                case "infimum":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"green\"]\n");
                    break;
                case "supremum":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"green\",\"style\"=\"dashed\"]\n");
                    break;
                case "delayFor":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"style\"=\"dashed\"]\n");
                    break;
                case "subClock":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append(" -> ").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"blue\",\"arrowhead\"=\"onormal\"]\n");
                    break;
                case "union":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append("->").append(node_map.get(edge.getTo().getClockName())).append("[\"color\"=\"orange\"]\n");
                    break;
                case "boundedDiff":
                    fileContent.append(node_map.get(edge.getFrom().getClockName())).append("->").append(node_map.get(edge.getTo().getClockName())).append("[label = \"").append(edge.getWeight()).append("\"]\n");
                    break;
            }

        }
        fileContent.append("}");

        File file = new File(fileName);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(fileContent.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("写文件内容操作出错");
            e.printStackTrace();
        }
    }

    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }

    private static Clock getStatePair(Clock clock, List<Clock> clocks) {
        if (clock.getClockName().endsWith(".s")) {
            String stateEnd = clock.getClockName().substring(0, clock.getClockName().length() - 2) + ".f";
            for (Clock c : clocks) {
                if (c.getClockName().equals(stateEnd)) {
                    return c;
                }
            }
        } else if (clock.getClockName().endsWith(".f")) {
            String stateBegin = clock.getClockName().substring(0, clock.getClockName().length() - 2) + ".s";
            for (Clock c : clocks) {
                if (c.getClockName().equals(stateBegin)) {
                    return c;
                }
            }
        }
        return null;
    }

    public List<ClockGraph> getCircles(ClockGraph minCCG) {
        List<ClockGraph> cCCGs = new ArrayList<>();
        for (int i = minCCG.getClocks().size() - 1; i >= 0; i--) {
            Clock clock = minCCG.getClocks().get(i);
            ClockGraph reCCG = getCircle(clock, clock, new ClockGraph());
            if (reCCG == null) {
                minCCG.getClocks().remove(i);
                minCCG.getEdges().removeAll(clock.getFromList());
                minCCG.getEdges().removeAll(clock.getToList());
                continue;
            }
            boolean res = true;
            for (ClockGraph CCG : cCCGs) {
                if (CCG.getEdges().containsAll(reCCG.getEdges())) {
                    res = false;
                    break;
                } else if (reCCG.getEdges().containsAll(CCG.getEdges())) {
                    cCCGs.set(cCCGs.indexOf(CCG), reCCG);
                    res = false;
                }
            }
            if (res) {
                cCCGs.add(reCCG);
            }
        }
        return cCCGs;
    }

    public List<MyConstraint> getPreCircle(ClockGraph cCCG) {
        //ClockGraph RECG = new ClockGraph();
        List<Edge> edges_circle = cCCG.getEdges();
        List<String> pre_circle = new ArrayList<>();
        List<MyConstraint> pre_My_constraints = new ArrayList<>();

        for (Edge edge : edges_circle) {
            pre_circle.add(edge.getCcsl());
        }

        for (MyConstraint pre_My_constraint : this.PreMyccslList) {
            if (pre_circle.contains(pre_My_constraint.getMyccsl())) {
                pre_My_constraints.add(pre_My_constraint);
            }
        }

        return pre_My_constraints;
    }

    public ClockGraph getCG(List<MyConstraint> myConstraintList) {
        return ByMyccsl(myConstraintList);
    }

//    public List<Constraint> getPreMyccslList() {
//        return this.PreMyccslList;
//    }
//
//    public void setPreMyccslList(List<Constraint> preMyccslList) {
//        this.PreMyccslList = preMyccslList;
//    }

    public ClockGraph transToPre(List<MyConstraint> myConstraintList) {
        this.PreMyccslList = new ArrayList<>();
        toPre(myConstraintList);
        for (MyConstraint myConstraint : myConstraintList) {
            List<MyConstraint> preList = myConstraint.getPreMyccslList();
            this.PreMyccslList.addAll(preList);
        }
        exclude(this.PreMyccslList);
        return getCG(this.PreMyccslList);
    }

    private List<MyConstraint> isCause(String a, String b, List<MyConstraint> route, List<MyConstraint> preMyccslList) {
        if (route == null) {
            route = new ArrayList<>();
        }
        if (a.equals(b)) {
            return route;
        }
        for (MyConstraint myccsl : preMyccslList) {
            if (myccsl.getMyccsl().contains(a + "≤")) {
                if (!route.contains(myccsl)) {
                    route.add(myccsl);
                    String clock = myccsl.getMyccsl().split("≤")[1];
                    List<MyConstraint> tmp = isCause(clock, b, route, preMyccslList);
                    if (tmp != null) {
                        return tmp;
                    } else {
                        route.remove(route.size() - 1);
                    }
                }
            }

        }
        return null;
    }

    public void exclude(List<MyConstraint> preMyccslList) {
        for (int i = preMyccslList.size() - 1; i >= 0; i--) {
            String myccsl = preMyccslList.get(i).getMyccsl();
            if (myccsl.contains("#")) {
                String a = myccsl.split("#")[0];
                String b = myccsl.split("#")[1];
                List<MyConstraint> cause = isCause(a, b, null, preMyccslList);
                if (cause != null) {
                    MyConstraint myConstraint = new MyConstraint(a + "<" + b);
                    myConstraint.getSourceList().addAll(preMyccslList.get(i).getSourceList());
                    for (MyConstraint myConstraint2 : cause) {
                        myConstraint.getSourceList().addAll(myConstraint2.getSourceList());
                    }
                    preMyccslList.set(i, myConstraint);
                } else {
                    cause = isCause(b, a, null, preMyccslList);
                    if (cause != null) {
                        MyConstraint myConstraint = new MyConstraint(b + "<" + a);
                        myConstraint.getSourceList().addAll(preMyccslList.get(i).getSourceList());
                        for (MyConstraint myConstraint2 : cause) {
                            myConstraint.getSourceList().addAll(myConstraint2.getSourceList());
                        }
                        preMyccslList.set(i, myConstraint);
                    }
                }
                preMyccslList.remove(i);
            }
        }
    }

    private void toPre(List<MyConstraint> myConstraintList) {
        for (MyConstraint myConstraint : myConstraintList) {
            List<MyConstraint> preMyConstraintList = new ArrayList<>();
            String myccsl = myConstraint.getMyccsl();
            if (myccsl.contains("-")) {

                String a = myccsl.split("-")[0];
                String exp = myccsl.split("-")[1];
                String b = "";
                for (String str : ranges) {
                    if (exp.contains(str)) {
                        b = exp.split(str)[0];
                        break;
                    }
                }
                String myccsl1 = b + "<" + a;
                MyConstraint preMyConstraint1 = new MyConstraint(myccsl1);
                preMyConstraint1.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint1);
            } else if (myccsl.contains("∧")) {
                String a = myccsl.split("=")[0];
                String expression = myccsl.split("=")[1];
                String b = expression.split("∧")[0];
                String c = expression.split("∧")[1];
                String myccsl1 = a + "≤" + b;
                String myccsl2 = a + "≤" + c;
                MyConstraint preMyConstraint1 = new MyConstraint(myccsl1);
                MyConstraint preMyConstraint2 = new MyConstraint(myccsl2);
                preMyConstraint1.getSourceList().add(myConstraint.getNo());
                preMyConstraint2.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint1);
                preMyConstraintList.add(preMyConstraint2);
                for (MyConstraint myConstraint1 : myConstraintList) {
                    if (myConstraint1.getMyccsl().contains("≤" + b)) {
                        String d = myConstraint1.getMyccsl().split("≤")[0];
                        for (MyConstraint myConstraint2 : myConstraintList) {
                            if (myConstraint2.getMyccsl().equals(d + "≤" + c)) {
                                MyConstraint preMyConstraint3 = new MyConstraint(d + "≤" + a);
                                preMyConstraint3.getSourceList().add(myConstraint.getNo());
                                preMyConstraint3.getSourceList().add(myConstraint1.getNo());
                                preMyConstraint3.getSourceList().add(myConstraint2.getNo());
                                preMyConstraintList.add(preMyConstraint3);
                                break;
                            }
                        }
                    }
                }
            } else if (myccsl.contains("∨")) {
                String a = myccsl.split("=")[0];
                String expression = myccsl.split("=")[1];
                String b = expression.split("∨")[0];
                String c = expression.split("∨")[1];
                String myccsl1 = b + "≤" + a;
                String myccsl2 = c + "≤" + a;
                MyConstraint preMyConstraint1 = new MyConstraint(myccsl1);
                MyConstraint preMyConstraint2 = new MyConstraint(myccsl2);
                preMyConstraint1.getSourceList().add(myConstraint.getNo());
                preMyConstraint2.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint1);
                preMyConstraintList.add(preMyConstraint2);
                for (MyConstraint myConstraint1 : myConstraintList) {
                    if (myConstraint1.getMyccsl().contains(b + "≤")) {
                        String d = myConstraint1.getMyccsl().split("≤")[1];
                        for (MyConstraint myConstraint2 : myConstraintList) {
                            if (myConstraint2.getMyccsl().equals(c + "≤" + d)) {
                                MyConstraint preMyConstraint3 = new MyConstraint(a + "≤" + d);
                                preMyConstraint3.getSourceList().add(myConstraint.getNo());
                                preMyConstraint3.getSourceList().add(myConstraint1.getNo());
                                preMyConstraint3.getSourceList().add(myConstraint2.getNo());
                                preMyConstraintList.add(preMyConstraint3);
                                break;
                            }
                        }
                    }
                }
            } else if (myccsl.contains("⊆")) {
                String a = myccsl.split("⊆")[0];
                String b = myccsl.split("⊆")[1];
                String premyccsl = b + "≤" + a;
                MyConstraint preMyConstraint = new MyConstraint(premyccsl);
                preMyConstraint.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint);
            } else if (myccsl.contains("+")) {
                String a = myccsl.split("=")[0];
                String expression = myccsl.split("=")[1];
                String b = expression.split("\\+")[0];
                String c = expression.split("\\+")[1];
                String myccsl1 = a + "≤" + b;
                String myccsl2 = a + "≤" + c;
                MyConstraint preMyConstraint1 = new MyConstraint(myccsl1);
                MyConstraint preMyConstraint2 = new MyConstraint(myccsl2);
                preMyConstraint1.getSourceList().add(myConstraint.getNo());
                preMyConstraint2.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint1);
                preMyConstraintList.add(preMyConstraint2);
            } else if (myccsl.contains("$")) {
                String a = myccsl.split("=")[0];
                String expression = myccsl.split("=")[1];
                String b = expression.split("\\$")[0];
                String premyccsl = b + "<" + a;
                MyConstraint preMyConstraint = new MyConstraint(premyccsl);
                preMyConstraint.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint);
            } else if (myccsl.contains("==")) {
                String a = myccsl.split("==")[0];
                String b = myccsl.split("==")[1];
                String myccsl1 = a + "≤" + b;
                String myccsl2 = b + "≤" + a;
                MyConstraint preMyConstraint1 = new MyConstraint(myccsl1);
                MyConstraint preMyConstraint2 = new MyConstraint(myccsl2);
                preMyConstraint1.getSourceList().add(myConstraint.getNo());
                preMyConstraint2.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint1);
                preMyConstraintList.add(preMyConstraint2);
            } else {
                MyConstraint preMyConstraint = new MyConstraint(myccsl);
                preMyConstraint.getSourceList().add(myConstraint.getNo());
                preMyConstraintList.add(preMyConstraint);
            }
            myConstraint.setPreMyccslList(preMyConstraintList);
        }
    }

    public ClockGraph ByMyccsl(List<MyConstraint> myConstraintList) {
        ClockGraph clockGraph = new ClockGraph();
        List<Clock> clockList = clockGraph.getClocks();
        for (MyConstraint myConstraint : myConstraintList) {
            String myccsl = myConstraint.getMyccsl();
            if (myccsl.contains("−")) {
                Edge edge = new Edge();
                String expression = "";
                String time;
                String weight = "";
                float max = 0.0f;
                float min = 0.0f;
                int maxTag = 0;
                int minTag = 0;

                if (myccsl.contains("=")) {
                    expression = myccsl.split("=")[0].trim();
                    time = myccsl.split("=")[1].trim();
                    weight = "=" + time;
                    min = Float.parseFloat(time);
                    max = Float.parseFloat(time);
                    minTag = 1;
                    maxTag = 1;
                } else if (myccsl.contains("<")) {
                    expression = myccsl.split("<")[0].trim();
                    time = myccsl.split("<")[1].trim();
                    weight = "<" + time;
                    min = 0.0f;
                    minTag = 0;
                    max = Float.parseFloat(time);
                    maxTag = 0;
                } else if (myccsl.contains("≤")) {
                    expression = myccsl.split("≤")[0].trim();
                    time = myccsl.split("≤")[1].trim();
                    weight = "≤" + time;
                    max = Float.parseFloat(time);
                    maxTag = 1;
                } else if (myccsl.contains(">")) {
                    expression = myccsl.split(">")[0].trim();
                    time = myccsl.split(">")[1].trim();
                    weight = ">" + time;
                    min = Float.parseFloat(time);
                    minTag = 0;
                } else if (myccsl.contains("≥")) {
                    expression = myccsl.split("≥")[0].trim();
                    time = myccsl.split("≥")[1].trim();
                    weight = "≥" + time;
                    min = Float.parseFloat(time);
                    minTag = 1;
                }

                String a = expression.split("−")[0];
                String b = expression.split("−")[1];

                if (a.contains("$")) {
                    a = expression.split("\\$")[0];
                }

                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                edge.setType("boundedDiff");
                edge.setWeight(weight);
                edge.setFrom(bclock);
                edge.setTo(aclock);
                edge.setCcsl(myccsl);
                edge.setMin(min);
                edge.setMin_tag(minTag);
                edge.setMax(max);
                edge.setMax_tag(maxTag);
                bclock.getToList().add(edge);
                aclock.getFromList().add(edge);
                bclock.setTo_num(bclock.getTo_num() + 1);
                aclock.setFrom_num(aclock.getFrom_num() + 1);
                clockGraph.getEdges().add(edge);
            } else if (myccsl.contains("<")) {
                Edge edge = new Edge();
                String[] clocks = myccsl.split("<");
                String a = clocks[0].trim();
                String b = clocks[1].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                aclock.getToList().add(edge);
                bclock.getFromList().add(edge);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                edge.setType("precedence");
                edge.setFrom(aclock);
                edge.setTo(bclock);
                edge.setCcsl(myccsl);
                clockGraph.getEdges().add(edge);
            } else if (myccsl.contains("≤")) {
                Edge edge = new Edge();
                String[] clocks = myccsl.split("≤");
                String a = clocks[0].trim();
                String b = clocks[1].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                edge.setType("causality");
                edge.setFrom(aclock);
                edge.setTo(bclock);
                edge.setCcsl(myccsl);
                aclock.getToList().add(edge);
                bclock.getFromList().add(edge);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                clockGraph.getEdges().add(edge);
            } else if (myccsl.contains("#")) {
                Edge edge = new Edge();
                String[] clocks = myccsl.split("#");
                String a = clocks[0].trim();
                String b = clocks[1].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                edge.setType("exclusion");
                edge.setFrom(aclock);
                edge.setTo(bclock);
                edge.setCcsl(myccsl);
                aclock.getToList().add(edge);
                bclock.getFromList().add(edge);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                clockGraph.getEdges().add(edge);
            } else if (myccsl.contains("∧")) {
                String a = myccsl.split("=")[0].trim();
                String expression = myccsl.split("=")[1].trim();
                String[] states = expression.split("∧");
                Clock aclock = getClock(a, clockList);
                for (String state : states) {
                    Edge dot = new Edge();
                    state = state.trim();
                    Clock bclock = getClock(state, clockList);
                    dot.setType("infimum");
                    dot.setFrom(aclock);
                    dot.setTo(bclock);
                    dot.setCcsl(myccsl);
                    aclock.getToList().add(dot);
                    bclock.getFromList().add(dot);
                    aclock.setTo_num(aclock.getTo_num() + 1);
                    bclock.setFrom_num(bclock.getFrom_num() + 1);
                    clockGraph.getEdges().add(dot);
                }
            } else if (myccsl.contains("∨")) {
                String a = myccsl.split("=")[0].trim();
                String expression = myccsl.split("=")[1].trim();
                String[] states = expression.split("∨");
                Clock aclock = getClock(a, clockList);
                for (String state : states) {
                    Edge dot = new Edge();
                    state = state.trim();
                    Clock bclock = getClock(state, clockList);
                    dot.setType("supremum");
                    dot.setFrom(aclock);
                    dot.setTo(bclock);
                    dot.setCcsl(myccsl);
                    aclock.getToList().add(dot);
                    bclock.getFromList().add(dot);
                    aclock.setTo_num(aclock.getTo_num() + 1);
                    bclock.setFrom_num(bclock.getFrom_num() + 1);
                    clockGraph.getEdges().add(dot);
                }
            } else if (myccsl.contains("⊆")) {
                Edge dot = new Edge();
                String[] clocks = myccsl.split("⊆");
                String a = clocks[0].trim();
                String b = clocks[1].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                dot.setType("subClock");
                dot.setFrom(aclock);
                dot.setTo(bclock);
                dot.setCcsl(myccsl);
                aclock.getToList().add(dot);
                bclock.getFromList().add(dot);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                clockGraph.getEdges().add(dot);
            } else if (myccsl.contains("+")) {
                String a = myccsl.split("=")[0].trim();
                String expression = myccsl.split("=")[1].trim();
                String[] states = expression.split("\\+");
                Clock aclock = getClock(a, clockList);
                for (String state : states) {
                    Edge dot = new Edge();
                    state = state.trim();
                    Clock bclock = getClock(state, clockList);
                    dot.setType("union");
                    dot.setFrom(aclock);
                    dot.setTo(bclock);
                    dot.setCcsl(myccsl);
                    aclock.getToList().add(dot);
                    bclock.getFromList().add(dot);
                    aclock.setTo_num(aclock.getTo_num() + 1);
                    bclock.setFrom_num(bclock.getFrom_num() + 1);
                    clockGraph.getEdges().add(dot);
                }
            } else if (myccsl.contains("==")) {
                Edge dot1 = new Edge();
                Edge dot2 = new Edge();
                String[] clocks = myccsl.split("==");
                String a = clocks[0].trim();
                String b = clocks[1].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                dot1.setType("subClock");
                dot1.setFrom(aclock);
                dot1.setTo(bclock);
                dot1.setCcsl(myccsl);
                dot2.setType("subClock");
                dot2.setFrom(bclock);
                dot2.setTo(aclock);
                dot2.setCcsl(myccsl);
                aclock.getToList().add(dot1);
                bclock.getFromList().add(dot1);
                aclock.getFromList().add(dot2);
                bclock.getToList().add(dot2);
                aclock.setFrom_num(bclock.getFrom_num() + 1);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                bclock.setTo_num(aclock.getTo_num() + 1);
                clockGraph.getEdges().add(dot1);
                clockGraph.getEdges().add(dot2);
            } else if (myccsl.contains("$")) {
                Edge dot1 = new Edge();
                String a = myccsl.split("=")[0].trim();
                String expression = myccsl.split("=")[1].trim();
                String b = expression.split("\\$")[0].trim();
                Clock aclock = getClock(a, clockList);
                Clock bclock = getClock(b, clockList);
                dot1.setType("delayFor");
                dot1.setFrom(aclock);
                dot1.setTo(bclock);
                dot1.setCcsl(myccsl);
                aclock.getToList().add(dot1);
                bclock.getFromList().add(dot1);
                aclock.setTo_num(aclock.getTo_num() + 1);
                bclock.setFrom_num(bclock.getFrom_num() + 1);
                clockGraph.getEdges().add(dot1);
            }
        }
        return clockGraph;
    }

    public Clock getClock(String clockName, List<Clock> clocks) {
        for (Clock clock : clocks) {
            if (clock.getClockName().equals(clockName)) {
                return clock;
            }
        }
        Clock clock = new Clock(clockName);
        clocks.add(clock);
        return clock;
    }

}
