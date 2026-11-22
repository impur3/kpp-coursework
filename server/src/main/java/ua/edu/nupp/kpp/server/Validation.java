package ua.edu.nupp.kpp.server;

final class Validation {
    private Validation() {
    }

    static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
