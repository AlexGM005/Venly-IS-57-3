package com.venly.backend.dto;

import java.util.Map;

public class ApiErrorResponse {

    private int codigo;
    private String mensaje;
    private Map<String, String> errores;

    public ApiErrorResponse(int codigo, String mensaje, Map<String, String> errores) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
