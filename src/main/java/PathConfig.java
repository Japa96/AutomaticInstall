
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class PathConfig {

    private boolean readProperties;

    private static final Logger LOGGER = LogManager.getLogger(PathConfig.class.getName());

    // ENUM para leitura do config.properties
    private static final String CONCENTRATOR_URL = "concentratorurl";
    private static final String ENTERPRISE = "enterprise";
    private static final String DIRETORIO_AGENTE = "diretorioagente";
    private static final String DIRETORIO_SAIDA_AGENTE = "diretoriosaidaagente";
    private static final String NOME_AGENTE = "nomeagente";
    private static final String ROUTER_URL = "routerurl";
    private static final String WSURL = "wsurl";
    private static final String INPUT = "input";
    private static final String SET_NUM = "set_num";
    private static final String SERIE = "serie";
    private static final String NUMBER = "numero";
    private static final String MODULO = "modulo";
    private static final String TIMESLEEP = "timesleep";
    private static final String QUANTIDADEARQUIVOS = "quantidadearquivos";
    private static final String PROCESSASETNUM = "processasetnum";
    private static final String LOG4J2 = "log4j2";
    private static final String EMAIL = "email";

    private static final String CONFIG_DEFAULT = "Ajustar com config completo";

    public String getConcentratorTargetUrl() {
        return readProperties().getProperty(CONCENTRATOR_URL);
    }
    public String getEnterprise(){
        return readProperties().getProperty(ENTERPRISE);
    }
    public String getDiretorioAgente(){
        return readProperties().getProperty(DIRETORIO_AGENTE);
    }
    public String getDiretorioSaidaAgente(){
        return readProperties().getProperty(DIRETORIO_SAIDA_AGENTE);
    }
    public String getNomeAgente(){
        return readProperties().getProperty(NOME_AGENTE);
    }
    public String getRouterUrl(){
        return readProperties().getProperty(ROUTER_URL);
    }
    public String getWsurl(){
        return readProperties().getProperty(WSURL);
    }
    public String getInput(){
        return readProperties().getProperty(INPUT);
    }
    public String getLog4j2(){
        return readProperties().getProperty(LOG4J2);
    }
    public String getEmail(){return readProperties().getProperty(EMAIL);}
    public int getSetNum(){
        return Integer.parseInt(readProperties().getProperty(SET_NUM));
    }
    public int getSerie(){
        return Integer.parseInt(readProperties().getProperty(SERIE));
    }
    public int getNumber(){
        return Integer.parseInt(readProperties().getProperty(NUMBER));
    }
    public int getModulo(){
        return Integer.parseInt(readProperties().getProperty(MODULO));
    }
    public int getTimeSleep(){
        return Integer.parseInt(readProperties().getProperty(TIMESLEEP));
    }
    public int getQuantidadeArquivos(){
        return Integer.parseInt(readProperties().getProperty(QUANTIDADEARQUIVOS));
    }


    private final File basePath;

    private Properties propertiesRead;

    public PathConfig(File basePath, boolean readProperties) {
        this.basePath = basePath;
        this.readProperties = readProperties;
    }

    private Properties readProperties() {
        if (this.propertiesRead == null || this.readProperties) {
            File inFile = new File(this.basePath, "config.properties");
            Properties props = new Properties();
            FileInputStream in;
            try {
                in = new FileInputStream(inFile);
                try {
                    props.load(in);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Arquivo config.properties não existe");
                createEmptyProperties();
            } catch (IOException e) {
                throw new RuntimeException("Nao foi possivel ler o arquivo de propriedades em: " + inFile, e);
            }
            this.propertiesRead = props;
        }
        this.readProperties = false;
        return this.propertiesRead;
    }

    private void createEmptyProperties() {
        LOGGER.info("Gerando o arquivo config.properties");
        File properties = new File(getBasePath(), "config.properties");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(properties))) {
            writer.write(getPropertiesDefault());
            setPermission(properties);
            LOGGER.info("Arquivo config.properties padrao gerado com sucesso");
        } catch (IOException e) {
            LOGGER.error("Erro ao gerar o arquivo do config.properties", e);
        }
    }

    private String getPropertiesDefault() {
        return CONFIG_DEFAULT;
    }

    public File setPermission(File file) {
        if (file.isDirectory()) {
            file.mkdirs();
        }
        file.setReadable(Boolean.TRUE, Boolean.FALSE);
        file.setWritable(Boolean.TRUE, Boolean.FALSE);
        file.setExecutable(Boolean.TRUE, Boolean.FALSE);
        return file;
    }

    public File getBasePath() {
        return this.basePath;
    }

    public void loadConfigLog4j() {
        String pathLog4j = new File(this.basePath, "log4j2.xml").getAbsolutePath();
        ConfigurationSource source = null;
        try {
            source = new ConfigurationSource(new FileInputStream(pathLog4j));
        } catch (IOException ex) {
            LOGGER.info("Erro ao carregar arquivo log4j.xml" + ex);
        }
        Configurator.initialize(null, source);
    }
}
