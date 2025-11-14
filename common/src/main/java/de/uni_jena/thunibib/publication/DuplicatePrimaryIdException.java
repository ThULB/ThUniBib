package de.uni_jena.thunibib.publication;

class DuplicatePrimaryIdException extends RuntimeException {

    DuplicatePrimaryIdException(String message) {
        super(message);
    }
}
