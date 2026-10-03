module com.cynthiasystems.generaciondecodigointermedio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    opens com.cynthiasystems.generaciondecodigointermedio to javafx.fxml, javafx.base;

    exports com.cynthiasystems.generaciondecodigointermedio;
    exports com.cynthiasystems.generaciondecodigointermedio.registros;
    exports com.cynthiasystems.generaciondecodigointermedio.ensamblador;
    exports com.cynthiasystems.generaciondecodigointermedio.lenguajemaquina;
    exports com.cynthiasystems.generaciondecodigointermedio.memoria;
    exports com.cynthiasystems.generaciondecodigointermedio.semantico;
}
