# Інформаційна система обліку дефектів будівельних об'єктів

Курсова робота з дисципліни «Кросплатформене програмування», варіант 16.
Система дозволяє вести облік будівельних об'єктів, результатів їх обстежень, виявлених дефектів і ремонтних завдань.
Для дефекту автоматично обчислюється інтегральний показник I = 0,40·F1 + 0,35·F2 + 0,25·F3
(F1 — тяжкість дефекту, F2 — площа/масштаб, F3 — вплив на безпеку експлуатації) і ведеться чотиристанний життєвий цикл:
ЗАФІКСОВАНО → ПЕРЕВІРЕНО → РЕМОНТ_ПРИЗНАЧЕНО → ЗАКРИТО.

## Технології
Java 21 · Maven · Servlet API 4.0.1 (Apache Tomcat 9.x, `javax.servlet`) · JDBC / MySQL Connector/J 9.7.0 ·
Jackson 2.18.2 · JavaFX 21.0.2 · JUnit 5.11.4 · MySQL (XAMPP)

## Структура
```
kpp-coursework-mandrehela-variant16/
├─ database.sql                  створення бази construction_defects і демонстраційні дані
├─ server/                       серверна частина (WAR): Servlet → Service → DAO → JDBC → MySQL
│  └─ src/test/...               10 модульних тестів JUnit 5
└─ client/                       JavaFX-клієнт, HTTP-клієнт, фоновий CSV-імпорт
   └─ sample-data/inspections.csv
```

## Підготовка MySQL
1. Запустіть у XAMPP Control Panel модулі Apache та MySQL.
2. Відкрийте http://localhost/phpmyadmin → Імпорт → виберіть `database.sql` → Вперед.
3. Має з'явитися база `construction_defects` з таблицями `building_object`, `inspection`, `defect`, `repair_task`.
4. Параметри підключення (URL, користувач, пароль) задані в `server/src/main/java/ua/edu/nupp/kpp/server/DBUtil.java`.
   Не зберігайте у репозиторії справжній пароль.

## Сервер
```
cd server
mvn clean test
mvn clean package
```
Скопіюйте `server/target/construction-defects-server.war` у `webapps` Apache Tomcat 9.x і запустіть `bin/startup.bat`.
Перевірка: http://localhost:8080/construction-defects-server/objects — має повернути JSON.

Маршрути (GET — читання/видалення, POST — створення/редагування, параметри у `application/x-www-form-urlencoded`):

| URL | Призначення |
|---|---|
| `/objects` | CRUD будівельних об'єктів (`code`, `name`, `address`) |
| `/inspections` | CRUD обстежень (`objectId`, `defectType`, `measuredValue`, `allowedValue`, `scaleScore`, `safetyScore`) |
| `/defects` | CRUD дефектів (`inspectionId`), `?action=transition&id=..&state=..` |
| `/tasks` | CRUD ремонтних завдань (`defectId`, `description`, `plannedDate`) |

Видалення: `GET ...?action=delete&id=..`. У разі помилки сервер повертає HTTP 400 і JSON `{"error": "..."}`.

## Клієнт
```
cd client
mvn javafx:run
```
Запускайте з папки `client` (CSV читається за відносним шляхом `sample-data/inspections.csv`). Спочатку мають бути запущені MySQL і Tomcat.

## Тести JUnit 5
```
cd server
mvn clean test
```
Тести не потребують JavaFX, Tomcat чи MySQL.

## CSV-імпорт
Файл `client/sample-data/inspections.csv`, кодування UTF-8, роздільник `;`, перший рядок — заголовок:
```
object_id;defect_type;measured_value;allowed_value;scale_score;safety_score
1;Відхилення від вертикалі;70.0;40.0;45;65
2;Вологість стіни;18.5;20.0;20;35
```
Імпорт виконується у фоні (JavaFX Task) кнопкою «Імпортувати CSV» на вкладці «Обстеження» з ProgressBar і статусом.
Некоректні рядки (неправильна кількість полів, число, неіснуючий об'єкт) пропускаються й записуються в журнал.
Рядок, який уже є в базі, не додається повторно (дублікати пропускаються). Неправильний заголовок, відсутній файл
або недоступний сервер зупиняють імпорт із повідомленням про помилку.

## Контрольний сценарій
1. Імпорт CSV: статус «Готово. Імпортовано: 2», повторний запуск — «Імпортовано: 0, пропущено: 2».
2. Вкладка «Дефекти» → ID обстеження `1` → «Створити»: F1 = 52, F2 = 60, F3 = 70, I = 59,3.
3. «Змінити стан» послідовно до ЗАКРИТО; пропуск стану (наприклад, ЗАФІКСОВАНО → ЗАКРИТО) дає помилку.
4. Вкладка «Ремонтні завдання»: додайте завдання для дефекту (дата РРРР-ММ-ДД).
