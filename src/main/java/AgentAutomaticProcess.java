import Utils.Modules;
import Utils.SetNum;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.parser.Parser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
    private static int quantidadeArquivos;
    private static int sucesso = 0;
    private static int falha = 0;

    private static final Logger LOGGER = LogManager.getLogger(AgentAutomaticProcess.class.getName());

    public static void main(String[] args) throws InterruptedException, IOException {

        PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
        pathConfig.loadConfigLog4j();

        Scanner scanner = new Scanner(System.in);
        RestartServiceController restartServiceController = new RestartServiceController();


        System.out.println("###################   Remocao Control e Logs   ###################\n");

        boolean ModuleStatusService = restartServiceController.checkStatusService();

        if (ModuleStatusService) {

            try {
                restartServiceController.stopService();

                Thread.sleep(20000);

                System.out.println("\nTentando deletar diretorio Control e Logs...");
                boolean statusDelete = tryDeleteFolderControlLogs();

                if (!statusDelete) {

                    Thread.sleep(10000);
                    tryDeleteFolderControlLogs();
                    Thread.sleep(10000);
                    System.out.println("\nFinalizado processo de exclusao de diretorios.");
                }

            } catch (Exception exception) {

                exception.getMessage();
            }

        } else {
            try {
                boolean statusDelete = tryDeleteFolderControlLogs();
                System.out.println("\nFinalizado processo de exclusao de diretorios.");

            } catch (Exception exception) {

                exception.getMessage();
            }
        }


        System.out.println("\n###################   Checagem servico   ###################\n");

        boolean statusService = restartServiceController.checkStatusService();

        if (statusService == true) {
            System.out.println("\nServico ja esta iniciado.");
        } else {
            restartServiceController.startService();
            Thread.sleep(2000);

            boolean checkServiceAgain = restartServiceController.checkStatusService();

            while (checkServiceAgain == false) {

                System.out.println("Erro ao iniciar o servico. Checar se o mesmo esta desativado.");

                Thread.sleep(5000);

                checkServiceAgain = restartServiceController.checkStatusService();
                restartServiceController.startService();
            }

            System.out.println("\nServico iniciado com sucesso.");

        }

        System.out.println("\n\n###################   Bem-vindo a ferramenta de ped_install   ###################");

        System.out.println(" 1 - Agente/Concentrador");
        System.out.println(" 2 - Web Service Router");
        System.out.println(" 3 - Web Service NFC-e");

        do {
            modulo = pathConfig.getModulo();
            //scanner.nextLine();
        }
        while (!Modules.checkModule(modulo));


        diretorioAgente = pathConfig.getDiretorioAgente();
        //diretorioAgente = scanner.nextLine();

        System.out.println("\n##################");
        checkFilesInDirectory();
        System.out.println("##################\n");

        diretorioSaidaAgente = pathConfig.getDiretorioSaidaAgente();
        //diretorioSaidaAgente = scanner.nextLine();

        System.out.println("\n##################");
        checkFilesOutDirectory();
        System.out.println("##################\n");

        quantidadeArquivos = pathConfig.getQuantidadeArquivos();
        //quantidadeArquivos = scanner.nextInt();
        //scanner.nextLine();

        nomeAgente = pathConfig.getNomeAgente();
        //nomeAgente = scanner.next();
        nomeAgente = nomeAgente.toUpperCase();
        //scanner.nextLine();

        timeSleep = pathConfig.getTimeSleep();
        transformSecondsToMilliseconds();
        //scanner.nextLine();

        switch (Modules.getProcess(modulo)) {
            case AGENTE_CONCENTRATOR:

                //System.out.print("Informe o concentratorurl. Linha 1000 do manual: ");
                concentratorurl = pathConfig.getConcentratorTargetUrl();
                concentratorurl = concentratorurl.toLowerCase();

                enterprise = pathConfig.getEnterprise();
                //enterprise = scanner.next();

                askSetNumUser();

                checkAnswerSetNumUser();

                break;

            case WEB_SERVICE_ROUTER:

                routerurl = pathConfig.getRouterUrl();
                //routerurl = scanner.next();

                enterprise = pathConfig.getEnterprise();
                //enterprise = scanner.next();

                askSetNumUser();

                checkAnswerSetNumUser();

                break;

            case WER_SERVICE_NFCE:
                wsurl = pathConfig.getWsurl();
                //wsurl = scanner.next();

                enterprise = pathConfig.getEnterprise();
                //enterprise = scanner.next();

                askSetNumUser();

                checkAnswerSetNumUser();

                break;
        }

        while (checkQuantityFilesProcessed() == false){
            System.out.println("\nAguardando retorno de todos os documentos processados.");
            Thread.sleep(8000);
        }

        filesToRead();

    }

    private static void askSetNumUser() {
        do {
            PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);
            set_num = pathConfig.getSetNum();
            //set_num = scanner.nextInt();
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
            //serie = scanner.nextInt();

            number = pathConfig.getNumber();
            //number = scanner.nextInt();
        }

        generateFile();
    }

    public static void generateFile() throws InterruptedException {
        if (set_num == SetNum.SET_NUM_EXISTENTE.getCode()) {
            System.out.println("########## Em processamento ##########\n");
            for (int i = 1; i <= quantidadeArquivos; i++) {
                writeFileWithSetNum(i);
                System.out.println("O arquivo com SET_NUM do " + nomeAgente + " " + i + " foi gerado com sucesso.");
                Thread.sleep(timeSleep);
            }

            System.out.println("\nFinalizado o processamento de " + quantidadeArquivos + " arquivo(s) com SET_NUM para o Agente: " + nomeAgente);
            System.out.println("\n########## Finalizado processamento ##########");

        }
        if (set_num == SetNum.SET_NUM_INEXISTENTE.getCode()) {
            System.out.println("########## Em processamento ##########\n");
            for (int i = 1; i <= quantidadeArquivos; i++) {
                writeFileWithOutSetNum(i);
                System.out.println("O arquivo  do " + nomeAgente + " " + i + " foi gerado com sucesso.");
                Thread.sleep(timeSleep);
            }
            System.out.println("\nFinalizado o processamento de " + quantidadeArquivos + " arquivo(s) para o Agente: " + nomeAgente);
            System.out.println("\n########## Finalizado processamento ##########");
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
                System.out.println("Diretorio nao esta vazio. Contem " + filesInDirectory + " arquivos.");
                System.out.println("Iniciando limpeza da mesma...");

                for (File cleanDirectory : diretory.listFiles()) {
                    cleanDirectory.delete();
                }

                System.out.println("Diretorio limpo com sucesso.");

            } else {
                System.out.println("Diretorio vazio.");
            }
        }
    }

    public static void checkFilesInDirectory() {
        File diretory = new File(diretorioAgente);

        if (diretory.isDirectory()) {
            String[] filesDirectory = diretory.list();

            if (filesDirectory.length > 0) {
                int filesInDirectory = filesDirectory.length;
                System.out.println("Diretorio nao esta vazio. Contem " + filesInDirectory + " arquivos.");

                for (File cleanDirectory : diretory.listFiles()) {
                    cleanDirectory.delete();
                }

                System.out.println("Diretorio limpo com sucesso.");

            } else {
                System.out.println("Diretorio vazio.");
            }
        }
    }

    public static boolean checkQuantityFilesProcessed(){

        File diretory = new File(diretorioSaidaAgente);
        String[] filesToCount = diretory.list();

        if (filesToCount.length == quantidadeArquivos){
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
            //directoryControl.delete();
            System.out.println("Diretorio control excluido com sucesso.");

            if (directoryLogs.isDirectory()) {
                FileUtils.deleteDirectory(directoryLogs);
                //directoryLogs.delete();
                System.out.println("Diretorio logs excluido com sucesso.");
                return true;
            }
        }
        return false;
    }

    public static void filesToRead() throws IOException {

        File diretory = new File(diretorioSaidaAgente);
        String[] filesToRead = diretory.list();

        System.out.println("\n\n########## Iniciando leitura do diretorio de SAIDA ##########\n");

        if (filesToRead.length > 0) {
            int quantityToRead = filesToRead.length;
            System.out.println("O diretorio contem " + quantityToRead + " arquivos para leitura.\n");

            File fileList[] = diretory.listFiles();
            System.out.println("Lista de arquivos no direterio de saida: ");

            for (File file : fileList) {
                System.out.println("Nome do arquivo: " + file.getName());
                System.out.println("Diretorio do arquivo: " + file.getAbsolutePath());

                input = FileUtils.readFileToString(file);

                if (input.contains("<code>100</code>")) {
                    sucesso += 1;
                }
                if (!input.contains("<code>100</code>")) {
                    falha += 1;
                }

                System.out.println("Conteudo do arquivo: " + input);

            }
            System.out.println("Sucesso: " + sucesso);
            System.out.println("Falha: " + falha);

        } else {
            System.out.println("O diretorio nao contem arquivos para leitura.");
        }
    }
}
