package com.venly.backend.dto;

import com.venly.backend.model.Role;

public class ProfileResponse {

    private Long id;
    private String nombre;
    private String cedula;
    private String correo;
    private String telefono;
    private Role rol;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }
}
