package ua.edu.nupp.kpp.client;

/** Стани життєвого циклу дефекту (назви збігаються із серверними). */
public enum DefectState {
    RECORDED("ЗАФІКСОВАНО"),
    VERIFIED("ПЕРЕВІРЕНО"),
    REPAIR_ASSIGNED("РЕМОНТ_ПРИЗНАЧЕНО"),
    CLOSED("ЗАКРИТО");

    private final String label;

    DefectState(String label) {
        this.label = label;
    }

    /** Українська назва показується в таблицях та списках. */
    @Override
    public String toString() {
        return label;
    }
}
