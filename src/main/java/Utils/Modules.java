package Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Modules {


    AGENTE_CONCENTRATOR(1, "Enum do Agente Concentrador"),
    WEB_SERVICE_ROUTER(2, "Enum do Web Service Router"),
    WER_SERVICE_NFCE(3, "Enum do Web Service NFC-e");

    private final int code;
    private final String description;
    private static final Logger LOGGER = LogManager.getLogger(Modules.class.getName());

    Modules(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Modules getProcess(int code) {
        for (Modules requestCodeEnum : Modules.values()) {
            if (requestCodeEnum.getCode() == code) {
                return requestCodeEnum;
            }
        }
        return null;
    }

    public static boolean checkModule(int code) {
        for (Modules requestCodeEnum : Modules.values()) {
            if (requestCodeEnum.getCode() == code) {
                return true;
            }
        }
        LOGGER.info("Módulo Inesxistente.");
        return false;
    }
}


