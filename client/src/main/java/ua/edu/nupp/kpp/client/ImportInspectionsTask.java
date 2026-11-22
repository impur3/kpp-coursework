package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Фоновий імпорт результатів обстежень із CSV (не блокує JavaFX Application Thread).
 * Предметна валідація і запис у MySQL виконуються на сервері; клієнт лише читає файл
 * і передає записи через HTTP-клієнт.
 */
public class ImportInspectionsTask extends Task<Integer> {
    private static final Logger LOG = Logger.getLogger(ImportInspectionsTask.class.getName());

    static final String HEADER =
            "object_id;defect_type;measured_value;allowed_value;scale_score;safety_score";

    /** Невелика пауза на рядок, щоб прогрес було видно навіть на малому файлі. */
    private static final long DELAY_MS = 400;

    private final Path file;
    private final HttpDefectClient api;
    private volatile int skipped;

    public ImportInspectionsTask(Path file, HttpDefectClient api) {
        this.file = file;
        this.api = api;
    }

    public int getSkipped() {
        return skipped;
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок імпорту: " + file);
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (java.nio.file.NoSuchFileException e) {
            LOG.severe("CSV-файл не знайдено: " + file);
            throw new IllegalStateException("CSV-файл не знайдено: " + file);
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV-файл порожній");
        }
        String header = lines.get(0).replace("\uFEFF", "").trim();
        if (!HEADER.equals(header)) {
            LOG.severe("Неправильний заголовок CSV: " + header);
            throw new IllegalArgumentException(
                    "Неправильний заголовок CSV. Очікується: " + HEADER);
        }

        // Уже наявні обстеження: дозволяє не створювати дублікатів при повторному імпорті.
        // Якщо сервер недоступний, виняток перериває імпорт.
        Set<String> existing = new HashSet<>();
        for (Inspection e : api.getInspections()) {
            existing.add(key(e));
        }

        List<String> rows = lines.stream().skip(1).filter(l -> !l.isBlank()).toList();
        int total = rows.size();
        int imported = 0;
        for (int i = 0; i < total; i++) {
            if (isCancelled()) {
                break;
            }
            int lineNo = i + 2;
            try {
                Inspection m = parse(rows.get(i));
                if (existing.contains(key(m))) {
                    skipped++;
                    LOG.warning("Рядок " + lineNo + " пропущено: такий запис уже існує (дублікат)");
                } else {
                    api.addInspection(m);
                    existing.add(key(m));
                    imported++;
                }
            } catch (ServerUnavailableException e) {
                LOG.severe("Імпорт перервано: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                skipped++;
                LOG.warning("Рядок " + lineNo + " пропущено: " + e.getMessage());
            }
            updateProgress(i + 1, total);
            updateMessage("Оброблено " + (i + 1) + " з " + total);
            Thread.sleep(DELAY_MS);
        }
        LOG.info("Імпорт завершено: імпортовано " + imported + ", пропущено " + skipped);
        return imported;
    }

    private static String key(Inspection m) {
        return m.getObjectId() + "|" + m.getDefectType() + "|" + m.getMeasuredValue() + "|"
                + m.getAllowedValue() + "|" + m.getScaleScore() + "|" + m.getSafetyScore();
    }

    private Inspection parse(String line) {
        String[] p = line.split(";", -1);
        if (p.length != 6) {
            throw new IllegalArgumentException("очікується 6 полів, знайдено " + p.length);
        }
        try {
            Inspection m = new Inspection();
            m.setObjectId(Long.parseLong(p[0].trim()));
            m.setDefectType(p[1].trim());
            m.setMeasuredValue(Double.parseDouble(p[2].trim()));
            m.setAllowedValue(Double.parseDouble(p[3].trim()));
            m.setScaleScore(Double.parseDouble(p[4].trim()));
            m.setSafetyScore(Double.parseDouble(p[5].trim()));
            return m;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("некоректне число: " + e.getMessage());
        }
    }
}
