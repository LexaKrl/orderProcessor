# Order Processor Service

Сервис для обработки и управления заказами. Поддерживает создание заказов, пагинацию, фильтрацию по статусам и асинхронную обработку событий через RabbitMQ.

---

## Архитектура

В основе — Spring Boot 3 и событийная модель на RabbitMQ для работы со статусами заказов. Особое внимание уделил производительности: база данных не перегружается благодаря правильной пагинации и оптимизированным запросам (решил проблему N+1 и защитил память от OutOfMemory).
Весь жизненный цикл схемы БД управляется через Liquibase. За преобразование объектов отвечает MapStruct, где я аккуратно настроил сохранение связей. Система обработки исключений выстроена так, что любая ошибка (будь то валидация или системный сбой) превращается в четкий и понятный API-ответ.
---

## Старт

### 1. Запуск инфраструктуры (PostgreSQL & RabbitMQ)

Для работы приложения необходимы базы данных и брокер сообщений. Они поднимаются с помощью `docker-compose`.

Найдите файл `docker-compose.yml` в корне проекта со следующим содержимым:

```yaml
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: order-rabbit
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: [ "CMD", "rabbitmq-diagnostics", "check_port_connectivity" ]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres:
    image: postgres:16-alpine
    container_name: order-postgres
    environment:
      POSTGRES_DB: orders
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres -d orders" ]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

Можно запустить docker-compose.yaml одной командой в терминале:

```text
docker-compose up -d
```

А также само приложение командой:

```text
./gradlew bootRun
```

### 2. Curl запросы на эндпоинты сервиса

#### 1. Создание нового заказа

```text
curl -X 'POST' \
  'http://localhost:8080/api/orders' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "customerName": "Lexa Rt",
  "items": [
    {
      "productName": "Lada",
      "quantity": 2,
      "price": 999.99
    }
  ]
}'
```

#### 2. Получение заказов

```text
curl -X 'GET' \
  'http://localhost:8080/api/orders?status=PROCESSING&page=0&size=10' \
  -H 'accept: */*'
```

Параметры status и sort являются необязательными

#### 3. Получить заказ по UUID

```text
curl -X 'GET' \
  'http://localhost:8080/api/orders/98a1f2e8-6f27-4df1-9779-f6a28b87fe68?orderItemsPage=0&orderItemsSize=10' \
  -H 'accept: application/json'
```
Параметры orderItemsPage и orderItemsSize являются необязательными

#### 4. Изменить статус заказа

```text
curl -X 'PUT' \
  'http://localhost:8080/api/orders/98a1f2e8-6f27-4df1-9779-f6a28b87fe68/status' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "status": "CANCELED"
}'
```
