package ua.edu.nupp.kpp.client;

/** Сервер недоступний (не запущений Tomcat, помилка мережі). */
public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
