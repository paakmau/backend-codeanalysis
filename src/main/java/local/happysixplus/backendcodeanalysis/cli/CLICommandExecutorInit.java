package local.happysixplus.backendcodeanalysis.cli;

import local.happysixplus.backendcodeanalysis.service.GraphService;

public class CLICommandExecutorInit implements CLICommandExecutor {

    @Override
    public void Execute(String[] params, GraphService graphService) {
        graphService.LoadCode(params[0]);
    }
}