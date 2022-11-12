package com.example.demo.util;

/**
 * @author yizai
 * @since 2022/11/11 11:52
 */
public class ScenarioRVConstants {
    /**
     * 路径配置
     */
    public static String rootAddress = "/Users/yizai/Env-Consistency/Project/";
    public static String userAddress = "/Users/yizai/Env-Consistency/env-projects";
    public static String dataAddress = "/Users/yizai/Env-Consistency/Data/program/case/";
    /**
     * 状态节点的标志
     */
    public static final String TAG_STATE = "(state)";
    /**
     * 匹配状态节点标志的正则表达式
     */
    public static final String TAG_STATE_REGEX = "\\(state\\)";
    /**
     * UNION 关系的连接符
     */
    public static final String TO_CCSL_HYPHEN = "__UN__";
    /**
     * StrictPre 约束，正则表达式和其相同
     */
    public static final String STRICT_PRE = "<";
}
