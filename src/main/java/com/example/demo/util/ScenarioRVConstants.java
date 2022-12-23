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
    public static final String UNION_STR = "+";
    /**
     * 匹配 UNION 关系的正则表达式
     */
    public static final String UNION_STR_REGEX = "\\+";
    /**
     * StrictPre 约束，正则表达式和其相同
     */
    public static final String STRICT_PRE = "<";
    /**
     * Alternately 约束，正则表达式和其相同
     */
    public static final String ALTER_STR = "~";
    /**
     * Exclusive 约束，正则表达式和其相同
     */
    public static final String EXCLUSIVE = "#";
    /**
     * Infimum 约束
     */
    public static final String INFIMUM = "˄";
    /**
     * 匹配 Infimum 约束的正则表达式
     */
    public static final String INFIMUM_REGEX = "\\˄";
    /**
     * Supremum 约束
     */
    public static final String SUPREMUM = "˅";
    /**
     * 匹配 Supremum 约束的正则表达式
     */
    public static final String SUPREMUM_REGEX = "\\˅";
    /**
     * Subclock 约束，正则表达式和其相同
     */
    public static final String SUBCLOCK = "⊆";
    /**
     * DelayFor 约束，正则表达式和其相同
     */
    public static final String DELAYFOR = "$";
    /**
     * Synchrony 关系
     */
    public static final String SYNC = "Synchrony";
}
