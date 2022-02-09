package Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum SetNum {

    SET_NUM_EXISTENTE(1, "Há SET_NUM no arquivo de ped_install"),
    SET_NUM_INEXISTENTE(2, "Não há SET_NUM no arquivo de ped_install");

    private final int code;
    private final String description;
    private static final Logger LOGGER = LogManager.getLogger(SetNum.class.getName());

    SetNum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public final int getCode() {
        return code;
    }

    public final String getDescription() {
        return description;
    }

    public static SetNum getProcess(int code) {
        for (SetNum requestCodeEnum : SetNum.values()) {
            if (requestCodeEnum.getCode() == code) {
                return requestCodeEnum;
            }
        }
        return null;
    }

    public static boolean checkSetNum(int code) {
        for (SetNum requestCodeEnum : SetNum.values()) {
            if (requestCodeEnum.getCode() == code) {
                return true;
            }
        }
        LOGGER.info("Parâmetro de SET_NUM Inexistente.");
        return false;
    }
}
