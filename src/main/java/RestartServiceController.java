import java.io.File;

public class RestartServiceController {

    public void restartService() {

        String service = checkIfIsNddOrNeogridAndGetNameService();

        try {
            String[] command = {"cmd.exe", "/c", "net", "stop", service, "&&", "net", "start", service};
            new ProcessBuilder(command).start();
            System.out.println("Checagem do serviço realizado com sucesso.");
        } catch (Exception ex) {
            System.out.println("Falha ao Finalizar / Iniciar execução do serviço.");
        }
    }
    private String checkIfIsNddOrNeogridAndGetNameService() {

        File rootPath = new File(System.getProperty("C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\bin\\NDDigital.eForms.Agent.Service.exe"));

        if (rootPath.getAbsolutePath().toLowerCase().contains("ndd")) {
            return "NDDigital" + rootPath.getName().replace(" ", "");
        }
        return "NFCe" + rootPath.getName().replace(" ", "");
    }
}
