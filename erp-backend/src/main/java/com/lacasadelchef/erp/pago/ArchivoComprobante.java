package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

final class ArchivoComprobante {

    static final int TAMANO_MAXIMO = 5 * 1024 * 1024;

    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F'};
    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    record Revisado(String nombre, String tipoContenido, byte[] contenido) {
    }

    private ArchivoComprobante() {
    }

    static Revisado revisar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo del comprobante está vacío");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new BusinessException("El archivo pesa más de 5 MB");
        }
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo del comprobante", e);
        }
        return new Revisado(nombre(archivo.getOriginalFilename()), tipoContenido(contenido), contenido);
    }

    static String tipoContenido(byte[] contenido) {
        if (empiezaCon(contenido, FIRMA_PDF)) {
            return "application/pdf";
        }
        if (empiezaCon(contenido, FIRMA_JPG)) {
            return "image/jpeg";
        }
        if (empiezaCon(contenido, FIRMA_PNG)) {
            return "image/png";
        }
        throw new BusinessException("El comprobante tiene que ser un PDF o una imagen JPG o PNG");
    }

    static String nombre(String original) {
        String nombre = original == null ? "" : original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").trim();
        if (nombre.isEmpty()) {
            nombre = "comprobante";
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private static boolean empiezaCon(byte[] contenido, byte[] firma) {
        return contenido.length >= firma.length && Arrays.equals(contenido, 0, firma.length, firma, 0, firma.length);
    }
}
