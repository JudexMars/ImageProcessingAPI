# ImageProcessingAPI

## Что это такое

Это микросервисное приложение, позволяющее загружать и редактировать фотографии. Оно предоставляет несколько фильтров, а также возможность обрезать фото и удалять фон с портрета человека.

## Как этим пользоваться

Для запуска приложения достаточно ввести `docker-compose up` в корневой директории при запущенном Docker (предупреждаю, оно требует довольно много ресурсов, ПК может подтормаживать). 
После этого можно перейти на страницу со сваггером `localhost:8080/api/v1/swagger-ui/index.html`, где описаны все возможные ручки. Начать стоит с регистрации и авторизации (с помощью JWT-токена).
Также можно посмотреть API приложения в файле `src/main/resources/openapi.yaml`.

## Какие есть возможности

1. Инверсия цветов
2. Изменение яркости
3. Обрезание фотографии
4. Удаление фона

Посмотреть примеры работы приложения и подробную инструкцию можно в пулл-реквесте: https://github.com/JudexMars/ImageProcessingAPI/pull/6

## Техническая реализация

- API (модуль image-crud) написан на **Java**.
- Обработчики изображений написаны на **Kotlin** (image-processor) и **Python** (cv-image-processor). Язык **Python** использовался для одного обработчика, поскольку на нем есть уже написанная библиотека для удаления фона с фото.
- Сервисы интегрированы друг с другом асинхронно посредством кластера Kafka в режиме работы KRAFT.
- Имеется возможность развертывания в кластере Kubernetes https://github.com/JudexMars/ImageProcessingAPI/pull/9

![image](https://github.com/user-attachments/assets/ad922ce3-cc73-44e6-9f1b-1cd177607314)