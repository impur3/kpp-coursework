package ua.edu.nupp.kpp.server;

/** Розрахунок факторів та інтегрального показника I = 0,40·F1 + 0,35·F2 + 0,25·F3. */
public final class RiskCalculator {

    private RiskCalculator() {
    }

    /**
     * F1 — нормована тяжкість дефекту: перевищення допустимого значення у відсотках, обмежене 0..100.
     * Якщо виміряне значення не перевищує допустиме, F1 = 0.
     */
    public static double f1(double measuredValue, double allowedValue) {
        if (allowedValue <= 0) {
            throw new IllegalArgumentException("Допустиме значення має бути більшим за нуль");
        }
        double raw = (measuredValue - allowedValue) * 100.0 / allowedValue;
        return Math.max(0, Math.min(100, raw));
    }

    /** Інтегральний показник I (0..100), округлений до двох знаків. */
    public static double risk(double f1, double f2, double f3) {
        check(f1, "F1");
        check(f2, "F2");
        check(f3, "F3");
        double value = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(value * 100.0) / 100.0;
    }

    private static void check(double f, String name) {
        if (Double.isNaN(f) || f < 0 || f > 100) {
            throw new IllegalArgumentException(name + " має бути в діапазоні 0..100");
        }
    }
}
