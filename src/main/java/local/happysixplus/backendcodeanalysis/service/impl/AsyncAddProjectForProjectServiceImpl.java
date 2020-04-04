package local.happysixplus.backendcodeanalysis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import local.happysixplus.backendcodeanalysis.util.callgraph.CallGraphMethods;
import local.happysixplus.backendcodeanalysis.data.ConnectiveDomainColorDynamicData;
import local.happysixplus.backendcodeanalysis.data.EdgeData;
import local.happysixplus.backendcodeanalysis.data.ProjectData;
import local.happysixplus.backendcodeanalysis.data.ProjectDynamicData;
import local.happysixplus.backendcodeanalysis.data.ProjectStaticAttributeData;
import local.happysixplus.backendcodeanalysis.data.SubgraphData;
import local.happysixplus.backendcodeanalysis.data.SubgraphDynamicData;
import local.happysixplus.backendcodeanalysis.data.VertexData;
import local.happysixplus.backendcodeanalysis.data.VertexPositionDynamicData;
import local.happysixplus.backendcodeanalysis.exception.MyRuntimeException;
import local.happysixplus.backendcodeanalysis.po.ConnectiveDomainColorDynamicPo;
import local.happysixplus.backendcodeanalysis.po.ConnectiveDomainDynamicPo;
import local.happysixplus.backendcodeanalysis.po.ConnectiveDomainPo;
import local.happysixplus.backendcodeanalysis.po.EdgeDynamicPo;
import local.happysixplus.backendcodeanalysis.po.EdgePo;
import local.happysixplus.backendcodeanalysis.po.ProjectDynamicPo;
import local.happysixplus.backendcodeanalysis.po.ProjectPo;
import local.happysixplus.backendcodeanalysis.po.ProjectStaticAttributePo;
import local.happysixplus.backendcodeanalysis.po.SubgraphDynamicPo;
import local.happysixplus.backendcodeanalysis.po.SubgraphPo;
import local.happysixplus.backendcodeanalysis.po.VertexDynamicPo;
import local.happysixplus.backendcodeanalysis.po.VertexPo;
import local.happysixplus.backendcodeanalysis.po.VertexPositionDynamicPo;
import local.happysixplus.backendcodeanalysis.vo.ConnectiveDomainAllVo;
import local.happysixplus.backendcodeanalysis.vo.ConnectiveDomainDynamicVo;
import local.happysixplus.backendcodeanalysis.vo.EdgeAllVo;
import local.happysixplus.backendcodeanalysis.vo.EdgeDynamicVo;
import local.happysixplus.backendcodeanalysis.vo.PackageNodeVo;
import local.happysixplus.backendcodeanalysis.vo.ProjectAllVo;
import local.happysixplus.backendcodeanalysis.vo.ProjectDynamicVo;
import local.happysixplus.backendcodeanalysis.vo.SubgraphAllVo;
import local.happysixplus.backendcodeanalysis.vo.SubgraphDynamicVo;
import local.happysixplus.backendcodeanalysis.vo.VertexAllVo;
import local.happysixplus.backendcodeanalysis.vo.VertexDynamicVo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;

@Service
public class AsyncAddProjectForProjectServiceImpl {

    static public class Vertex {
        Long id;
        String functionName = "";
        String sourceCode = "";
        int inDegree = 0;
        int outDegree = 0;
        List<Edge> edges = new ArrayList<>();
        List<Edge> allEdges = new ArrayList<>();

        Vertex(VertexPo po) {
            id = po.getId();
            functionName = po.getFunctionName();
            sourceCode = po.getSourceCode();
        }

        VertexPo getVertexPo() {
            return new VertexPo(id, functionName, sourceCode);
        }

        VertexAllVo getAllVo(VertexDynamicVo dVo) {
            return new VertexAllVo(id, functionName, sourceCode, dVo);
        }
    }

    static public class Edge {
        Long id;
        Double closeness = 0D;
        Vertex from;
        Vertex to;

        Edge(EdgePo po, Vertex from, Vertex to) {
            id = po.getId();
            closeness = po.getCloseness();
            this.from = from;
            this.to = to;
        }

        EdgePo getEdgePo(Map<String, VertexPo> vMap) {
            return new EdgePo(id, vMap.get(from.functionName), vMap.get(to.functionName), closeness);
        }

        EdgeAllVo getAllVo(EdgeDynamicVo dVo) {
            return new EdgeAllVo(id, from.id, to.id, closeness, dVo);
        }

    }

    static public class ConnectiveDomain {
        Long id;
        List<Long> vertexIds = new ArrayList<Long>();
        List<Long> edgeIds = new ArrayList<Long>();

        ConnectiveDomain(List<Long> v, List<Long> e) {
            vertexIds = v;
            edgeIds = e;
        }

        ConnectiveDomain(ConnectiveDomainPo po) {
            id = po.getId();
            // anotation = po.getAnotation();
            // color = po.getColor();
            vertexIds = new ArrayList<>(po.getVertexIds());
            edgeIds = new ArrayList<>(po.getEdgeIds());
        }

        ConnectiveDomainPo getConnectiveDomainPo() {
            return new ConnectiveDomainPo(id, new ArrayList<>(vertexIds), new ArrayList<>(edgeIds));
        }

        ConnectiveDomainAllVo getAllVo(ConnectiveDomainDynamicVo dVo) {
            return new ConnectiveDomainAllVo(id, new ArrayList<>(vertexIds), new ArrayList<>(edgeIds), dVo);
        }

    }

    static public class Subgraph {
        Long id;
        Double threshold;
        List<ConnectiveDomain> connectiveDomains = new ArrayList<ConnectiveDomain>();

        Subgraph(Double t, List<ConnectiveDomain> c) {
            threshold = t;
            connectiveDomains = c;
        }

        Subgraph(SubgraphPo po) {
            id = po.getId();
            threshold = po.getThreshold();
            // name = po.getName();
            for (var cPo : po.getConnectiveDomains()) {
                connectiveDomains.add(new ConnectiveDomain(cPo));
            }
        }

        SubgraphPo getSubgraphPo(Long projectId) {
            var cPo = new HashSet<ConnectiveDomainPo>(connectiveDomains.size());
            for (var c : connectiveDomains)
                cPo.add(c.getConnectiveDomainPo());
            return new SubgraphPo(id, projectId, threshold, cPo);
        }

        SubgraphAllVo getAllVo(SubgraphDynamicVo dVo, Map<Long, ConnectiveDomainDynamicPo> cDPoMap,
                Map<Long, ConnectiveDomainColorDynamicPo> cColorDPoMap) {
            var cdVos = new ArrayList<ConnectiveDomainAllVo>(connectiveDomains.size());
            for (var c : connectiveDomains)
                cdVos.add(c.getAllVo(dPoTodVo(cDPoMap.get(c.id), cColorDPoMap.get(c.id))));
            return new SubgraphAllVo(id, threshold, cdVos, dVo);
        }

    }

    static public class Project {
        Long id;
        Long userId;
        Map<Long, Vertex> vIdMap = new HashMap<Long, Vertex>();
        Map<Long, Edge> eIdMap = new HashMap<Long, Edge>();
        String packageStructureJSON;

        Project(ProjectPo po) {
            id = po.getId();
            userId = po.getUserId();
            for (var vPo : po.getVertices())
                vIdMap.put(vPo.getId(), new Vertex(vPo));
            for (var ePo : po.getEdges())
                eIdMap.put(ePo.getId(),
                        new Edge(ePo, vIdMap.get(ePo.getFrom().getId()), vIdMap.get(ePo.getTo().getId())));
            packageStructureJSON = po.getPackageStructure();
        }

        ProjectAllVo getAllVo(Map<Long, VertexDynamicPo> vDPoMap, Map<Long, VertexPositionDynamicPo> cPDPoMap,
                Map<Long, EdgeDynamicPo> eDPoMap, List<SubgraphAllVo> sVos, ProjectDynamicVo dVo) {
            List<VertexAllVo> vVos = new ArrayList<>();
            for (var v : vIdMap.values())
                vVos.add(v.getAllVo(dPoTodVo(vDPoMap.get(v.id), cPDPoMap.get(v.id))));
            var rootVo = JSONObject.parseObject(packageStructureJSON, PackageNode.class).getVo();
            List<EdgeAllVo> eVos = new ArrayList<>();
            for (var e : eIdMap.values())
                eVos.add(e.getAllVo(dPoTodVo(eDPoMap.get(e.id))));
            return new ProjectAllVo(id, vVos, rootVo, eVos, sVos, dVo);
        }

        class DfsE {
            long id;
            DfsV to;
            double closeness;

            DfsE(long id, DfsV to, double closeness) {
                this.id = id;
                this.to = to;
                this.closeness = closeness;
            }
        }

        class DfsV {
            long id;
            boolean vst;
            List<DfsE> es = new ArrayList<>();

            DfsV(long id) {
                this.id = id;
                this.vst = false;
            }
        }

        SubgraphPo initSubgraph(Double threshold) {
            var resConnectiveDomains = new ArrayList<ConnectiveDomain>();
            // 点
            var vs = new HashMap<Long, DfsV>(vIdMap.size());
            for (var v : vIdMap.values())
                vs.put(v.id, new DfsV(v.id));
            // 添加双向边
            for (var e : eIdMap.values()) {
                vs.get(e.from.id).es.add(new DfsE(e.id, vs.get(e.to.id), e.closeness));
                vs.get(e.to.id).es.add(new DfsE(e.id, vs.get(e.from.id), e.closeness));
            }
            for (var p : vs.values()) {
                List<Long> domainVertexs = new ArrayList<>();
                Set<Long> domainEdges = new HashSet<>();
                Dfs(threshold, p, domainVertexs, domainEdges);
                if (domainVertexs.size() > 1)
                    resConnectiveDomains.add(new ConnectiveDomain(domainVertexs, new ArrayList<>(domainEdges)));
            }
            resConnectiveDomains.sort((a, b) -> {
                return b.vertexIds.size() - a.vertexIds.size();
            });
            return new Subgraph(threshold, resConnectiveDomains).getSubgraphPo(id);
        }

        void Dfs(double threshold, DfsV p, List<Long> domainVertexs, Set<Long> domainEdges) {
            if (p.vst)
                return;
            p.vst = true;
            domainVertexs.add(p.id);
            for (var e : p.es) {
                if (e.closeness < threshold)
                    continue;
                domainEdges.add(e.id);
                DfsV to = e.to;
                Dfs(threshold, to, domainVertexs, domainEdges);
            }
        }
    }

    @Data
    @NoArgsConstructor
    static class PackageNode {
        String str;
        Map<String, PackageNode> chrs = new HashMap<>();
        List<Long> funcs = new ArrayList<>();

        PackageNode(String str) {
            this.str = str;
        }

        void insertFunc(long id, String partName) {
            if (partName == null) {
                funcs.add(id);
                return;
            }
            var partNameSplit = partName.split("\\.", 2);
            var nextStr = partNameSplit[0];
            var nextPartName = partNameSplit.length == 1 ? null : partNameSplit[1];
            var chr = chrs.get(nextStr);
            if (chr == null) {
                chr = new PackageNode(nextStr);
                chrs.put(nextStr, chr);
            }
            chr.insertFunc(id, nextPartName);
        }

        PackageNodeVo getVo() {
            var chrVos = new ArrayList<PackageNodeVo>(chrs.size());
            for (var chr : chrs.values())
                chrVos.add(chr.getVo());
            return new PackageNodeVo(str, chrVos, funcs);
        }
    }

    @Autowired
    CallGraphMethods callGraphMethods;

    @Autowired
    ProjectData projectData;

    @Autowired
    SubgraphData subgraphData;

    @Autowired
    EdgeData edgeData;

    @Autowired
    VertexData vertexData;

    @Autowired
    ProjectStaticAttributeData projectStaticAttributeData;

    @Autowired
    ProjectDynamicData projectDynamicData;

    @Autowired
    SubgraphDynamicData subgraphDynamicData;

    @Autowired
    ConnectiveDomainColorDynamicData connectiveDomainColorDynamicData;

    @Autowired
    VertexPositionDynamicData vertexPositionDynamicData;

    @Async("AddProjectExecutor")
    public CompletableFuture<String> asyncAddProject(Long projectId, String projectName, String url, long userId) {
        try {
            var projectInfo = callGraphMethods.initGraph(url);
            if (projectInfo == null)
                throw new MyRuntimeException("您的项目有问题");
            var failedDPo = new ProjectDynamicPo(projectId, userId, projectName + "（解析失败）");
            projectDynamicData.save(failedDPo);
            failedDPo = null;
            String[] callGraph = projectInfo.getCallGraph();
            var sourceCode = projectInfo.getSourceCode();
            List<String> caller = new ArrayList<>();
            List<String> callee = new ArrayList<>();
            Set<List<String>> edgeSet = new HashSet<>();
            for (var str : callGraph) {
                List<String> tempList = new ArrayList<>();
                String[] temp = str.split(" ");
                tempList.add(temp[0].substring(2));
                tempList.add(temp[1].substring(3));
                edgeSet.add(tempList);
            }
            callGraph = null;
            projectInfo = null;
            for (var edge : edgeSet) {
                caller.add(edge.get(0));
                callee.add(edge.get(1));
            }
            // 生成并存入项目静态信息
            var project = initAndSaveProject(projectId, caller, callee, sourceCode, userId);
            sourceCode = null;
            // 生成并存入默认子图静态信息
            var subPo = project.initSubgraph(0D);
            subPo = subgraphData.save(subPo);
            // 存入项目静态属性信息
            var projSAPo = new ProjectStaticAttributePo(project.id, userId, project.vIdMap.size(),
                    project.vIdMap.size(), subPo.getConnectiveDomains().size());
            projSAPo = projectStaticAttributeData.save(projSAPo);
            // 存入项目动态信息
            var projDPo = new ProjectDynamicPo(project.id, userId, projectName);
            projDPo = projectDynamicData.save(projDPo);
            // 存入子图动态信息
            var subgDPo = new SubgraphDynamicPo(subPo.getId(), project.id, "Default subgraph");
            subgDPo = subgraphDynamicData.save(subgDPo);
            // 生成联通域初始颜色并存储
            var cDPoMap = new HashMap<Long, ConnectiveDomainColorDynamicPo>(subPo.getConnectiveDomains().size());
            String[] colors = { "#CDCDB4", "#CDB5CD", "#CDBE70", "#B4CDCD", "#CD919E", "#9ACD32", "#CD4F39", "#8B3E2F",
                    "#8B7E66", "#8B668B", "#36648B", "#141414" };
            for (var cd : subPo.getConnectiveDomains()) {
                var color = colors[((int) (Math.random() * colors.length))];
                var cDPo = connectiveDomainColorDynamicData
                        .save(new ConnectiveDomainColorDynamicPo(cd.getId(), project.id, color));
                cDPoMap.put(cd.getId(), cDPo);
            }
            // 生成节点初始位置
            var cdList = new ArrayList<>(subPo.getConnectiveDomains());
            cdList.sort((a, b) -> {
                return b.getVertexIds().size() - a.getVertexIds().size();
            });
            var vPosPoMap = new HashMap<Long, VertexPositionDynamicPo>(cdList.size());
            class Util {
                HashMap<Long, VertexPositionDynamicPo> map;
                Long projectId;
                // TODO: 可以根据前端显示效果修改该值，也可以让前端把(0, 0)作为中心
                double centerX = 800;
                double centerY = 800;

                Util(HashMap<Long, VertexPositionDynamicPo> map, Long projectId) {
                    this.map = map;
                    this.projectId = projectId;
                }

                double calcRadius(int size) {
                    // TODO: 应当根据前端显示效果修改半径系数
                    return (30 * Math.sqrt((double) size));
                }

                void calcPosForCD(Coordinate center, double radius, List<Long> vIds) {
                    AffineTransformation.translationInstance(centerX, centerY).transform(center, center);
                    var fact = new GeometricShapeFactory();
                    fact.setCentre(center);
                    fact.setSize(radius * 2);
                    fact.setNumPoints((int) (radius / 10));
                    var g = fact.createCircle();
                    var pb = new RandomPointsBuilder();
                    pb.setExtent(g);
                    pb.setNumPoints(vIds.size());
                    var randRes = pb.getGeometry().getCoordinates();
                    for (int i = 0; i < vIds.size(); i++) {
                        map.put(vIds.get(i), new VertexPositionDynamicPo(vIds.get(i), projectId, (float) randRes[i].x,
                                (float) randRes[i].y));
                    }
                }
            }
            Util util = new Util(vPosPoMap, project.id);
            if (cdList.size() > 0) {
                var it = cdList.iterator();
                var cd = it.next();
                var radius = util.calcRadius(cd.getVertexIds().size());
                // 中心联通域
                util.calcPosForCD(new Coordinate(), radius, cd.getVertexIds());
                while (it.hasNext()) {
                    // 确定半径
                    double centerR;
                    double r;
                    double theta;
                    Coordinate p;
                    // 第一个
                    cd = it.next();
                    r = util.calcRadius(cd.getVertexIds().size());
                    centerR = radius + r;
                    theta = Math.asin(r / centerR) * 2;
                    radius += r * 2;
                    p = new Coordinate(0, centerR);
                    util.calcPosForCD(p, r, cd.getVertexIds());
                    // 其余的几个
                    int num = (int) (6.28 / theta);
                    for (int i = 1; i < num; i++) {
                        if (it.hasNext())
                            cd = it.next();
                        else
                            break;
                        AffineTransformation.rotationInstance(theta).transform(p, p);
                        util.calcPosForCD(p, r, cd.getVertexIds());
                    }
                }
            }
            // 存储节点初始位置
            for (var vp : vPosPoMap.values())
                vertexPositionDynamicData.save(vp);
        } catch (Exception e) {
            var failedDPo = new ProjectDynamicPo(projectId, userId, projectName + "（解析失败）");
            projectDynamicData.save(failedDPo);
            throw e;
        }
        return CompletableFuture.completedFuture("Finished");
    }

    Project initAndSaveProject(Long projectId, List<String> caller, List<String> callee, Map<String, String> sourceCode,
            Long userId) {
        Set<EdgePo> edgePos = new HashSet<EdgePo>();
        Set<VertexPo> vertexPos = new HashSet<VertexPo>();
        Map<String, VertexPo> vertexMap = new HashMap<String, VertexPo>();
        Map<String, Integer> outdegree = new HashMap<String, Integer>();
        Map<String, Integer> indegree = new HashMap<String, Integer>();

        var vertexNameSet = new HashSet<String>();
        vertexNameSet.addAll(caller);
        vertexNameSet.addAll(callee);
        var vertexNames = new ArrayList<String>(vertexNameSet);

        for (var str : vertexNames) {
            VertexPo vPo;
            if (sourceCode.containsKey(str))
                vPo = new VertexPo(null, str, sourceCode.get(str));
            else
                vPo = new VertexPo(null, str, "");
            vertexPos.add(vPo);
            vertexMap.put(str, vPo);
            outdegree.put(str, 0);
            indegree.put(str, 0);
        }

        for (int i = 0; i < caller.size(); i++) {
            String startName = caller.get(i);
            String endName = callee.get(i);
            outdegree.put(startName, outdegree.get(startName) + 1);
            indegree.put(endName, indegree.get(endName) + 1);
        }

        for (int i = 0; i < caller.size(); i++) {
            String startName = caller.get(i);
            String endName = callee.get(i);
            VertexPo from = vertexMap.get(startName);
            VertexPo to = vertexMap.get(endName);
            Double closeness = 2.0 / (outdegree.get(startName) + indegree.get(endName));
            edgePos.add(new EdgePo(null, from, to, closeness));
        }

        var projPo = projectData.save(new ProjectPo(projectId, userId, vertexPos, edgePos, ""));
        var vMap = projPo.getVertices().stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        PackageNode root = new PackageNode("src");
        for (var v : vMap.values())
            root.insertFunc(v.getId(), v.getFunctionName().split(":", 2)[0]);
        projPo.setPackageStructure(JSONObject.toJSONString(root));
        projPo = projectData.save(projPo);
        return new Project(projPo);
    }

    private static VertexDynamicVo dPoTodVo(VertexDynamicPo po, VertexPositionDynamicPo posPo) {
        // 初始一定有Pos
        var res = new VertexDynamicVo(posPo.getId(), "", posPo.getX(), posPo.getY());
        if (po != null)
            res.setAnotation(po.getAnotation());
        return res;
    }

    private static EdgeDynamicVo dPoTodVo(EdgeDynamicPo po) {
        if (po == null)
            return null;
        return new EdgeDynamicVo(po.getId(), po.getAnotation());
    }

    private static ConnectiveDomainDynamicVo dPoTodVo(ConnectiveDomainDynamicPo po,
            ConnectiveDomainColorDynamicPo cPo) {
        // 初始一定有Color
        var res = new ConnectiveDomainDynamicVo(cPo.getId(), "", cPo.getColor());
        if (po != null)
            res.setAnotation(po.getAnotation());
        return res;
    }

}