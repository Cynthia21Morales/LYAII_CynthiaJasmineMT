package com.cynthiasystems.generaciondecodigointermedio;

import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void irARegistros() {
        Navegador.cargar("registros/registros-view.fxml");
    }

    @FXML
    private void irAEnsamblador() {
        Navegador.cargar("ensamblador/ensamblador-view.fxml");
    }

    @FXML
    private void irALenguajeMaquina() {
        Navegador.cargar("lenguajemaquina/lenguajemaquina-view.fxml");
    }

    @FXML
    private void irAMemoria() {
        Navegador.cargar("memoria/memoria-view.fxml");
    }
}
