package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Ізолює JavaFX від деталей HTTP: формує запити й перетворює JSON у Java-об'єкти. */
public class HttpDefectClient {

    private static final String BASE =
            "http://localhost:8080/construction-defects-server";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- будівельні об'єкти ----------
    public List<BuildingObject> getObjects() {
        return Arrays.asList(read(get("/objects"), BuildingObject[].class));
    }

    public void addObject(BuildingObject o) {
        post("/objects", params("code", o.getCode(), "name", o.getName(), "address", o.getAddress()));
    }

    public void updateObject(long id, BuildingObject o) {
        post("/objects", params("id", id, "code", o.getCode(), "name", o.getName(),
                "address", o.getAddress()));
    }

    public void deleteObject(long id) {
        get("/objects?action=delete&id=" + id);
    }

    // ---------- обстеження ----------
    public List<Inspection> getInspections() {
        return Arrays.asList(read(get("/inspections"), Inspection[].class));
    }

    public void addInspection(Inspection m) {
        post("/inspections", inspectionParams(null, m));
    }

    public void updateInspection(long id, Inspection m) {
        post("/inspections", inspectionParams(id, m));
    }

    public void deleteInspection(long id) {
        get("/inspections?action=delete&id=" + id);
    }

    private Map<String, String> inspectionParams(Long id, Inspection m) {
        Map<String, String> p = new LinkedHashMap<>();
        if (id != null) {
            p.put("id", String.valueOf(id));
        }
        p.put("objectId", String.valueOf(m.getObjectId()));
        p.put("defectType", m.getDefectType());
        p.put("measuredValue", String.valueOf(m.getMeasuredValue()));
        p.put("allowedValue", String.valueOf(m.getAllowedValue()));
        p.put("scaleScore", String.valueOf(m.getScaleScore()));
        p.put("safetyScore", String.valueOf(m.getSafetyScore()));
        return p;
    }

    // ---------- дефекти ----------
    public List<Defect> getDefects() {
        return Arrays.asList(read(get("/defects"), Defect[].class));
    }

    /** Створює дефект за обстеженням; F1 та I розраховує сервер. */
    public void addDefect(long inspectionId) {
        post("/defects", params("inspectionId", inspectionId));
    }

    /** Перераховує дефект (з тим самим або іншим обстеженням). */
    public void updateDefect(long id, long inspectionId) {
        post("/defects", params("id", id, "inspectionId", inspectionId));
    }

    public void changeDefectState(long id, DefectState state) {
        get("/defects?action=transition&id=" + id + "&state=" + state.name());
    }

    public void deleteDefect(long id) {
        get("/defects?action=delete&id=" + id);
    }

    // ---------- ремонтні завдання ----------
    public List<RepairTask> getTasks() {
        return Arrays.asList(read(get("/tasks"), RepairTask[].class));
    }

    public void addTask(RepairTask t) {
        post("/tasks", params("defectId", t.getDefectId(), "description", t.getDescription(),
                "plannedDate", t.getPlannedDate() == null ? "" : t.getPlannedDate()));
    }

    public void updateTask(long id, RepairTask t) {
        post("/tasks", params("id", id, "defectId", t.getDefectId(), "description", t.getDescription(),
                "plannedDate", t.getPlannedDate() == null ? "" : t.getPlannedDate()));
    }

    public void deleteTask(long id) {
        get("/tasks?action=delete&id=" + id);
    }

    // ---------- низькорівневі методи ----------
    private String get(String path) {
        return send(HttpRequest.newBuilder(URI.create(BASE + path)).GET().build());
    }

    private String post(String path, Map<String, String> params) {
        String body = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new RuntimeException(errorMessage(response));
            }
            return response.body();
        } catch (IOException e) {
            throw new ServerUnavailableException(
                    "Сервер недоступний. Запустіть Tomcat і перевірте адресу " + BASE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запит перервано", e);
        }
    }

    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode node = mapper.readTree(response.body());
            String error = node.path("error").asText("");
            if (!error.isEmpty()) {
                return error;
            }
        } catch (IOException ignored) {
            // тіло відповіді не є JSON
        }
        return "Помилка сервера (HTTP " + response.statusCode() + ")";
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Некоректна відповідь сервера: " + e.getMessage(), e);
        }
    }

    private static Map<String, String> params(Object... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        }
        return map;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
