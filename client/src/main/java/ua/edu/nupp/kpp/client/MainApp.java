package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.logging.Logger;

/** Головне вікно JavaFX-клієнта: чотири вкладки, TableView і форми введення. */
public class MainApp extends Application {
    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private final HttpDefectClient api = new HttpDefectClient();

    private final ObservableList<BuildingObject> objects = FXCollections.observableArrayList();
    private final ObservableList<Inspection> inspections = FXCollections.observableArrayList();
    private final ObservableList<Defect> defects = FXCollections.observableArrayList();
    private final ObservableList<RepairTask> tasks = FXCollections.observableArrayList();

    private final ProgressBar progress = new ProgressBar(0);
    private final Label status = new Label("");

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(objectsTab(), inspectionsTab(), defectsTab(), tasksTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setTitle("Облік дефектів будівельних об’єктів");
        stage.setScene(new Scene(tabs, 1050, 560));
        stage.show();

        safe(this::refreshAll);
    }

    // ------------------------------------------------------------------ вкладки

    private Tab objectsTab() {
        TableView<BuildingObject> table = new TableView<>(objects);
        columns(table, "ID", "id", 60, "Код", "code", 100, "Назва", "name", 220, "Адреса", "address", 260);

        TextField code = new TextField();
        TextField name = new TextField();
        TextField address = new TextField();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                code.setText(s.getCode());
                name.setText(s.getName());
                address.setText(s.getAddress());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addObject(new BuildingObject(code.getText(), name.getText(), address.getText()));
            refreshObjects();
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(e -> safe(() -> {
            BuildingObject sel = selected(table);
            api.updateObject(sel.getId(), new BuildingObject(code.getText(), name.getText(), address.getText()));
            refreshObjects();
        }));
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteObject(selected(table).getId());
            refreshAll();
        }));
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshObjects));

        VBox form = form("Додати / Редагувати об’єкт",
                "Код", code, "Назва", name, "Адреса", address, buttons(add, edit, delete, refresh));
        return tab("Будівельні об’єкти", table, form, null);
    }

    private Tab inspectionsTab() {
        TableView<Inspection> table = new TableView<>(inspections);
        columns(table, "ID", "id", 50, "Об’єкт", "objectId", 70, "Тип дефекту", "defectType", 200,
                "Виміряне", "measuredValue", 90, "Норматив", "allowedValue", 90,
                "F2", "scaleScore", 60, "F3", "safetyScore", 60);

        TextField objectId = new TextField();
        TextField defectType = new TextField();
        TextField measured = new TextField();
        TextField allowed = new TextField();
        TextField f2 = new TextField();
        TextField f3 = new TextField();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                objectId.setText(String.valueOf(s.getObjectId()));
                defectType.setText(s.getDefectType());
                measured.setText(String.valueOf(s.getMeasuredValue()));
                allowed.setText(String.valueOf(s.getAllowedValue()));
                f2.setText(String.valueOf(s.getScaleScore()));
                f3.setText(String.valueOf(s.getSafetyScore()));
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addInspection(readInspection(objectId, defectType, measured, allowed, f2, f3));
            refreshInspections();
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(e -> safe(() -> {
            Inspection sel = selected(table);
            api.updateInspection(sel.getId(), readInspection(objectId, defectType, measured, allowed, f2, f3));
            refreshInspections();
        }));
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteInspection(selected(table).getId());
            refreshAll();
        }));
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshInspections));

        VBox form = form("Додати / Редагувати обстеження",
                "ID об’єкта", objectId, "Тип дефекту", defectType, "Виміряне значення", measured,
                "Норматив (допустиме значення)", allowed, "F2 — площа/масштаб (0–100)", f2,
                "F3 — вплив на безпеку (0–100)", f3, buttons(add, edit, delete, refresh));

        Button importCsv = new Button("Імпортувати CSV");
        progress.setPrefWidth(220);
        importCsv.setOnAction(e -> runImport(importCsv));
        HBox importBar = new HBox(10, importCsv, progress, status);
        importBar.setPadding(new Insets(8, 0, 0, 0));

        return tab("Обстеження", table, form, importBar);
    }

    private Tab defectsTab() {
        TableView<Defect> table = new TableView<>(defects);
        columns(table, "ID", "id", 50, "Обстеження", "inspectionId", 90, "Стан", "state", 160,
                "F1", "f1", 70, "F2", "f2", 70, "F3", "f3", 70, "I", "riskIndex", 80);

        TextField inspectionId = new TextField();
        ComboBox<DefectState> newState = new ComboBox<>(FXCollections.observableArrayList(DefectState.values()));
        newState.getSelectionModel().selectFirst();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                inspectionId.setText(String.valueOf(s.getInspectionId()));
            }
        });

        Button create = new Button("Створити");
        create.setOnAction(e -> safe(() -> {
            api.addDefect(number(inspectionId, "ID обстеження"));
            refreshDefects();
        }));
        Button changeState = new Button("Змінити стан");
        changeState.setOnAction(e -> safe(() -> {
            Defect sel = selected(table);
            DefectState to = newState.getValue();
            if (to == null) {
                throw new IllegalArgumentException("Виберіть новий стан");
            }
            api.changeDefectState(sel.getId(), to);
            refreshDefects();
        }));
        Button recalc = new Button("Перерах.");
        recalc.setOnAction(e -> safe(() -> {
            Defect sel = selected(table);
            api.updateDefect(sel.getId(), number(inspectionId, "ID обстеження"));
            refreshDefects();
        }));
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteDefect(selected(table).getId());
            refreshAll();
        }));
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshDefects));

        VBox form = form("Операції з дефектом",
                "ID обстеження", inspectionId, "Новий стан", newState,
                buttons(create, changeState), buttons(recalc, delete, refresh));
        return tab("Дефекти", table, form, null);
    }

    private Tab tasksTab() {
        TableView<RepairTask> table = new TableView<>(tasks);
        columns(table, "ID", "id", 50, "Дефект", "defectId", 70, "Опис", "description", 320,
                "Планова дата", "plannedDate", 120);

        TextField defectId = new TextField();
        TextField description = new TextField();
        TextField plannedDate = new TextField();
        plannedDate.setPromptText("РРРР-ММ-ДД");

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                defectId.setText(String.valueOf(s.getDefectId()));
                description.setText(s.getDescription());
                plannedDate.setText(s.getPlannedDate() == null ? "" : s.getPlannedDate());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(e -> safe(() -> {
            api.addTask(readTask(defectId, description, plannedDate));
            refreshTasks();
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(e -> safe(() -> {
            RepairTask sel = selected(table);
            api.updateTask(sel.getId(), readTask(defectId, description, plannedDate));
            refreshTasks();
        }));
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> safe(() -> {
            api.deleteTask(selected(table).getId());
            refreshTasks();
        }));
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> safe(this::refreshTasks));

        VBox form = form("Додати / Редагувати завдання",
                "ID дефекту", defectId, "Опис", description, "Планова дата", plannedDate,
                buttons(add, edit, delete, refresh));
        return tab("Ремонтні завдання", table, form, null);
    }

    // ------------------------------------------------------------------ CSV-імпорт

    private void runImport(Button importButton) {
        ImportInspectionsTask task = new ImportInspectionsTask(
                Path.of("sample-data", "inspections.csv"), api);
        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        importButton.setDisable(true);

        task.setOnSucceeded(ev -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            progress.setProgress(1);
            String text = "Готово. Імпортовано: " + task.getValue();
            if (task.getSkipped() > 0) {
                text += ", пропущено: " + task.getSkipped();
            }
            status.setText(text);
            importButton.setDisable(false);
            safe(this::refreshAll);
        });
        task.setOnFailed(ev -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            progress.setProgress(0);
            status.setText("Помилка імпорту");
            importButton.setDisable(false);
            Throwable t = task.getException();
            LOG.warning("Помилка імпорту: " + t);
            showError(t == null || t.getMessage() == null ? "Невідома помилка імпорту" : t.getMessage());
        });

        new Thread(task, "csv-import").start();
    }

    // ------------------------------------------------------------------ оновлення даних

    private void refreshObjects() {
        objects.setAll(api.getObjects());
    }

    private void refreshInspections() {
        inspections.setAll(api.getInspections());
    }

    private void refreshDefects() {
        defects.setAll(api.getDefects());
    }

    private void refreshTasks() {
        tasks.setAll(api.getTasks());
    }

    /** Потрібен, коли зміна однієї сутності може вплинути на відображення інших. */
    private void refreshAll() {
        refreshObjects();
        refreshInspections();
        refreshDefects();
        refreshTasks();
    }

    // ------------------------------------------------------------------ допоміжні методи

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            showError(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private static <T> T selected(TableView<T> table) {
        T item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            throw new IllegalStateException("Виберіть запис");
        }
        return item;
    }

    private static long number(TextField field, String name) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має містити ціле число");
        }
    }

    private static double decimal(TextField field, String name) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має містити число");
        }
    }

    private static Inspection readInspection(TextField objectId, TextField defectType, TextField measured,
                                             TextField allowed, TextField f2, TextField f3) {
        Inspection m = new Inspection();
        m.setObjectId(number(objectId, "ID об’єкта"));
        m.setDefectType(defectType.getText().trim());
        m.setMeasuredValue(decimal(measured, "Виміряне значення"));
        m.setAllowedValue(decimal(allowed, "Норматив"));
        m.setScaleScore(decimal(f2, "F2"));
        m.setSafetyScore(decimal(f3, "F3"));
        return m;
    }

    private static RepairTask readTask(TextField defectId, TextField description, TextField plannedDate) {
        RepairTask t = new RepairTask();
        t.setDefectId(number(defectId, "ID дефекту"));
        t.setDescription(description.getText().trim());
        t.setPlannedDate(plannedDate.getText().trim());
        return t;
    }

    /** Додає до таблиці колонки; параметри йдуть трійками: заголовок, властивість, ширина. */
    private static <S> void columns(TableView<S> table, Object... spec) {
        for (int i = 0; i < spec.length; i += 3) {
            TableColumn<S, Object> column = new TableColumn<>((String) spec[i]);
            column.setCellValueFactory(new PropertyValueFactory<>((String) spec[i + 1]));
            column.setPrefWidth(((Number) spec[i + 2]).doubleValue());
            table.getColumns().add(column);
        }
    }

    /** Форма: заголовок, потім пари «підпис, поле» та окремі вузли (наприклад, рядки кнопок). */
    private static VBox form(String title, Object... items) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(0, 0, 0, 14));
        box.setPrefWidth(300);
        Label heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        box.getChildren().add(heading);
        int i = 0;
        while (i < items.length) {
            if (items[i] instanceof String text) {
                box.getChildren().addAll(new Label(text), (Node) items[i + 1]);
                i += 2;
            } else {
                box.getChildren().add((Node) items[i]);
                i += 1;
            }
        }
        return box;
    }

    private static HBox buttons(Button... buttons) {
        HBox row = new HBox(6, buttons);
        row.setPadding(new Insets(6, 0, 0, 0));
        return row;
    }

    private static Tab tab(String title, TableView<?> table, VBox form, Node bottom) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        Label heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        VBox left = new VBox(6, heading, table);
        pane.setCenter(left);
        pane.setRight(form);
        if (bottom != null) {
            pane.setBottom(bottom);
        }
        Tab tab = new Tab(title, pane);
        tab.setClosable(false);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
