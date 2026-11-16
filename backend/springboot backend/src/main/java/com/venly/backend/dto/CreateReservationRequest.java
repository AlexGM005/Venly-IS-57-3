package com.venly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateReservationRequest {

    @NotNull(message = "La función es obligatoria")
    private Long peliculaId;

    @NotEmpty(message = "Debes seleccionar al menos un asiento")
    private List<String> asientos;

    @NotBlank(message = "El titular de la tarjeta es obligatorio")
    private String titular;

    @NotBlank(message = "El número de tarjeta es obligatorio")
    private String numeroTarjeta;

    @NotBlank(message = "El vencimiento es obligatorio")
    private String vencimiento;

    @NotBlank(message = "El CVV es obligatorio")
    private String cvv;

    public Long getPeliculaId() { return peliculaId; }
    public void setPeliculaId(Long peliculaId) { this.peliculaId = peliculaId; }

    public List<String> getAsientos() { return asientos; }
    public void setAsientos(List<String> asientos) { this.asientos = asientos; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getVencimiento() { return vencimiento; }
    public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
