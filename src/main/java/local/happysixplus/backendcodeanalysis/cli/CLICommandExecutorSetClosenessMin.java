package local.happysixplus.backendcodeanalysis.cli;

import local.happysixplus.backendcodeanalysis.service.GraphService;

public class CLICommandExecutorSetClosenessMin implements CLICommandExecutor {

    @Override
    public void Execute(String[] params, GraphService graphService) {
        graphService.SetClosenessMin(Double.valueOf(params[0]));
    }
}