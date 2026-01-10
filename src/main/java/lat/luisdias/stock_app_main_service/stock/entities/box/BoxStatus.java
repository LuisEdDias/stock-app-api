package lat.luisdias.stock_app_main_service.stock.entities.box;

public enum BoxStatus {
    EMPTY("MUITO ESPAÇO"),
    SOME_SPACE("POUCO ESPAÇO"),
    FULL("CHEIO");

    public final String nameStr;

    BoxStatus(String nameStr){
        this.nameStr = nameStr;
    }
}
