import Utils.Modules;
import Utils.SetNum;
import model.ResultTests;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class AgentAutomaticProcess {

    //Variáveis Globais
    private static String concentratorurl;
    private static String enterprise;
    private static String diretorioAgente = "";
    private static String diretorioSaidaAgente = "";
    private static String nomeAgente;
    private static String routerurl;
    private static String wsurl;
    private static String input = "";
    private static int set_num;
    private static int serie;
    private static int number;
    private static int modulo;
    private static int timeSleep;

    private static ResultTests resultTests = new ResultTests();

    private static final Logger LOGGER = LogManager.getLogger(AgentAutomaticProcess.class.getName());

    public static void main(String[] args) throws InterruptedException, IOException {

        PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
        pathConfig.loadConfigLog4j();

        File log4j2 = new File(pathConfig.getLog4j2());

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(log4j2.toURI());

        LoggerContext.getContext().reconfigure();

        Scanner scanner = new Scanner(System.in);
        RestartServiceController restartServiceController = new RestartServiceController();

        System.out.println("Iniciando processo de ped_install com sucesso"); // Informar inicio processo no prompt
        LOGGER.info("###################   Remocao Control e Logs   ###################\n");

        boolean ModuleStatusService = restartServiceController.checkStatusService();

        if (ModuleStatusService) {

            try {
                restartServiceController.stopService();

                Thread.sleep(20000);

                LOGGER.info("\nTentando deletar diretorio Control e Logs...");
                boolean statusDelete = tryDeleteFolderControlLogs();

                if (!statusDelete) {

                    Thread.sleep(10000);
                    tryDeleteFolderControlLogs();
                    Thread.sleep(10000);
                    LOGGER.info("\nFinalizado processo de exclusao de diretorios.");
                }

            } catch (Exception exception) {

                LOGGER.error(exception.getMessage());
            }

        } else {
            try {
                boolean statusDelete = tryDeleteFolderControlLogs();
                LOGGER.info("\nFinalizado processo de exclusao de diretorios.");

            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        LOGGER.info("\n###################   Checagem servico   ###################\n");

        boolean statusService = restartServiceController.checkStatusService();

        if (statusService == true) {
            LOGGER.info("\nServico ja esta iniciado.");
        } else {
            restartServiceController.startService();
            Thread.sleep(2000);

            boolean checkServiceAgain = restartServiceController.checkStatusService();

            while (checkServiceAgain == false) {

                LOGGER.error("Erro ao iniciar o servico. Checar se o mesmo esta desativado.");

                Thread.sleep(5000);

                checkServiceAgain = restartServiceController.checkStatusService();
                restartServiceController.startService();
            }

            LOGGER.info("\nServico iniciado com sucesso.");

        }

        LOGGER.info("\n\n###################   Bem-vindo a ferramenta de ped_install   ###################");

        LOGGER.info(" 1 - Agente/Concentrador");
        LOGGER.info(" 2 - Web Service Router");
        LOGGER.info(" 3 - Web Service NFC-e");

        do {
            modulo = pathConfig.getModulo();
            LOGGER.info("O módulo escolhido e o " + modulo);
        }
        while (!Modules.checkModule(modulo));

        diretorioAgente = pathConfig.getDiretorioAgente();
        LOGGER.info("O diretório de processamento do Agente e " + diretorioAgente);

        LOGGER.info("\n##################");
        checkFilesInDirectory();
        LOGGER.info("\n##################");

        diretorioSaidaAgente = pathConfig.getDiretorioSaidaAgente();
        LOGGER.info("O diretório de retorno do Agente e " + diretorioSaidaAgente);

        LOGGER.info("\n##################");
        checkFilesOutDirectory();
        LOGGER.info("\n##################");

        resultTests.setQuantidadeArquivos(pathConfig.getQuantidadeArquivos());
        LOGGER.info("A quantidade de arquivos a serem gerados e processados e " + resultTests.getQuantidadeArquivos());

        nomeAgente = pathConfig.getNomeAgente();
        nomeAgente = nomeAgente.toUpperCase();
        LOGGER.info("O nome do Agente e " + nomeAgente);

        timeSleep = pathConfig.getTimeSleep();
        LOGGER.info("O Time Sleep e de " + timeSleep + " segundos.");
        transformSecondsToMilliseconds();

        switch (Modules.getProcess(modulo)) {
            case AGENTE_CONCENTRATOR:

                concentratorurl = pathConfig.getConcentratorTargetUrl();
                concentratorurl = concentratorurl.toLowerCase();
                LOGGER.info("O concentratorURL e " + concentratorurl);

                enterprise = pathConfig.getEnterprise();
                LOGGER.info("O enterprise e " + enterprise);

                askSetNumUser();

                checkAnswerSetNumUser();

                break;

            case WEB_SERVICE_ROUTER:

                routerurl = pathConfig.getRouterUrl();
                LOGGER.info("O routerURL e " + routerurl);

                enterprise = pathConfig.getEnterprise();
                LOGGER.info("O enterprise e " + enterprise);

                askSetNumUser();

                checkAnswerSetNumUser();

                break;

            case WER_SERVICE_NFCE:
                wsurl = pathConfig.getWsurl();
                LOGGER.info("O wsURL e " + routerurl);

                enterprise = pathConfig.getEnterprise();
                LOGGER.info("O enterprise e " + enterprise);

                askSetNumUser();

                checkAnswerSetNumUser();

                break;
        }

        while (checkQuantityFilesProcessed() == false){
            LOGGER.info("\nAguardando retorno de todos os documentos processados.");
            Thread.sleep(8000);
        }

        filesToRead();
        EmailProcess.envioEmail(resultTests);
        LOGGER.info("Processo de ped_install automatico finalizado com sucesso");
        System.out.println("Processo de ped_install automatico finalizado com sucesso"); // Informar finalizacao processo prompt

    }

    private static void askSetNumUser() {
        do {
            PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
            set_num = pathConfig.getSetNum();
            if (set_num == 1){
                LOGGER.info("O processo com SET_NUM foi escolhido.");
            }else {
                LOGGER.info("O processo sem SET_NUM foi escolhido.");
            }
        } while (!SetNum.checkSetNum(set_num));
    }

    private static void transformSecondsToMilliseconds() {
        PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
        timeSleep = pathConfig.getTimeSleep() * 1000;
    }

    private static void checkAnswerSetNumUser() throws InterruptedException {
        PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
        if (set_num == SetNum.SET_NUM_EXISTENTE.getCode()) {
            serie = pathConfig.getSerie();
            LOGGER.info("A serie do SET_NUM e " + serie);

            number = pathConfig.getNumber();
            LOGGER.info("O numero do SET_NUM e " + number);
        }

        generateFile();
    }

    public static void generateFile() throws InterruptedException {
        if (set_num == SetNum.SET_NUM_EXISTENTE.getCode()) {
            LOGGER.info("########## Em processamento ##########\n");
            for (int i = 1; i <= resultTests.getQuantidadeArquivos(); i++) {
                writeFileWithSetNum(i);
                LOGGER.info("O arquivo com SET_NUM do " + nomeAgente + " " + i + " foi gerado com sucesso.");
                Thread.sleep(timeSleep);
            }

            LOGGER.info("\nFinalizado o processamento de " + resultTests.getQuantidadeArquivos() + " arquivo(s) com SET_NUM para o Agente: " + nomeAgente);
            LOGGER.info("\n########## Finalizado processamento ##########");

        }
        if (set_num == SetNum.SET_NUM_INEXISTENTE.getCode()) {
            LOGGER.info("########## Em processamento ##########\n");
            for (int i = 1; i <= resultTests.getQuantidadeArquivos(); i++) {
                writeFileWithOutSetNum(i);
                LOGGER.info("O arquivo  do " + nomeAgente + " " + i + " foi gerado com sucesso.");
                Thread.sleep(timeSleep);
            }
            LOGGER.info("\nFinalizado o processamento de " + resultTests.getQuantidadeArquivos() + " arquivo(s) para o Agente: " + nomeAgente);
            LOGGER.info("\n########## Finalizado processamento ##########");
        }
    }

    public static void writeFileWithOutSetNum(int i) {
        try {
            FileWriter file = new FileWriter(diretorioAgente + "\\" + nomeAgente + "#00" + Integer.toString(i) + "_ped_install.xml");

            file.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            file.write("<install version=\"4.00\">\n");

            if (modulo == Modules.AGENTE_CONCENTRATOR.getCode()) {
                file.write("<concentratorurl>" + concentratorurl + "</concentratorurl>\n");
            }
            if (modulo == Modules.WEB_SERVICE_ROUTER.getCode()) {
                file.write("<routerurl>" + routerurl + "</routerurl>\n");
            }
            if (modulo == Modules.WER_SERVICE_NFCE.getCode()) {
                file.write("<wsurl>" + wsurl + "</wsurl>\n");
            }

            file.write("<enterprise>" + enterprise + "</enterprise>\n");
            file.write("</install>");

            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeFileWithSetNum(int i) {
        try {
            FileWriter file = new FileWriter(diretorioAgente + "\\" + nomeAgente + "#00" + i + "_ped_install.xml");

            file.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            file.write("<install version=\"4.00\">\n");

            if (modulo == Modules.AGENTE_CONCENTRATOR.getCode()) {
                file.write("<concentratorurl>" + concentratorurl + "</concentratorurl>\n");
            }
            if (modulo == Modules.WEB_SERVICE_ROUTER.getCode()) {
                file.write("<routerurl>" + routerurl + "</routerurl>\n");
            }
            if (modulo == Modules.WER_SERVICE_NFCE.getCode()) {
                file.write("<wsurl>" + wsurl + "</wsurl>\n");
            }

            file.write("<enterprise>" + enterprise + "</enterprise>\n");
            file.write("<setnum>\n");
            file.write("<serie>" + serie + "</serie>\n");
            file.write("<number>" + number + "</number>\n");
            file.write("</setnum>\n");
            file.write("</install>");

            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void checkFilesOutDirectory() {
        File diretory = new File(diretorioSaidaAgente);

        if (diretory.isDirectory()) {
            String[] filesDirectory = diretory.list();

            if (filesDirectory.length > 0) {
                int filesInDirectory = filesDirectory.length;
                LOGGER.info("Diretorio nao esta vazio. Contem " + filesInDirectory + " arquivos.");
                LOGGER.info("Iniciando limpeza da mesma...");

                for (File cleanDirectory : diretory.listFiles()) {
                    cleanDirectory.delete();
                }

                LOGGER.info("Diretorio limpo com sucesso.");

            } else {
                LOGGER.error("Diretorio vazio.");
            }
        }
    }

    public static void checkFilesInDirectory() {
        File diretory = new File(diretorioAgente);

        if (diretory.isDirectory()) {
            String[] filesDirectory = diretory.list();

            if (filesDirectory.length > 0) {
                int filesInDirectory = filesDirectory.length;
                LOGGER.info("Diretorio nao esta vazio. Contem " + filesInDirectory + " arquivos.");

                for (File cleanDirectory : diretory.listFiles()) {
                    cleanDirectory.delete();
                }

                LOGGER.info("Diretorio limpo com sucesso.");

            } else {
                LOGGER.error("Diretorio vazio.");
            }
        }
    }

    public static boolean checkQuantityFilesProcessed(){

        File diretory = new File(diretorioSaidaAgente);
        String[] filesToCount = diretory.list();

        if (filesToCount.length == resultTests.getQuantidadeArquivos()){
            return true;
        }else{
            return false;
        }

    }

    public static boolean tryDeleteFolderControlLogs() throws IOException {

        String control = "C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\control";
        String logs = "C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\logs";

        File directoryControl = new File(control);
        File directoryLogs = new File(logs);

        if (directoryControl.isDirectory()) {
            FileUtils.deleteDirectory(directoryControl);
            LOGGER.info("Diretorio control excluido com sucesso.");

            if (directoryLogs.isDirectory()) {
                FileUtils.deleteDirectory(directoryLogs);
                LOGGER.info("Diretorio logs excluido com sucesso.");
                return true;
            }
        }
        return false;
    }

    public static void filesToRead() throws IOException {

        File diretory = new File(diretorioSaidaAgente);
        String[] filesToRead = diretory.list();

        LOGGER.info("\n\n########## Iniciando leitura do diretorio de SAIDA ##########\n");

        if (filesToRead.length > 0) {
            int quantityToRead = filesToRead.length;
            LOGGER.info("O diretorio contem " + quantityToRead + " arquivos para leitura.\n");

            File fileList[] = diretory.listFiles();
            LOGGER.info("Lista de arquivos no direterio de saida: ");

            String content = "";

            for (File file : fileList) {
                LOGGER.info("Nome do arquivo: " + file.getName());
                LOGGER.info("Diretorio do arquivo: " + file.getAbsolutePath());

                input = FileUtils.readFileToString(file);

                if (input.contains("<code>100</code>")) {
                    resultTests.setSucesso();
                }
                if (!input.contains("<code>100</code>")) {
                    resultTests.setFalha();
                }

                content += "\n" + input + "\n";
                content += "\nNome do arquivo: " + file.getName() + "\n";
                content += "\nDiretorio do arquivo: " + file.getAbsolutePath() + "\n\n";
                content += "-------------------------------------------------------";

            }

            content += "\nTotal de arquivo processados com SUCESSO: " + resultTests.getSucesso();
            content += "\nTotal de arquivo processados com FALHA: " + resultTests.getFalha() + "\n";
            content += "-------------------------------------------------------\n";

            String diretorioFinal = new File(System.getProperty("user.dir")).getParent() + "\\";

            Date data = new Date();
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy-HHmmss");
            String formattedDate = format.format(data);

            File dirRelatorio = new File(diretorioFinal + "\\" + "Relatorio");
            if(!dirRelatorio.exists()){
                dirRelatorio.mkdirs();
            }

            FileWriter relatorio = new FileWriter(dirRelatorio + "\\" + "Processamento_PED_INSTALL_" + formattedDate + ".txt");
            relatorio.write(content);
            relatorio.close();

            LOGGER.info(content);
            LOGGER.info("Sucesso: " + resultTests.getSucesso());
            LOGGER.info("Falha: " + resultTests.getFalha());

        } else {
            LOGGER.error("O diretorio nao contem arquivos para leitura.");
        }
    }
}
