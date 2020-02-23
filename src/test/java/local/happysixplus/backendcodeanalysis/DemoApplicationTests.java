package local.happysixplus.backendcodeanalysis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import local.happysixplus.backendcodeanalysis.service.GraphService;

@SpringBootTest
class DemoApplicationTests {
	@Autowired
	GraphService graphService;

	@Test
	void contextLoads() {
		graphService.LoadCode("/Users/macbook/Desktop/call_dependencies.txt");
	}

}
