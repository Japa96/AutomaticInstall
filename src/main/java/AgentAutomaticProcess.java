import Utils.Modules;
import Utils.SetNum;

import javax.swing.text.html.parser.Parser;
import java.io.File;
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
    private static int set_num;
    private static int serie;
    private static int number;
    private static int modulo;
    private static int timeSleep;
    private static int quantidadeArquivos;

    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        RestartServiceController restartServiceController = new RestartServiceController();

        System.out.println("###################   Remoção Control e Logs   ###################\n");

        boolean ModuleStatusService = restartServiceController.checkStatusService();

        if (ModuleStatusService){

            try {
                restartServiceController.stopService();

                Thread.sleep(5000);

                System.out.println("\nTentando deletar diretório Control e Logs...");
                boolean statusDelete = tryDeleteFolderControlLogs();

                if (!statusDelete){

                    Thread.sleep(5000);
                    tryDeleteFolderControlLogs();
                    System.out.println("\nFinalizado processo de exclusão de diretórios.");
                }

            }catch(Exception exception){

                exception.getMessage();
            }

        }else{
            try {
                boolean statusDelete = tryDeleteFolderControlLogs();
                System.out.println("\nFinalizado processo de exclusão de diretórios.");

            }catch(Exception exception){

                exception.getMessage();
            }
        }


        System.out.println("###################   Checagem serviço   ###################\n");

        boolean statusService = restartServiceController.checkStatusService();

        if(statusService == true){
            System.out.println("\nServiço já está iniciado.");
        }else {
            restartServiceController.startService();
            Thread.sleep(2000);

            boolean checkServiceAgain = restartServiceController.checkStatusService();

            while (checkServiceAgain == false){

                System.out.println("Erro ao iniciar o serviço. Checar se o mesmo está desativado.");

                Thread.sleep(5000);

                checkServiceAgain = restartServiceController.checkStatusService();
                restartServiceController.startService();
            }

            System.out.println("\nServiço iniciado com sucesso.");

        }

        System.out.println("\n\n###################   Bem-vindo a ferramenta de ped_install   ###################");

        System.out.println(" 1 - Agente/Concentrador");
        System.out.println(" 2 - Web Service Router");
        System.out.println(" 3 - Web Service NFC-e");

        do {
            System.out.print("Informe para qual módulo deseja enviar o arquivo de ped_install: ");
            modulo = scanner.nextInt();
            scanner.nextLine();
        }
        while (!Modules.checkModule(modulo));


        System.out.print("Informe o diretório de processamento: ");
        diretorioAgente = scanner.nextLine();

        System.out.println("\n##################");
        checkFilesInDirectory();
        System.out.println("##################\n");

        System.out.print("Informe o diretório de retorno de processamento: ");
        diretorioSaidaAgente = scanner.nextLine();

        System.out.println("\n##################");
        checkFilesOutDirectory();
        System.out.println("##################\n");

        System.out.print("Informe a quantidade de arquivos a processar: ");
        quantidadeArquivos = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Informe o nome do agente: ");
        nomeAgente = scanner.next();
        nomeAgente = nomeAgente.toUpperCase();
        scanner.nextLine();

        System.out.print("Informe o Time Sleep(s): ");
        transformSecondsToMilliseconds(scanner);
        scanner.nextLine();

        switch (Modules.getProcess(modulo)) {
            case AGENTE_CONCENTRATOR:

                System.out.print("Informe o concentratorurl. Linha 1000 do manual: ");
                concentratorurl = scanner.next();
                concentratorurl = concentratorurl.toLowerCase();

                System.out.print("Informe a enterprise. Linha 2000 do manual: ");
                enterprise = scanner.next();

                askSetNumUser(scanner);

                checkAnswerSetNumUser(scanner);

                break;

            case WEB_SERVICE_ROUTER:

                System.out.print("Informe o routerurl. Linha 1001 do manual: ");
                routerurl = scanner.next();

                System.out.print("Informe a enterprise. Linha 2000 do manual: ");
                enterprise = scanner.next();

                askSetNumUser(scanner);

                checkAnswerSetNumUser(scanner);

                break;

            case WER_SERVICE_NFCE:
                System.out.print("Informe o wsurl. Linha 1002 do manual: ");
                wsurl = scanner.next();

                System.out.print("Informe a enterprise. Linha 2000 do manual: ");
                enterprise = scanner.next();

                askSetNumUser(scanner);

                checkAnswerSetNumUser(scanner);

                break;
        }

    }

    private static void askSetNumUser(Scanner scanner) {
        do {
            System.out.print("Deseja processar um Set Num junto ao ped_intall? 1 - Sim / 2 - Não - : ");
            set_num = scanner.nextInt();
        }while (!SetNum.checkSetNum(set_num));
    }

    private static void transformSecondsToMilliseconds(Scanner scanner) {
        timeSleep = scanner.nextInt() * 1000;
    }

    private static void checkAnswerSetNumUser(Scanner scanner) throws InterruptedException {
        if (set_num == SetNum.SET_NUM_EXISTENTE.getCode()) {
            System.out.print("Informe a série do do agente: ");
            serie = scanner.nextInt();

            System.out.print("Informe o número de início do agente: \n");
            number = scanner.nextInt();
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

    public static void checkFilesOutDirectory(){
        File diretory = new File(diretorioSaidaAgente);

        if(diretory.isDirectory()){
            String[] filesDirectory = diretory.list();

            if (filesDirectory.length > 0){
                int filesInDirectory = filesDirectory.length;
                System.out.println("Diretório não está vazio. Contém " + filesInDirectory + " arquivos.");
                System.out.println("Iniciando limpeza da mesma...");

                for (File cleanDirectory: diretory.listFiles()){
                    cleanDirectory.delete();
                }

                System.out.println("Diretório limpo com sucesso.");

            }else{
                System.out.println("Diretório vazio.");
            }
        }
    }

    public static void checkFilesInDirectory(){
        File diretory = new File(diretorioAgente);

        if(diretory.isDirectory()){
            String[] filesDirectory = diretory.list();

            if (filesDirectory.length > 0){
                int filesInDirectory = filesDirectory.length;
                System.out.println("Diretório não está vazio. Contém " + filesInDirectory + " arquivos.");

                for (File cleanDirectory: diretory.listFiles()){
                    cleanDirectory.delete();
                }

                System.out.println("Diretório limpo com sucesso.");

            }else{
                System.out.println("Diretório vazio.");
            }
        }
    }

    public static boolean tryDeleteFolderControlLogs() {

        String control = "C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\control";
        String logs = "C:\\Program Files\\NDDigital\\eForms_NFCe\\Agent Service\\logs";

        File directoryControl = new File(control);
        File directoryLogs = new File(logs);

        if (directoryControl.isDirectory()){
            directoryControl.delete();
            System.out.println("Diretório control excluído com sucesso.");

            if (directoryLogs.isDirectory()){
                directoryLogs.delete();
                System.out.println("Diretório logs excluído com sucesso.");
                return true;
            }
        }
            return false;
    }

}
