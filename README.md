# Attendance Server (Задание 3)

Spring Boot сервер для учета посещаемости.

## Что реализовано сейчас
- CRUD для **групп студентов** (`/api/groups`)
- CRUD для **студентов** (`/api/students`, список — по `groupId`)
- CRUD для **преподавателей** (`/api/teachers`, список — постранично)
- CRUD для **дисциплин** (`/api/subjects`)
- CRUD для **занятий** (`/api/lessons`) + посещаемость (`PUT /api/lessons/{id}/attendance`)
- Валидация входных данных
- Стандартизированные ответы:
  - успех: `{ "success": true, "data": ... }`
  - ошибка: `{ "success": false, "errorCode": ..., "errorMessage": ..., "details": [...] }`
- Flyway миграции `V1__init.sql`, `V2__teachers_subjects_lessons_attendance.sql`
- Логирование HTTP-запросов (access log) + корреляционный `requestId` (`X-Request-Id`)

## Переменные окружения (не хранить в git)
Секреты (пароли и т.д.) не должны коммититься.

1) Скопируйте шаблон:
- `copy .env.example .env`
2) Отредактируйте `.env` (например, `POSTGRES_PASSWORD`).

Основные переменные:
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `APP_PORT` (опционально)

Логирование:
- `LOG_LEVEL_ROOT` (по умолчанию `INFO`)
- `LOG_LEVEL_HTTP` (access log, по умолчанию `INFO`)

## Запуск через Docker Compose
В корне проекта есть `docker-compose.yml`.

### Windows (cmd)
```bat
copy .env.example .env
notepad .env

docker compose up --build
```

Приложение будет доступно на `http://localhost:8080`.

PostgreSQL поднимается в отдельном контейнере и сохраняет данные в volume.

## Примеры API
- `GET /api/groups`
- `POST /api/groups` `{ "name": "ИКБО-01-21" }`
- `GET /api/students?groupId=1`
- `POST /api/students` `{ "fullName": "Иванов Иван Иванович", "groupId": 1 }`
- `POST /api/teachers` `{ "fullName": "Петров Петр Петрович" }`
- `POST /api/subjects` `{ "name": "Математика" }`
- `POST /api/lessons` `{ "teacherId": 1, "subjectId": 1, "groupId": 1, "date": "2026-05-15", "pairNumber": 1 }`
- `PUT /api/lessons/1/attendance` `{ "presentStudentIds": [1,2] }`
