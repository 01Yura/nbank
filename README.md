# NBank API & UI Testing Framework

Автоматизированный фреймворк для тестирования API и UI банковской системы, построенный на Java 21, REST Assured, Selenide и JUnit 5. Поддерживает многопоточное тестирование и расширяемую архитектуру.

## 🏗️ Архитектура проекта

Проект разделен на три уровня сложности (junior, middle, senior) и поддерживает как API, так и UI тесты, включая многопоточные сценарии.

### 📁 Структура проекта

```
src/
├── main/java/
│   ├── api/
│   │   ├── middle/         # Средний уровень API
│   │   │   ├── models/     # Модели данных
│   │   │   ├── requests/   # HTTP запросы
│   │   │   ├── generators/ # Генераторы данных
│   │   │   └── specs/      # Спецификации
│   │   └── senior/         # Продвинутый уровень API
│   │       ├── configs/    # Конфигурации
│   │       ├── generators/ # Генераторы данных
│   │       ├── models/     # Модели данных и сравнение
│   │       ├── requests/   # HTTP запросы, CRUD-интерфейсы
│   │       └── specs/      # Спецификации
│   ├── common/             # Общие аннотации, расширения, storage
│   │   ├── annotations/    # Аннотации для тестов и сессий
│   │   ├── extensions/     # JUnit5-расширения
│   │   └── storage/        # Хранилище сессий
│   └── ui/
│       ├── middle/         # UI-страницы среднего уровня
│       │   └── pages/      # Страницы: Login, Dashboard, Transfer и др.
│       └── senior/         # UI-страницы и элементы senior-уровня
│           ├── elements/   # Базовые элементы UI
│           └── pages/      # Страницы: AdminPanel, UserDashboard и др.
└── test/java/
    ├── api/
    │   ├── iteration1_junior_level/   # Тесты начального уровня (итерация 1)
    │   ├── iteration1_middle_level/   # Тесты среднего уровня (итерация 1)
    │   ├── iteration1_senior_level/   # Тесты продвинутого уровня (итерация 1)
    │   ├── iteration2_junior_level/   # Тесты начального уровня (итерация 2)
    │   ├── iteration2_middle_level/   # Тесты среднего уровня (итерация 2)
    │   └── iteration2_senior_level/   # Тесты продвинутого уровня (итерация 2)
    ├── ui/
    │   ├── iteration1_junior_level/   # UI тесты начального уровня
    │   ├── iteration1_middle_level/   # UI тесты среднего уровня
    │   ├── iteration1_senior_level/   # UI тесты продвинутого уровня
    │   ├── iteration2_junior_level/   # UI тесты начального уровня (итерация 2)
    │   ├── iteration2_middle_level/   # UI тесты среднего уровня (итерация 2)
    │   └── iteration2_senior_level/   # UI тесты продвинутого уровня (итерация 2)
    └── multithreading/
        ├── api/                       # Многопоточные API тесты
        └── ui/                        # Многопоточные UI тесты
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

# Запуск многопоточных тестов
mvn test -Dtest="multithreading.api.*"

# Запуск конкретного теста
mvn test -Dtest="CreateUserTest"
```

## 🛠️ Технологический стек

- **Java 21** — основной язык программирования
- **Maven** — система сборки
- **REST Assured 5.5.1** — API тестирование
- **JUnit 5** — фреймворк для тестирования
- **Selenide 7.9.3** — UI тестирование
- **Lombok** — уменьшение boilerplate кода
- **Jackson** — сериализация/десериализация JSON
- **AssertJ** — fluent assertions
- **rgxgen** — генерация данных по regex
- **SLF4J** — логирование

## 📊 Возможности фреймворка

### Senior Level

- ✅ Автоматическая генерация тестовых данных по regex
- ✅ Валидация моделей через аннотации
- ✅ Сравнение request/response моделей
- ✅ CRUD операции через универсальный интерфейс
- ✅ Кастомное логирование запросов
- ✅ Конфигурация через properties файлы
- ✅ Расширяемые UI-страницы и элементы

### Middle Level

- ✅ Базовые HTTP операции
- ✅ Генерация случайных данных
- ✅ Авторизация пользователей
- ✅ UI тесты пользовательских сценариев

### Junior Level

- ✅ Простые API и UI тесты
- ✅ Базовые assertions

### Многопоточные тесты

- ✅ Поддержка параллельного запуска API и UI тестов
- ✅ Проверка корректности работы при одновременных операциях (например, массовые переводы, депозиты, обновления профиля)
- ✅ Базовые сценарии гонок и проверки целостности данных

## 🔧 Конфигурация

Основные настройки находятся в `src/main/resources/config.properties`:

```properties
url=http://localhost:4111
apiVersion=/api/v1
```

## 📝 Примеры использования

### Многопоточный API тест (пример)

```java
@RepeatedTest(10)
void concurrentTransfers() {
    // Запуск нескольких потоков для одновременного перевода средств
    IntStream.range(0, 5).parallel().forEach(i -> {
        // Логика перевода
    });
}
```

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

1. **Позитивные тесты** — проверка корректной работы API и UI
2. **Негативные тесты** — проверка обработки ошибок
3. **Параметризованные тесты** — тестирование с различными данными
4. **Интеграционные тесты** — тестирование полных сценариев
5. **Многопоточные тесты** — проверка работы под нагрузкой и в условиях гонок

## 📈 Метрики качества

- Покрытие тестами: все основные API и UI endpoints
- Поддержка различных уровней авторизации
- Валидация бизнес-правил
- Проверка граничных значений
- Проверка устойчивости к параллельным операциям

## 🤝 Контрибуция

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект предназначен для образовательных целей.

## 📞 Поддержка

При возникновении вопросов создайте Issue в репозитории.
