Веб-приложение для генерации и управления паролями с тегами. Использует PostgreSQL, BCrypt, Spring Security, кэширование и CRUD.

# Требования

- **Java 17**
- **Maven**
- **PostgreSQL** (база: `passworddb`)
- **Переменная окружения**: `DB_PASSWORD`

# API Endpoints

## 1. Генерация паролей
- **GET** `http://localhost:8080/api/passwords/generate?length=X&complexity=Y&owner=Z`
    - Параметры: `length` (4–30), `complexity` (1-3), `owner` (опционально)
    - Пример: `http://localhost:8080/api/passwords/generate?length=12&complexity=3&owner=testUser`
- **POST** `http://localhost:8080/api/passwords/generate-bulk`
    - Тело: `[{"length": X, "complexity": Y, "owner": "Z"}, ...]`
    - Пример: `[{"length": 8, "complexity": 2, "owner": "user1"}]`

## 2. Поиск по тегу
- **GET** `http://localhost:8080/api/passwords/by-tag?tagName={tagName}`
    - Пример: `http://localhost:8080/api/passwords/by-tag?tagName=work`

## 3. CRUD
- Пароли: `POST|GET|PUT|DELETE /api/passwords`
- Теги: `POST|GET|PUT|DELETE /api/tags`

## 4. Счетчик 
- GET `http://localhost:8080/api/passwords/count` — Получение количества вызовов генерации паролей

Особенности
- Кэш: In-memory Map для `generatePassword`, `findAll`, `findById`, `findPasswordsByTagName`
- Хранилище: таблицы `passwords`, `tags`, связь `password_tag` (many-to-many)
- Тесты: >80% покрытие бизнес-логики
- Безопасность: Spring Security с открытым доступом к `/api/**`, `/swagger-ui/**`