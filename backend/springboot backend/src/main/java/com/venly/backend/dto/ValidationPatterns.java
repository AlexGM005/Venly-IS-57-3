package com.venly.backend.dto;

/** Regular expressions shared by request DTOs (kept in sync with the frontend validators). */
public final class ValidationPatterns {

    public static final String NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$";
    public static final String CEDULA = "^[0-9]{6,12}$";
    public static final String TELEFONO = "^[0-9]{10,15}$";
    public static final String CONTRASENA = "^(?=.*[A-Za-z])(?=.*[0-9]).+$";
    public static final String HORARIO = "^([01]\\d|2[0-3]):[0-5]\\d\\s*-\\s*([01]\\d|2[0-3]):[0-5]\\d$";
    public static final String DURACION = "^(\\d{1,2}h( \\d{1,2}min)?|\\d{1,3}min)$";
    public static final String CLASIFICACION = "^(G|PG|PG-13|R|NC-17)$";

    private ValidationPatterns() {
    }
}
