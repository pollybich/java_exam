# Медиапортал

API для статей, видео и подкастов. Java 21, Spring Boot, PostgreSQL 17, Flyway, Redis.

## Запуск

Нужны Docker и Docker Compose v2. Порты 8080, 5432 и 6379 должны быть свободны.

```bash
docker compose up --build -d
```

- API: http://localhost:8080/api/articles
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs (копия — `docs/openapi.json`)

Первый запуск занимает несколько минут. Логи: `docker compose logs -f app`.
Остановка: `docker compose down`. Данные PostgreSQL сохраняются.
Удалить данные: `docker compose down -v`. Логин и пароль БД: `media`.

## API

Для `/api/articles`, `/api/videos`, `/api/podcasts`:

| Метод | Путь | Ответ |
| --- | --- | --- |
| POST | базовый путь | Создание, 201 |
| GET | базовый путь | Список, 200 |
| GET | базовый путь/{id} | Один объект, 200 |
| PUT | базовый путь/{id} | Замена, 200 |
| DELETE | базовый путь/{id} | Удаление, 204 |

Данные передаются в JSON. Нет объекта — 404, неверные поля — 400.
Формат ошибки: `timestamp`, `status`, `message`, `errors`.
Поля обязательны. Длительность — положительное целое число секунд, дата — `YYYY-MM-DD`, ссылки — HTTP/HTTPS.
Номера эпизодов положительные и не повторяются. PUT подкаста заменяет все эпизоды; пустой список разрешён. Удаление подкаста удаляет эпизоды.

## Redis

- `POST /api/popular/{type}/{id}/views` — добавить просмотр, ответ 204. Тип: `articles`, `videos` или `podcasts`.
- `GET /api/popular` — топ-10 по просмотрам: `type`, `id`, `views`.

Рейтинг хранится в Sorted Sets за текущий час по UTC, с началом нового часа отсчёт начинается заново. Старые ключи удаляются через 2 часа после последнего просмотра.
Просмотр добавляется отдельным POST, обычный GET его не считает. Удалённый материал убирается из рейтинга.
GET по ID кешируется на 10 минут через `@Cacheable`. PUT и DELETE очищают кеш.

## Демонстрация

1. Импортируйте `postman/MediaPortal.postman_collection.json` и выполните запросы по порядку. ID сохраняются автоматически.
2. В каждой папке: создайте материал, получите и измените его, добавьте просмотр, проверьте топ, удалите. Повторный GET вернёт 404.
3. Через Swagger отправьте пустой заголовок или два эпизода с одинаковым номером — получите 400.

## Тесты

Нужны JDK 21, Maven 3.9+ и работающий Docker 25+.
`mvn test` запускает модульные тесты сервисов и интеграционные тесты с отдельными PostgreSQL и Redis через Testcontainers.
Проверяются CRUD, ошибки, эпизоды, кеш, рейтинг и Swagger.
Сборка: `mvn clean package`.

Для запуска без контейнера приложения: `docker compose up -d db redis`, затем `mvn spring-boot:run`.
Настройки подключения: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`.
Таблицы создаёт Flyway, Hibernate работает с `ddl-auto: validate`.
