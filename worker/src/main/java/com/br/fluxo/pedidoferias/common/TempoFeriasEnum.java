package com.br.fluxo.pedidoferias.common;

public enum TempoFeriasEnum {
    SETE_DIAS(7),
    QUINZE_DIAS(15),
    TRINTA_DIAS(30);

    private int valor;

    public int getValor() {
        return valor;
    }

    TempoFeriasEnum(int valor) {this.valor = valor;}
}
