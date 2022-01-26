package Utils;

public enum Modules {


    AGENTE_CONCENTRATOR(1, "Enum do Agente Concentrador"),
    WEB_SERVICE_ROUTER(2, "Enum do Web Service Router"),
    WER_SERVICE_NFCE(3, "Enum do Web Service NFC-e");

    private final int code;
    private final String description;

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
        System.out.println("Módulo Inesxistente.");
        return false;
    }
}


