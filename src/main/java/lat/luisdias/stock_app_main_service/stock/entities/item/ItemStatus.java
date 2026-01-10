package lat.luisdias.stock_app_main_service.stock.entities.item;

public enum ItemStatus {
    TESTED_OK("TESTADO OK"),
    TO_TEST("TESTAR"),
    FAULTY("DEFEITO"),
    WRITE_OFF("BAIXA");

    public final String nameStr;

    ItemStatus(String nameStr){
        this.nameStr = nameStr;
    }
}
