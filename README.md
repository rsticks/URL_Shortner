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
    
