# NBank API Testing Framework

Автоматизированный фреймворк для тестирования API банковской системы, построенный с использованием Java 21, REST Assured и JUnit 5.

## 🏗️ Архитектура проекта

Проект разделен на три уровня сложности:

### 📁 Структура проекта

```
src/
├── main/java/
│   ├── senior/          # Продвинутый уровень
│   │   ├── configs/     # Конфигурации
│   │   ├── generators/  # Генераторы данных
│   │   ├── models/      # Модели данных
│   │   ├── requests/    # HTTP запросы
│   │   └── specs/       # Спецификации
│   └── middle/          # Средний уровень
│       ├── models/      # Модели данных
│       ├── requests/    # HTTP запросы
│       ├── generators/  # Генераторы данных
│       └── specs/       # Спецификации
└── test/java/
    ├── iteration1_junior_level/   # Тесты начального уровня
    ├── iteration1_middle_level/   # Тесты среднего уровня
    ├── iteration1_senior_level/   # Тесты продвинутого уровня
    ├── iteration2_junior_level/   # Тесты начального уровня (итерация 2)
    ├── iteration2_middle_level/   # Тесты среднего уровня (итерация 2)
    └── iteration2_senior_level/   # Тесты продвинутого уровня (итерация 2)
```

## 🚀 Быстрый старт

### Предварительные требования

- Java 21+
- Maven 3.6+
- Запущенный API сервер на `http://localhost:4111`

### Установка и запуск

1. **Клонирование репозитория**

```bash
git clone <repository-url>
cd nbank
```

2. **Запуск тестов**

```bash
# Запуск всех тестов
mvn test

# Запуск тестов конкретного уровня
mvn test -Dtest="iteration1_senior_level.*"

# Запуск конкретного теста
mvn test -Dtest="CreateUserTest"
```

## 🛠️ Технологический стек

- **Java 21** - основной язык программирования
- **Maven** - система сборки
- **REST Assured 5.5.1** - библиотека для API тестирования
- **JUnit 5** - фреймворк для тестирования
- **Lombok** - уменьшение boilerplate кода
- **Jackson** - сериализация/десериализация JSON
- **AssertJ** - fluent assertions
- **rgxgen** - генерация данных по regex

## 📊 Возможности фреймворка

### Senior Level

- ✅ Автоматическая генерация тестовых данных по regex
- ✅ Валидация моделей через аннотации
- ✅ Сравнение request/response моделей
- ✅ CRUD операции через универсальный интерфейс
- ✅ Кастомное логирование запросов
- ✅ Конфигурация через properties файлы

### Middle Level

- ✅ Базовые HTTP операции
- ✅ Генерация случайных данных
- ✅ Авторизация пользователей

### Junior Level

- ✅ Простые API тесты
- ✅ Базовые assertions

## 🔧 Конфигурация

Основные настройки находятся в `src/main/resources/config.properties`:

```properties
url=http://localhost:4111
apiVersion=/api/v1
```

## 📝 Примеры использования

### Создание пользователя (Senior Level)

```java
@Test
void adminCanCreateUserWithValidCredentials() {
    CreateUserRequestModel request = RandomModelGenerator.generateRandomModel(CreateUserRequestModel.class);

    CreateUserResponseModel response = new ValidatedCrudRequester<CreateUserResponseModel>(
        RequestSpecs.adminSpec(),
        ResponseSpecs.responseReturns201Spec(),
        Endpoint.ADMIN_USERS
    ).post(request);

    ModelAssertions.assertThatModels(request, response).match();
}
```

### Параметризованный тест

```java
@ParameterizedTest
@MethodSource("argsFor_userCanDepositMoney")
void userCanDepositMoney(Float depositBalance) {
    // Тестовая логика
}
```

## 🧪 Типы тестов

1. **Позитивные тесты** - проверка корректной работы API
2. **Негативные тесты** - проверка обработки ошибок
3. **Параметризованные тесты** - тестирование с различными данными
4. **Интеграционные тесты** - тестирование полных сценариев

## 📈 Метрики качества

- Покрытие тестами: все основные API endpoints
- Поддержка различных уровней авторизации
- Валидация бизнес-правил
- Проверка граничных значений

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект предназначен для образовательных целей.

## 📞 Поддержка

При возникновении вопросов создайте Issue в репозитории.
