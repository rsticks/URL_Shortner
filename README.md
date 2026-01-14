# Url shortener service

Сокращатель ссылок
 USAGE:
 1. Склонировать ветку
 2. Запустить docker build
    Опционально: если деплоим в облоко - надо запушить готовый image
 4. Запустить скрипт run.sh для docker-compose
 5. Использовать API:
    1) для сокращения ссылки: http://host:port/api/v1/url
    в body передать длинную ссылку
    {
    "url": "https://example.com/"
    }

    2) перейти по ссылке из ответа
    
## Frontend (Vite + React)

В репозитории есть простой фронтенд в папке `frontend/`.

### Dev-режим

1. Поднимите бэкенд (например через `./run.sh`, он откроется на `http://localhost:8080`).
2. Запустите фронтенд:

```bash
cd frontend
npm install
npm run dev
```

Vite настроен так, что запросы на `/api/*` проксируются на `http://localhost:8080` (см. `frontend/vite.config.ts`).

### Переменные окружения

- `VITE_API_BASE`: базовый URL для API (по умолчанию пусто, используется относительный `/api/...`).

## Docker: локально (Apple Silicon) и сервер (amd64)

### Локально (M1/M2)

Теперь `Dockerfile` multi-arch, поэтому на Apple Silicon можно просто:

```bash
docker compose up -d --build
```

### Сервер (amd64)

На x86_64 сервере можно так же:

```bash
docker compose up -d --build
```

Если вы хотите **собрать amd64 образ на M1** и запушить в registry:

```bash
docker buildx build --platform linux/amd64 -t your-registry/url_shortener:tag --push .
```
