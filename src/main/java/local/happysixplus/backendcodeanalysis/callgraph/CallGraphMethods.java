package local.happysixplus.backendcodeanalysis.callgraph;

import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface CallGraphMethods{
    /*
     *
     * @param githubLink 项目的github连接，要求public项目，且完全符合maven规范
     *
     * @param projectName 项目名称，要求和github项目根目录名称相同
     *
     *
     * @return Pair的第一个元素：每一行为一个代码依赖 第二个元素：String的ArrayList，每个String为一个函数的源码
     *
     */
    public Pair<String[],ArrayList<String>> initGraph(String githubLink, String projectName);
}