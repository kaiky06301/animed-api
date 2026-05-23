package br.com.fiap.clyvovet.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " com ID " + id + " não encontrado(a).");
    }
}
