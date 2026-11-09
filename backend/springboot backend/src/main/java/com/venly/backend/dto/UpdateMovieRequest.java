package com.venly.backend.dto;

/**
 * Same fields and validations as creation. When {@code imagenBase64} is empty the current image is kept,
 * unless {@code quitarImagen} is true.
 */
public class UpdateMovieRequest extends CreateMovieRequest {

    private boolean quitarImagen;

    public boolean isQuitarImagen() { return quitarImagen; }
    public void setQuitarImagen(boolean quitarImagen) { this.quitarImagen = quitarImagen; }
}
