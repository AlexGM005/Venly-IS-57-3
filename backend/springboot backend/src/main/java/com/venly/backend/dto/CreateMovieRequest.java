package com.venly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateMovieRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    private String titulo;

    @NotBlank(message = "El horario es obligatorio")
    @Pattern(regexp = ValidationPatterns.HORARIO, message = "El horario debe tener el formato HH:mm - HH:mm (ej. 14:00 - 16:30)")
    private String horario;

    @NotBlank(message = "La duración es obligatoria")
    @Pattern(regexp = ValidationPatterns.DURACION, message = "La duración debe tener el formato 2h 30min, 2h o 90min")
    private String duracion;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(max = 2000, message = "La sinopsis no puede exceder 2000 caracteres")
    private String sinopsis;

    @NotBlank(message = "La clasificación es obligatoria")
    @Pattern(regexp = ValidationPatterns.CLASIFICACION, message = "La clasificación debe ser G, PG, PG-13, R o NC-17")
    private String clasificacion;

    /** Optional image encoded in base64 (a "data:image/...;base64," prefix is accepted). */
    private String imagenBase64;

    private String imagenTipo;

    @NotNull(message = "La sala es obligatoria")
    @Positive(message = "La sala no es válida")
    private Long salaId;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }

    public String getImagenTipo() { return imagenTipo; }
    public void setImagenTipo(String imagenTipo) { this.imagenTipo = imagenTipo; }

    public Long getSalaId() { return salaId; }
    public void setSalaId(Long salaId) { this.salaId = salaId; }
}
