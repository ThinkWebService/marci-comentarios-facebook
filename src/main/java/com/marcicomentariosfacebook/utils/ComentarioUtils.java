package com.marcicomentariosfacebook.utils;

public class ComentarioUtils {

    /**
     * Limpia un texto eliminando caracteres no alfabéticos o numéricos, dejando solo letras, números y espacios.
     *
     * @param mensaje El mensaje original.
     * @return Texto limpio.
     */
    public static String limpiarTexto(String mensaje) {
        if (mensaje == null) return "";

        // Elimina símbolos raros que no aportan al significado
        String limpio = mensaje.replaceAll("[^\\p{L}\\p{N}\\s]", ""); // solo letras, números y espacios
        limpio = limpio.replaceAll("\\s+", " ").trim(); // limpia espacios extra
        return limpio;
    }

    /**
     * Verifica si un mensaje es válido: no nulo, con al menos 3 palabras y no es solo números.
     *
     * @param mensaje El mensaje original.
     * @return true si el mensaje es considerado válido.
     */
    public static boolean esMensajeValido(String mensaje) {
        String limpio = limpiarTexto(mensaje);
        return limpio.split(" ").length >= 3 && !limpio.matches("^\\d+$");
    }
}
