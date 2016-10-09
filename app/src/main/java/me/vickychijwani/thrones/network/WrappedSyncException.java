package me.vickychijwani.thrones.network;

final class WrappedSyncException extends RuntimeException {

    WrappedSyncException(String message, Throwable cause) {
        super(message, cause);
    }

}
