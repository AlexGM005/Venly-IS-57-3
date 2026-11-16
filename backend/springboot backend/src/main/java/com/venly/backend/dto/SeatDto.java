package com.venly.backend.dto;

public class SeatDto {

    private String codigo;
    private String estado;

    public SeatDto() {
    }

    public SeatDto(String codigo, String estado) {
        this.codigo = codigo;
        this.estado = estado;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
