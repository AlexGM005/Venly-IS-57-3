package com.venly.backend.dto;

import java.util.List;

public class SeatMapResponse {

    private Long peliculaId;
    private String peliculaTitulo;
    private String salaNombre;
    private String horario;
    private int filas;
    private int columnas;
    private int maxAsientosPorCompra;
    private double precioUnitario;
    private List<SeatDto> asientos;

    public Long getPeliculaId() { return peliculaId; }
    public void setPeliculaId(Long peliculaId) { this.peliculaId = peliculaId; }

    public String getPeliculaTitulo() { return peliculaTitulo; }
    public void setPeliculaTitulo(String peliculaTitulo) { this.peliculaTitulo = peliculaTitulo; }

    public String getSalaNombre() { return salaNombre; }
    public void setSalaNombre(String salaNombre) { this.salaNombre = salaNombre; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public int getFilas() { return filas; }
    public void setFilas(int filas) { this.filas = filas; }

    public int getColumnas() { return columnas; }
    public void setColumnas(int columnas) { this.columnas = columnas; }

    public int getMaxAsientosPorCompra() { return maxAsientosPorCompra; }
    public void setMaxAsientosPorCompra(int maxAsientosPorCompra) { this.maxAsientosPorCompra = maxAsientosPorCompra; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public List<SeatDto> getAsientos() { return asientos; }
    public void setAsientos(List<SeatDto> asientos) { this.asientos = asientos; }
}
