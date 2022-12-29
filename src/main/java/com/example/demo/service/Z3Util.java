package com.example.demo.service;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Z3Util {
    String[] sym=new String[]{"<","≤","⊆","#","\\+","\\*","∧","∨","\\$","∝","☇","|","=="};
    int timeout;
    String s;
    int b;
    List<String> clocks = new LinkedList<>();
    int pb;
    boolean dl;
    boolean p;
    String res="",tmp="",pec="",ncl="",ltl="",dlk="",filter="";
    int lct = 0;

    public Z3Util(int timeout, int b, boolean p, int pb, boolean dl) {
        this.timeout = timeout;
        this.b = b;
        this.pb = pb;
        this.dl = dl;
        this.p = p;
    }

    public void exportSMT(String path, List<String> ccsls, String filename){
        String str;
        int strb;
        res = "";
        res += "(set-option :print-success false)\n";
        res += "(set-option :produce-models true)\n";
        res += "(set-logic AUFLIRA)\n";
        res += "(declare-fun n () Int)\n";
        if(b > 0)
            res += "(assert (= n " + b + "))\n";
        res += "(declare-fun d () Int)\n";
        res += "(assert (> d 0))\n";
        if(p){
            res += "(declare-fun k () Int)\n";
            res += "(declare-fun l () Int)\n";
            res += "(declare-fun p () Int)\n";
            res += "(assert (> l 0))\n";
            if(pb == 0)
                res += "(assert (> p 0))\n";
            else
                res += "(assert (= p " + pb + "))\n";
            res += "(assert (= k (+ l p)))\n";
            res += "(assert (<= k n))\n";
        }
        if(dl){
            res += "(declare-fun np () Int)\n";
            res += "(assert (= np (+ n 1)))\n";
            res += "(declare-fun npp () Int)\n";
            res += "(assert (= npp (+ n 2)))\n";
        }
        for(int i = 0;i < ccsls.size();i++){
            String strs[];
            str = ccsls.get(i);
            if(str.equals("") || str.charAt(0)=='/' || str.charAt(0)==' '){
            }
            else if(str.charAt(0) == '('){
                tmp += str + "\n";
            }
            else if(str.charAt(0) == '-'){
                ncl = "";
                str = str.substring(2);
                strs = str.split(",");
                for(int j = 0;j < strs.length; j ++ ){
                    init(strs[j],true);
                }
                if (strs.length == 1)
                    tmp += "(assert (not" + ncl + "))\n";
                else
                    tmp += "(assert (not (and" + ncl + ")))\n";
            }
            else if(str.charAt(0) == '='){
                ltl="";
                str=str.substring(2);
                strs=str.split(",");
                for(int j = 0;j < strs.length; j ++ ){
                    ltl += " " + addl(strs[j],"1");
                }
                if(strs.length == 1)
                    tmp += "(assert (not" + ltl + "))\n";
                else
                    tmp += "(assert (not (and" + ltl + ")))\n";
            }
            else if(str.charAt(0) == '@'){
                str = str.substring(1);
                strb = 0;
                str = readFile(str);
                strs = str.split("\n");
                for(int j = 0;j < strs.length; j ++ ){
                    String[] sss = strs[j].split(" ");
                    strb = sss.length - 1;
                    for(int k = 1;k < sss.length; k ++ ){
                        tmp += "(assert (= (t_" + sss[0] + " " + k + ") " + sss[k] + "))\n";
                    }
                }
                if(b == 0)
                    System.out.println("The length of trace is " + strb + ". You should set a bound to check the trace first.");
            }
            else{
                init(str,false);
            }
        }
        for(int i = 0;i < clocks.size(); i ++ ){
//        	System.out.println(clocks.get(i));
            res += "(declare-fun t_" + clocks.get(i) + " (Int) Bool)\n";
            res += "(declare-fun h_" + clocks.get(i) + " (Int) Int)\n";
            res += "(assert (= (h_" + clocks.get(i) + " 1) 0))\n";
            if(b > 0){
                for(int j = 1;j <= b;j ++ ){
                    res += "(assert (ite (t_" + clocks.get(i) + " " + j + ") (= (h_" + clocks.get(i) + " " + ( j + 1 ) +
                            ") (+ (h_" + clocks.get(i) + " " + j + ") 1)) (= (h_" + clocks.get(i) + " " + ( j + 1 ) +
                            ") (h_" + clocks.get(i) + " " + j + "))))\n";
                }
            }
            else if(!p){
                res += "(assert (forall ((x Int)) (=> (>= x 1) ";
                res += "(ite (t_" + clocks.get(i) + " x) (= (h_" + clocks.get(i) + " (+ x 1)) (+ (h_" + clocks.get(i) +
                        " x) 1)) (= (h_" + clocks.get(i) + " (+ x 1)) (h_" + clocks.get(i) + " x))))))\n";
            }
            else{
                res += "(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) ";
                res += "(ite (t_" + clocks.get(i) + " x) (= (h_" + clocks.get(i) + " (+ x 1)) (+ (h_" + clocks.get(i) +
                        " x) 1)) (= (h_" + clocks.get(i) + " (+ x 1)) (h_" + clocks.get(i) + " x))))))\n";
            }
        }
        res += "(declare-fun t_idealClock (Int) Bool)\n";
        res += "(declare-fun h_idealClock (Int) Int)\n";
        res += "(assert (= (h_idealClock 1) 0))\n";
        if(b > 0){
            for(int j = 1;j <= b;j ++ ){
                res += "(assert (ite (t_idealClock " + j + ") (= (h_idealClock " + ( j + 1 ) + ") (+ (h_idealClock " + j +
                        ") 1)) (= (h_idealClock " + ( j + 1 ) + ") (h_idealClock " + j + "))))\n";
            }
        }
        else if(!p){
            res += "(assert (forall ((x Int)) (=> (>= x 1) ";
            res += "(ite (t_idealClock x) (= (h_idealClock (+ x 1)) (+ (h_idealClock x) 1)) (= (h_idealClock (+ x 1)) (h_idealClock x))))))\n";
        }
        else{
            res += "(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) ";
            res += "(ite (t_idealClock x) (= (h_idealClock (+ x 1)) (+ (h_idealClock x) 1)) (= (h_idealClock (+ x 1)) (h_idealClock x))))))\n";
        }
        if(b == 0 && !p)
            res += "(assert (forall ((x Int)) (=> (>= x 1) ";
        else
            res += "(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) ";
        res += "(= (t_idealClock x) true)";
        res += ")))\n";
        //at least one clock tick
//        if(clocks.size() > 0){
//            if(b == 0 && !p)
//            	res += "(assert (forall ((x Int)) (=> (>= x 1) (or";
//            else
//            	res += "(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (or";
//            for(int i = 0; i < clocks.size(); i ++ ){
//                res += " (t_" + clocks.get(i) + " x)";
//            }
//            res += "))))\n";
//        }
        if (clocks.size() > 0) {
            int tmpb = b + 1;
            //allow a clock not to tick all the time
            if (b == 0 && !p) res += "(assert (forall ((x Int)) (=> (>= x 1) (or";
            else{
                //每个时刻都至少有一个时钟tick
                res += "(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (or";
                for (int i = 0; i < clocks.size(); i++) {
                    res += " (t_" + clocks.get(i) + " x)";
                }
                res += "))))\n";

                //所有时钟都至少tick一次
                if(clocks.size() == 1) res += "(assert (> (h_" + clocks.get(0) + " " + tmpb + ") 0))";
                else{
                    res += "(assert (and ( > (h_" + clocks.get(0) + " " + tmpb + ") 0)";
                    for(int i = 1;i < clocks.size();i++){
                        res += " (> (h_" + clocks.get(i) + " " + tmpb + ") 0)";
                    }
                    res += "))\n";
                }
            }
        }
        tmp += filter;
        if(p)
            tmp += pec;
        if(dl){
            tmp += "(assert (forall (";
            for(int i = 0; i < clocks.size(); i ++ ){
                tmp += "(t" + clocks.get(i) + " Bool)";
            }
            tmp += ") (=> (or";
            for(int i = 0; i < clocks.size(); i ++ ){
                tmp += " t" + clocks.get(i);
            }
            tmp += ") (exists (";
            for(int i = 0; i < clocks.size(); i ++ ){
                tmp += "(h" + clocks.get(i) + " Int)";
            }
            tmp += ") (and";
            for(int i = 0; i < clocks.size(); i ++ ){
                tmp += " (ite t" + clocks.get(i) + " (= h" + clocks.get(i) + " (+ (h_" + clocks.get(i) + " np) 1)) (= h" + clocks.get(i)
                        + " (h_" + clocks.get(i) + " np)))";
            }
            tmp += " (not (and" + dlk + ")))))))\n";
        }
        res += tmp;
        res += "(check-sat)\n";
        if(p){
            res += "(get-value (l))\n";
            res += "(get-value (p))\n";
            res += "(get-value (k))\n";
        }
        if(b > 0){
            for(int i = 0; i < clocks.size(); i ++ ){
                for(int j = 1; j <= b; j ++ ){
                    res += "(get-value ((t_" + clocks.get(i) + " " + j + ")))\n";
                }
            }
        }

        // 检查路径是否存在
        File filePath = new File(path);
        if(!filePath.exists()){
            filePath.mkdirs();
        }
        writeFile( path + "constraints_" + filename + ".smt2", res);
    }

    public String trace(){
        String[] ss=s.split("\n");
        String result="";
        if(!ss[0].equals("sat")&&!ss[0].equals("unknown")) result="";
        else{
            if(b==0) return result="";
            else{
                String tname="";
                for(int i=1;i<ss.length;i++){
                    String[] sss =ss[i].split(" ");
                    if(sss.length==3){
                        String name=sss[0].split("\\(t_")[1];
                        //id=sss[1].split(")")[0]-'0';
                        String val=sss[2].split("\\)")[0];
                        if(!name.equals(tname)){
                            if(tname.equals("")) result+=name+" "+val;
                            else result+="\n"+name+" "+val;
                        }
                        else result+=" "+val;
                        tname=name;
                    }
                }
            }
        }
        return result;
    }

    private String readFile(String filePath){
        StringBuffer result = new StringBuffer("");
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)),
                    "UTF-8"));
            String lineTxt = null;
            while((lineTxt = br.readLine()) != null){
                result.append(lineTxt);
                result.append('\n');
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }

    private void writeFile(String filePath, String content){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath);
            String[] strs = content.split("\n");
            for(int i = 0;i < strs.length;i++){
                fileWriter.write(strs[i] + "\r\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String pres(String str){
        return str.replace(" ","");
    }

    private void init(String str, boolean flag){
        for(int i = 0; i < sym.length; i ++ ){
            String tempSym = sym[i];
            if(tempSym.contains("\\")) tempSym = tempSym.substring(1);
            if(str.indexOf(tempSym) >= 0){
                String[] c;
                int ii;
                if(i < 4){
                    c = str.split(tempSym);
                    addc(c[0]);
                    addc(c[1]);
                    ii = i;
                }
                else if(i == 4){
                    String[] opt = str.split("\\+");
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = opt[1];
                    for(int j = 0;j < 3; j ++ )
                        addc(c[j]);
                    ii = i;
                }
                else if(i == 5){
                    String[] opt = str.split("\\*");
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = opt[1];
                    for(int j = 0;j < 3; j ++ )
                        addc(c[j]);
                    ii = i;
                }
                else if(i < 8){
                    String[] opt = str.split(tempSym);
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = opt[1];
                    for(int j = 0;j < 3; j ++ )
                        addc(c[j]);
                    ii = i;
                }
                else if(i == 8){
                    String[] opt = str.split("\\$");
                    c = new String[4];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    if(opt[1].indexOf(" on ") >= 0){
                        c[2] = opt[1].split(" on ")[0];
                        c[3] = opt[1].split(" on ")[1];
                        addc(c[0]);addc(c[1]);
                        if(!c[3].equals("idealClock"))
                            addc(c[3]);
                        ii = i + 1;
                    }
                    else{
                        c[2] = opt[1];
                        addc(c[0]);
                        addc(c[1]);
                        ii = i;
                    }
                }
                else if(i == 9){
                    String[] opt = str.split(tempSym);
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = opt[1];
                    addc(c[0]);
                    addc(c[1]);
                    ii = i + 1;
                }
                else if(i == 10){
                    String[] opt = str.split(tempSym);
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = opt[1];
                    addc(c[0]);
                    addc(c[1]);
                    addc(c[2]);
                    ii = i + 1;
                }
                else if(i == 11){
                    String[] opt = str.split("\\|");
                    c = new String[3];
                    c[0] = opt[0].split("=")[0];
                    c[1] = opt[0].split("=")[1];
                    c[2] = c[0] + "F" + c[1];
                    addc(c[0]);
                    addc(c[1]);
                    addc(c[2]);
                    ii = 5;
                    filterfunc(c[2],opt[1]);
                }
                else {
                    String[] opt = str.split(tempSym);
                    c = new String[2];
                    c[0]=opt[0];
                    c[1]=opt[1];
                    addc(c[0]);
                    addc(c[1]);
                    ii = i;
                }
                if(flag) {
                    ncl += addt(c,ii);
                }else {
                    tmp += "(assert" + addt(c,ii) + ")\n";
//                	System.out.println("***this error***");
//                	System.out.println("(assert" + addt(c,ii) + ")");
                }
                if(dl)
                    dlk += addd(c,ii);
                break;
            }
        }
    }

    private void filterfunc(String c,String str){
        int left=str.indexOf("(")+1;
        int right=str.indexOf(")");
        int p1=right-left;
        String ss=str.replace("(","").replace(")","");
        String tmp="";
        for(int i=0;i<ss.length();i++){
            tmp+="(= (t_"+c+" "+(i+1)+") "+(ss.charAt(i)=='0'?false:true)+") ";
        }
        tmp+="(=> (> x "+ss.length()+") (= (t_"+c+" x) (t_"+c+" (+ "+left+" (mod (- x "+left+") "+p+")))))";

        if(b>0||p1>0) filter+="(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (and "+tmp+"))))\n";
        else filter+="(assert (forall ((x Int)) (=> (>= x 1) (and "+tmp+"))))\n";
    }

    private void addc(String c){
        boolean flg = true;
        for(int i = 0;i < clocks.size();i++){
            if(clocks.get(i).equals(c)){
                flg = false;
                break;
            }
        }
        if(flg) clocks.add(c);
    }

    private String addt(String[] c,int i){
        String ftmp = "";
//        System.out.println(i);
        switch(i){
            //<
            case 0:
                pec += "(assert (and (= (t_"+c[0]+" l) (t_"+c[0]+" k)) (= (t_"+c[1]+" l) (t_"+c[1]+" k))))\n";
                pec += "(assert (>= (- (h_" + c[0] + " k) (h_" + c[0] + " l)) (- (h_" + c[1] + " k) (h_" + c[1] + " l))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (h_" + c[0] + " x) (h_" + c[1] + " x)) (not (t_" + c[1] + " x))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (h_" + c[0] + " x) (h_" + c[1] + " x)) (not (t_" + c[1] + " x))))";
                break;
            //<=
            case 1:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k))))\n";
                pec += "(assert (>= (- (h_" + c[0] + " k) (h_" + c[0] + " l)) (- (h_" + c[1] + " k) (h_" + c[1] + " l))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (>= (h_" + c[0] + " x) (h_" + c[1] + " x))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (>= (h_" + c[0] + " x) (h_" + c[1] + " x))))";
                break;
            //sub
            case 2:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (=> (t_" + c[0] + " x) (t_" + c[1] + " x))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (=> (t_" + c[0] + " x) (t_" + c[1] + " x))))";
                break;
            //#
            case 3:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k))))\n";

                if(b > 0 || p)
                    ftmp=" (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (or (not (t_" + c[0] + " x)) (not (t_" + c[1] + " x)))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (or (not (t_" + c[0] + " x)) (not (t_" + c[1] + " x)))))";
                break;
            //+
            case 4:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] + " k))))\n";

                if( b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (or (t_" + c[1] + " x) (t_" + c[2] + " x)))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (or (t_" + c[1] + " x) (t_" + c[2] + " x)))))";
                break;
            //x
            case 5:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] + " k))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (and (t_" + c[1] + " x) (t_" + c[2] + " x)))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (and (t_" + c[1] + " x) (t_" + c[2] + " x)))))";
                break;
            //and
            case 6:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] + " k))))\n";
                pec += "(assert (= (- (h_" + c[0] + " k) (h_" + c[0] + " l)) (- (h_" + c[1] + " k) (h_" + c[1] + " l)) (- (h_" + c[2] + " k) (h_" + c[2] + " l))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_" + c[1] + " x) (h_" + c[2] + " x)) (= (h_" + c[0] +
                            " x) (h_" + c[1] + " x)) (= (h_" + c[0] + " x) (h_" + c[2] + " x)))))";
                else ftmp = " (forall ((x Int)) (=> (>= x 1) (ite (>= (h_" + c[1] + " x) (h_" + c[2] + " x)) (= (h_" + c[0] + " x) (h_" + c[1] +
                        " x)) (= (h_" + c[0] + " x) (h_" + c[2] + " x)))))";
                break;
            //or
            case 7:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] + " k))))\n";
                pec += "(assert (= (- (h_" + c[0] + " k) (h_" + c[0] + " l)) (- (h_" + c[1] + " k) (h_" + c[1] + " l)) (- (h_" + c[2] + " k) (h_" + c[2] + " l))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_" + c[1] + " x) (h_" + c[2] + " x)) (= (h_" + c[0] + " x) (h_" + c[2] +
                            " x)) (= (h_" + c[0] + " x) (h_" + c[1] + " x)))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (ite (>= (h_" + c[1] + " x) (h_" + c[2] + " x)) (= (h_" + c[0] + " x) (h_" + c[2] +
                            " x)) (= (h_" + c[0] + " x) (h_" + c[1] + " x)))))";
                break;
            //delay
            case 8:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k))))\n";
                pec += "(assert (and (>= (h_" + c[1] + " l) " + c[2] + ") (= (- (h_" + c[0] + " k) (h_" + c[0] + " l)) (- (h_" + c[1] + " k) (h_" + c[1] + " l)))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_" + c[1] + " x) " + c[2] + ") (= (h_" + c[0] + " x) (- (h_" +
                            c[1] + " x) " + c[2] + ")) (= (h_" + c[0] + " x) 0))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (ite (>= (h_" + c[1] + " x) " + c[2] + ") (= (h_" + c[0] + " x) (- (h_" + c[1] + " x) " + c[2] +
                            ")) (= (h_" + c[0] + " x) 0))))";
                break;
            //delayfor
            case 9:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[3] + " l) (t_" + c[3] +
                        " k))))\n";
                if(c[3].equals("idealClock")){
                    pec += "(assert (and (> l " + c[2] + ") (= (t_" + c[0] + " (- l " + c[2] + ")) (t_" + c[0] + " (- k " + c[2] + "))) (= (t_" + c[1] +
                            " (- l " + c[2] + ")) (t_" + c[1] + " (- k " + c[2] + ")))))\n";

                    if(b > 0 || p)
                        ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (and (> x " + c[2] + ") (t_" + c[1] + " (- x " + c[2] + "))))))";
                    else
                        ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (and (> x " + c[2] + ") (t_" + c[1] + " (- x " + c[2] + "))))))";
                }
                else{
                    pec += "(assert (forall ((x Int)) (=> (and (>= x l) (< x k)) (and (t_" + c[3] + " x) (exists ((m Int)) (=> (and (>= m 1) (t_" + c[1] + " m) (= (- (h_" + c[3] + " x) (h_" + c[3] + " m)) " + c[2] + ")) (>= m l)))))))\n";
                    pec += "(assert (forall ((x Int)) (=> (and (>= x 1) (< x k)) (t_" + c[1] + " x) (exists ((m Int)) (and (>= m 1) (t_" + c[0] + " m) (>= m x) (< m k))))))\n";

                    if(b > 0 || p)
                        ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (and (t_" + c[3] + " x) (exists ((m Int)) (and (>= m 1) (< m x) (t_" +
                                c[1] + " m) (= (- (h_" + c[3] + " x) (h_" + c[3] + " m)) " + c[2] + ")))))))";
                    else
                        ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (and (t_" + c[3] + " x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_" +
                                c[1] + " m) (= (- (h_" + c[3] + " x) (h_" + c[3] + " m)) " + c[2] + ")))))))";
                }
                break;
            //periodic
            case 10:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] + " k))))\n";
                pec += "(assert (= (mod (- (h_" + c[2] + " k) (h_" + c[2] + " l)) " + c[1] + ") 0))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (and (t_" + c[1] + " x) (> (h_" + c[1] +
                            " x) 0) (= (mod (h_" + c[1] + " x) " + c[2] + ") 0)))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (and (t_" + c[1] + " x) (> (h_" + c[1] + " x) 0) (= (mod (h_" + c[1] +
                            " x) " + c[2] + ") 0)))))";
                break;
            //simpleon
            case 11:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k)) (= (t_" + c[2] + " l) (t_" + c[2] +
                        " k))))\n";
                pec += "(assert (forall ((x Int)) (=> (and (>= x l) (< x k)) (and (t_" + c[2] + " x) (exists ((m Int)) (=> (and (>= m 1) (= (- (h_" + c[2] +
                        " x) (h_" + c[2] + " m)) 1) (>= (- (h_" + c[1] + " x) (h_" + c[1] + " m)) 1)) (>= m l)))))))\n";
                pec += "(assert (forall ((x Int)) (=> (and (>= x l) (< x k)) (t_" + c[1] + " x) (exists ((y Int)(z Int)) (and (> y x) (< y k) (> z x) (< z k) (t_" +
                        c[2] + " y) (t_" + c[2] + " z))))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (and (t_" + c[2] + " x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_" +
                            c[2] + " m) (= (- (h_" + c[2] + " x) (h_" + c[2] + " m)) 1) (>= (- (h_" + c[1] + " x) (h_" + c[1] + " m)) 1)))))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (and (t_" + c[2] + " x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_" +
                            c[2] + " m) (= (- (h_" + c[2] + " x) (h_" + c[2] + " m)) 1) (>= (- (h_" + c[1] + " x) (h_" + c[1] + " m)) 1)))))))";
                break;
            //==
            case 12:
                pec += "(assert (and (= (t_" + c[0] + " l) (t_" + c[0] + " k)) (= (t_" + c[1] + " l) (t_" + c[1] + " k))))\n";

                if(b > 0 || p)
                    ftmp = " (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_" + c[0] + " x) (t_" + c[1] + " x))))";
                else
                    ftmp = " (forall ((x Int)) (=> (>= x 1) (= (t_" + c[0] + " x) (t_" + c[1] + " x))))";
                break;
        }
        return ftmp;
    }

    private String addd(String[] c,int i){
        String ftmp="";
        switch(i){
            //<
            case 0:
                ftmp=" (=> (= (h_"+c[0]+" np) (h_"+c[1]+" np)) (not t"+c[1]+"))";
                break;
            //<=
            case 1:
                //ftmp=" (>= (h_"+c[0]+" npp) (h_"+c[1]+" npp))";
                ftmp=" (>= h"+c[0]+" h"+c[1]+")";
                break;
            //sub
            case 2:
                ftmp=" (=> t"+c[0]+" t"+c[1]+")";
                break;
            //#
            case 3:
                ftmp=" (or (not t"+c[0]+") (not t"+c[1]+"))";
                break;
            //+
            case 4:
                ftmp=" (= t"+c[0]+" (or t"+c[1]+" t"+c[2]+"))";
                break;
            //x
            case 5:
                ftmp=" (= t"+c[0]+" (and t"+c[1]+" t"+c[2]+"))";
                break;
            //and
            case 6:
                //ftmp=" (ite (>= (h_"+c[1]+" npp) (h_"+c[2]+" npp)) (= (h_"+c[0]+" npp) (h_"+c[1]+" npp)) (= (h_"+c[0]+" npp) (h_"+c[2]+" npp)))";
                ftmp=" (ite (>= h"+c[1]+" h"+c[2]+") (= h"+c[0]+" h"+c[1]+") (= h"+c[0]+" h"+c[2]+"))";
                break;
            //or
            case 7:
                //ftmp=" (ite (>= (h_"+c[1]+" npp) (h_"+c[2]+" npp)) (= (h_"+c[0]+" npp) (h_"+c[2]+" npp)) (= (h_"+c[0]+" npp) (h_"+c[1]+" npp)))";
                ftmp=" (ite (>= h"+c[1]+" h"+c[2]+") (= h"+c[0]+" h"+c[2]+") (= h"+c[0]+" h"+c[1]+"))";
                break;
            //delay
            case 8:
                //ftmp=" (ite (>= (h_"+c[1]+" npp) "+c[2]+") (= (h_"+c[0]+" npp) (- (h_"+c[1]+" npp) "+c[2]+")) (= (h_"+c[0]+" npp) 0))";
                ftmp=" (ite (>= h"+c[1]+" "+c[2]+") (= h"+c[0]+" (- h"+c[1]+" "+c[2]+")) (= h"+c[0]+" 0))";
                break;
            //delayfor
            case 9:
                if(c[3].equals("idealClock")){
                    //ftmp=" (= t"+c[0]+" (exists ((m Int)) (and (>= m 1) (< m np) (t_"+c[1]+" m) (= (- (h_"+c[3]+" np) (h_"+c[3]+" m)) "+c[2]+"))))";
                    ftmp=" (= t"+c[0]+" (and (> np "+c[2]+") (t_"+c[1]+" (- np "+c[2]+"))))";
                }
                else ftmp=" (= t"+c[0]+" (and t"+c[3]+" (exists ((m Int)) (and (>= m 1) (<= m np) (t_"+c[1]+" m) (= (- (h_"+c[3]+" np) (h_"+c[3]+" m)) "+c[2]+")))))";
                break;
            //periodic
            case 10:
                ftmp=" (= t"+c[0]+" (and t"+c[1]+" (> (h_"+c[1]+" np) 0) (= (mod (h_"+c[1]+" np) "+c[2]+") 0)))";
                break;
        }
        return ftmp;
    }

    private String po(int i){
        if(i==0) return "x"+lct;
        else return "x"+lct++;
    }

    private String addl(String str, String step){
        String s1;
        if(str.charAt(0)=='¬'){
            s1=str.substring(2,str.length()-1);
            return "(not "+addl(s1,step)+")";
        }
        else if(str.charAt(0)=='X'){
            s1=str.substring(2,str.length()-1);
            if(!p) return "(ite (< "+step+" n) (exists (("+po(0)+" Int)) (and (= "+po(0)+" (+ "+step+" 1)) "+addl(s1,po(1))+")) false)";
            else return "(ite (< "+step+" k) (exists (("+po(0)+" Int)) (and (= "+po(0)+" (+ "+step+" 1)) "+addl(s1,po(1))+")) (exists (("+po(0)+" Int)) (and (= "+po(0)+" (+ l 1)) "+addl(s1,po(1))+")))";
        }
        else if(str.charAt(0)=='F'){
            s1=str.substring(2,str.length()-1);
            if(!p) return "(exists (("+po(0)+" Int)) (and (>= "+po(0)+" "+step+") (<= "+po(0)+" k) "+addl(s1,po(1))+"))";
            else return "(ite (< "+step+" l) (exists (("+po(0)+" Int)) (and (>= "+po(0)+" "+step+") (<= "+po(0)+" k) "+addl(s1,po(1))+")) (exists (("+po(0)+" Int)) (and (>= "+po(0)+" l) (<= "+po(0)+" k) "+addl(s1,po(1))+")))";
        }
        else if(str.charAt(0)=='G'){
            s1=str.substring(2,str.length()-1);
            if(!p) return "false";
            else return "(ite (< "+step+" l) (forall (("+po(0)+" Int)) (=> (and (>= "+po(0)+" "+step+") (<= "+po(0)+" k)) "+addl(s1,po(1))+")) (forall (("+po(0)+" Int)) (=> (and (>= "+po(0)+" l) (<= "+po(0)+" k)) "+addl(s1,po(1))+")))";
        }
        else if(str.charAt(0)=='('){
            int bral=0,brar=0,bram=0;
            for(int i=0;i<str.length();i++){
                if(str.charAt(i)=='(') bral++;
                if(str.charAt(i)==')') brar++;
                if(bral==brar){
                    bram=i;
                    break;
                }
            }
            String s2;
            char s3;
            s1=str.substring(1,bram);
            s2=str.substring(bram+3,str.length()-1);
            s3=str.charAt(bram+1);
            if(s3=='∧'){
                return "(and "+addl(s1,step)+" "+addl(s2,step)+")";
            }
            else if(s3=='∨'){
                return "(or "+addl(s1,step)+" "+addl(s2,step)+")";
            }
            else if(s3=='→'){
                return "(=> "+addl(s1,step)+" "+addl(s2,step)+")";
            }
            else if(s3=='U'){
                String po1,po2,po3,po4,po5;
                po1=po(1);po2=po(1);
                if(!p) return "(exists (("+po1+" Int)) (and (>= "+po1+" "+step+") (<= "+po1+" n) "+addl(s2,po1)+" (forall (("+po2+" Int)) (=> (and (>= "+po2+" "+step+") (< "+po2+" "+po1+")) "+addl(s1,po2)+"))))";
                else {
                    po3=po(1);po4=po(1);po5=po(1);
                    return "(or (exists (("+po1+" Int)) (and (>= "+po1+" "+step+") (<= "+po1+" k) "+addl(s2,po1)+" (forall (("+po2+" Int)) (=> (and (>= "+po2+" "+step+") (< "+po2+" "+po1+")) "+addl(s1,po2)+")))) (exists (("+po3+" Int)) (and (>= "+po3+" l) (<= "+po3+" k) "+addl(s2,po1)+" (forall (("+po4+" Int)("+po5+" Int)) (=> (and (>= "+po4+" l) (< "+po4+" "+po3+") (>= "+po5+" "+step+") (<= "+po5+" k)) (and "+addl(s1,po4)+" "+addl(s1,po5)+"))))))";
                }
            }
            else{
                return "false";
            }
        }
        else return "(t_"+str+" "+step+")";
    }



    public static void main(String[] args){
//        Z3Util test = new Z3Util(10, "./constraints.myccsl",10,0,false,false);
//        test.exportSMT();
    }
}
