import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class RestartServiceController {

    private String service;
    private static final Logger LOGGER = LogManager.getLogger(AgentAutomaticProcess.class.getName());

    public void restartService() {

        LoggerContext.getContext().reconfigure();

        service = checkIfIsNddOrNeogridAndGetNameService();

        try {
            String[] command = {"cmd.exe", "/c", "net", "stop", service, "&&", "net", "start", service};
            new ProcessBuilder(command).start();
            LOGGER.info("Checagem do serviço realizado com sucesso.");
        } catch (Exception ex) {
            LOGGER.error("Falha ao Finalizar / Iniciar execução do serviço.");
        }
    }

    private String checkIfIsNddOrNeogridAndGetNameService() {

        File rootPath = new File("C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\bin\\NDDigital.eForms.Agent.Service.exe");

        if (rootPath.getAbsolutePath().toLowerCase().contains("ndd")) {
            //return "NDDigital" + rootPath.getName().replace(" ", "");
            return "NDDigitalAgentService";
        }
        return "NFCe" + rootPath.getName().replace(" ", "");
    }

    public void startService(){

        service = checkIfIsNddOrNeogridAndGetNameService();

        try {
            String[] command = {"cmd.exe", "/c", "net", "start", service};
            new ProcessBuilder(command).start();
        } catch (Exception ex) {
            LOGGER.error("\nFalha ao Iniciar execução do serviço.");
        }
    }

    public void stopService(){

        service = checkIfIsNddOrNeogridAndGetNameService();

        try {
            String[] command = {"cmd.exe", "/c", "net", "stop", service};
            new ProcessBuilder(command).start();
            LOGGER.info("Parando serviço do módulo.");
            killProcessJavaw();
            LOGGER.info("Finalizando processos de Javaw.exe");
        } catch (Exception ex) {
            LOGGER.error("Falha ao Finalizar / Iniciar execução do serviço.");
        }
    }

    public boolean checkStatusService(){

        service = checkIfIsNddOrNeogridAndGetNameService();

        try {

            Process process;
            process = Runtime.getRuntime().exec("sc query " + service);
            Scanner reader = new Scanner(process.getInputStream(), "UTF-8");
            while (reader.hasNextLine()){
                if (reader.nextLine().contains("RUNNING")){
                    return true;
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return false;
    }

    public void killProcessJavaw(){

        try{
            String[] command = {"cmd.exe", "/c", "taskkill /f /im javaw.exe"};
            new ProcessBuilder(command).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
