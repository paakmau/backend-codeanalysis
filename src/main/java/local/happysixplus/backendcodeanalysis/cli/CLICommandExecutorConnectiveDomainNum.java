package local.happysixplus.backendcodeanalysis.cli;

import java.util.Scanner;

import local.happysixplus.backendcodeanalysis.service.GraphService;
import lombok.var;

public class CLICommandExecutorConnectiveDomainNum implements CLICommandExecutor {

    @Override
    public void execute(String[] params, Scanner scanner, GraphService graphService) {
        var domainNum = graphService.getConnectiveDomains().size();
        System.out.println("Domain num (excluding domains with only one vertex): " + domainNum);
    }
}