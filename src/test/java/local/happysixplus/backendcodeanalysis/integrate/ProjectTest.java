package local.happysixplus.backendcodeanalysis.integrate;

import com.alibaba.fastjson.JSONObject;
import local.happysixplus.backendcodeanalysis.data.ProjectData;
import local.happysixplus.backendcodeanalysis.data.SubgraphData;
import local.happysixplus.backendcodeanalysis.vo.*;
import lombok.var;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ProjectData projectData;

	@Autowired
	SubgraphData subgraphData;

	long getHashCodeForProjectStaticVo(ProjectStaticVo vo) {
		long res = vo.getId().hashCode();
		res += new HashSet<>(vo.getEdges()).hashCode();
		res += new HashSet<>(vo.getVertices()).hashCode();
		res += vo.getSubgraphs().stream().map((SubgraphStaticVo subgVo) -> {
			return (int) subgVo.getId().hashCode() + (long) subgVo.getThreshold().hashCode()
					+ (long) new HashSet<>(subgVo.getConnectiveDomains()).hashCode();
		}).collect(Collectors.toSet()).hashCode();
		return res;
	}

	long getHashCodeForProjectDynamicVo(ProjectDynamicVo vo) {
		long res = vo.getId().hashCode();
		res += new HashSet<>(vo.getEdges()).hashCode();
		res += new HashSet<>(vo.getVertices()).hashCode();
		res += vo.getSubgraphs().stream().map((SubgraphDynamicVo subgVo) -> {
			return subgVo.getId() + (long) subgVo.getName().hashCode()
					+ (long) new HashSet<>(subgVo.getConnectiveDomains()).hashCode();
		}).collect(Collectors.toSet()).hashCode();
		return res;
	}

	long getHashCodeForProjectAllVo(ProjectAllVo vo) {
		return getHashCodeForProjectStaticVo(vo.getStaticVo()) + getHashCodeForProjectDynamicVo(vo.getDynamicVo());
	}

	@Test
	public void Test1() throws Exception {
		MvcResult resAdd = mockMvc
				.perform(MockMvcRequestBuilders.post("/project").param("projectName", "Linux")
						.param("url", "https://gitee.com/forsakenspirit/Linux").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

		mockMvc.perform(MockMvcRequestBuilders.get("/project").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var resa = JSONObject.parseObject(resAdd.getResponse().getContentAsString(), ProjectAllVo.class);
		assertEquals(resa.getDynamicVo().getVertices().size(), 9);
		assertEquals(resa.getDynamicVo().getEdges().size(), 10);
		assertEquals(resa.getDynamicVo().getProjectName(), "Linux");
	}

	@Test
	public void Test2() throws Exception {
		MvcResult resGet = mockMvc.perform(MockMvcRequestBuilders.get("/project").param("userId", "1"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		System.out.println(resGet);
	}

	@Test
	public void Test3() throws Exception {
		MvcResult resGet1 = mockMvc.perform(MockMvcRequestBuilders.get("/project").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var v = resGet1.getResponse().getContentAsString();
		var vo = (ProjectAllVo) JSONObject.parseObject(JSONObject.parseObject(v, List.class).get(0).toString(),
				ProjectAllVo.class);
		// var
		// vo=(ProjectAllVo)(JSONObject.parseObject(resGet1.getResponse().getContentAsString(),
		// List.class).get(0));
		long projectId = vo.getId();
		vo.getDynamicVo().setProjectName("SKTelecomT1Faker");
		MvcResult resUpdate = mockMvc
				.perform(MockMvcRequestBuilders.put("/project/{projectId}", projectId)
						.contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSONString(vo.getDynamicVo())))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		mockMvc.perform(MockMvcRequestBuilders.get("/project").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		// System.out.println(resGet1);
		System.out.print(resUpdate);
		// System.out.println(resGet2);
	}

	@Test
	public void Test4() throws Exception {
		MvcResult resAdd = mockMvc
				.perform(MockMvcRequestBuilders.post("/project").param("projectName", "RUa")
						.param("url", "https://gitee.com/forsakenspirit/Linux").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var v = resAdd.getResponse().getContentAsString();
		var vo = (ProjectAllVo) JSONObject.parseObject(v, ProjectAllVo.class);
		long projectId = vo.getId();
		MvcResult resRemove = mockMvc.perform(MockMvcRequestBuilders.delete("/project/{id}", projectId))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		System.out.print(resRemove);
	}

	@Test
	public void Test5() throws Exception {
		MvcResult resAdd = mockMvc
				.perform(MockMvcRequestBuilders.post("/project").param("projectName", "Test5")
						.param("url", "https://gitee.com/forsakenspirit/Linux").param("userId", "5"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var v = resAdd.getResponse().getContentAsString();
		var vo = (ProjectAllVo) JSONObject.parseObject(v, ProjectAllVo.class);
		long projectId = vo.getId();
		MvcResult resAddSubgraph = mockMvc
				.perform(MockMvcRequestBuilders.post("/project/{projectId}/subgraph", projectId).param("threshold",
						"0.5"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var subgraphVo = JSONObject.parseObject(resAddSubgraph.getResponse().getContentAsString(), SubgraphAllVo.class);

		MvcResult resGetSubgraph = mockMvc
				.perform(MockMvcRequestBuilders.get("/project/{projectId}/subgraph", projectId))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var subgraphAllVos = JSONObject.parseArray(resGetSubgraph.getResponse().getContentAsString(),
				SubgraphAllVo.class);
		System.out.println(subgraphAllVos.size());
		vo.getDynamicVo().getSubgraphs().add(subgraphVo.getDynamicVo());
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/project/{projectId}/subgraph/{id}", projectId, subgraphVo.getId()))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
	}

	@Test
	public void Test6() throws Exception {
		MvcResult resAdd = mockMvc
				.perform(MockMvcRequestBuilders.post("/project").param("projectName", "TestSix")
						.param("url", "https://gitee.com/forsakenspirit/Linux").param("userId", "2"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var v = resAdd.getResponse().getContentAsString();
		var vo = (ProjectAllVo) JSONObject.parseObject(v, ProjectAllVo.class);
		long projectId = vo.getId();
		MvcResult resGetFunction = mockMvc
				.perform(MockMvcRequestBuilders.get("/project/{projectId}/similarFunction", projectId).param("funcName",
						"C"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		List<String> strs = JSONObject.parseArray(resGetFunction.getResponse().getContentAsString(), String.class);
		for (var str : strs)
			System.out.println(str);
	}

	@Test
	public void Test7() throws Exception {
		MvcResult resAdd = mockMvc
				.perform(MockMvcRequestBuilders.post("/project").param("projectName", "TestSeven")
						.param("url", "https://gitee.com/forsakenspirit/Linux").param("userId", "3"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var v = resAdd.getResponse().getContentAsString();
		var vo = (ProjectAllVo) JSONObject.parseObject(v, ProjectAllVo.class);
		long projectId = vo.getId();
		List<VertexDynamicVo> vdvs = vo.getDynamicVo().getVertices();
		long id1 = vdvs.get(0).getId();
		long id2 = vdvs.get(vdvs.size() - 1).getId();
		MvcResult resGetFunction = mockMvc
				.perform(MockMvcRequestBuilders.get("/project/{projectId}/originalGraphShortestPath", projectId)
						.param("startVertexId", Long.toString(id1)).param("endVertexId", Long.toString(id2)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
		var res = resGetFunction.getResponse().toString();
		System.out.println(res);

	}

	@Test
	public void Test8() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/project").param("projectName", "TestEightDemo")
				.param("url", "https://gitee.com/forsakenspirit/Demo").param("userId", "4396"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

	}
}
