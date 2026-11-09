package com.venly.backend.dto;

import com.venly.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    @Pattern(regexp = ValidationPatterns.NOMBRE, message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = ValidationPatterns.CEDULA, message = "La cédula debe contener entre 6 y 12 dígitos")
    private String cedula;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = ValidationPatterns.TELEFONO, message = "El teléfono debe contener entre 10 y 15 dígitos")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Pattern(regexp = ValidationPatterns.CONTRASENA, message = "La contraseña debe contener letras y números")
    private String contrasena;

    private Role rol;

    private String claveOrganizacion;

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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public String getClaveOrganizacion() {
        return claveOrganizacion;
    }

    public void setClaveOrganizacion(String claveOrganizacion) {
        this.claveOrganizacion = claveOrganizacion;
    }
}
