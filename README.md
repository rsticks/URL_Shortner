# Url shortener service

Сокращатель ссылок
 USAGE:
 1. Склонировать ветку
 2. Запустить docker build
 3. Запустить скрипт run.sh для docker-compose
 4. Использовать API:
    1) для сокращения ссылки: http://host:port/api/v1/url
    в body передать длинную ссылку
    {
    "url": "https://example.com/"
    }

    2) перейти по ссылке из ответа
    
