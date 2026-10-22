
# Contributing to TerraCraftCore

Спасибо, что хотите помочь! Ниже — правила, чтобы всё прошло гладко.

---

## 📋 Требования

- **JDK 25+** ([Adoptium](https://adoptium.net/)).
- **Gradle 9+** (или `./gradlew`).
- **Git**.
- **IDE**: IntelliJ IDEA (рекомендуется), Eclipse, VS Code — на ваш вкус.

---

## 🔧 Настройка окружения

```bash
# 1. Форкните репозиторий на GitHub
#    https://github.com/luci4ka/TerraCraftCore/fork

# 2. Клонируйте свой форк
git clone https://github.com/<ваш-ник>/TerraCraftCore.git
cd TerraCraftCore

# 3. Добавьте upstream (оригинал)
git remote add upstream https://github.com/luci4ka/TerraCraftCore.git

# 4. Проверьте сборку
./gradlew clean build -x test

# 5. (Опционально) проверьте, что JitPack-конфиг на месте
cat jitpack.yml   # должно быть: jdk: [openjdk25]
```

> **Требуется `gradlew` в репозитории.** Если его нет — `gradle wrapper --gradle-version 9.3.0`.

---

## 🌿 Ветвление

| Ветка | Назначение |
|-------|-----------|
| `main` | Стабильные релизы. **Не пушить напрямую.** |
| `dev`  | Текущая разработка. PR — сюда. |
| `feature/<name>` | Новая фича |
| `fix/<name>` | Багфикс |
| `docs/<name>` | Документация |
| `api/<name>` | Изменения публичного API (`ru.terracraft.api.*`) |

**Правила:**
- Один PR — одна задача.
- Ветку создавайте от `dev`, не от `main`.
- Название — `feature/teleport-command`, `fix/vanish-npe`, `docs/readme-update`.
- **Изменения в `ru.terracraft.api.*`** → отдельный PR с меткой `api` + обновление `CHANGELOG` (breaking changes!).

---

## ✍️ Стиль кода

### Java

- **Java 25** — используйте `record`, `switch`-выражения, `text blocks`.
- **Отступы:** 4 пробела.
- **Строки:** до 120 символов.
- **Именование:**
    - `PascalCase` — классы.
    - `camelCase` — методы, поля.
    - `UPPER_SNAKE_CASE` — константы.
    - Пакеты — `ru.terracraft.core.*` (внутренние) / `ru.terracraft.api.*` (публичные).
- **Javadoc** — для публичных классов и методов. Кратко, по делу.
- **`@NotNull` / `@Nullable`** — обязательно для параметров и возвращаемых значений (JetBrains annotations).
- **`final`** — для полей, которые не меняются.
- **Никаких `System.out.println`** — только `plugin.getLogger()`.

### Пример

```java
/**
 * Поджигает игрока на {@code seconds} секунд.
 *
 * @param target  цель (не {@code null})
 * @param seconds длительность (1..60)
 * @throws IllegalArgumentException если {@code seconds} вне диапазона
 */
public void burn(@NotNull Player target, int seconds) {
    if (seconds < 1 || seconds > 60) {
        throw new IllegalArgumentException("seconds must be 1..60");
    }
    target.setFireTicks(seconds * 20);
}
```

### YAML

- **Отступы:** 2 пробела.
- **Ключи:** `kebab-case` (`max-smooth-seconds`, не `maxSmoothSeconds`).
- **Комментарии** — на русском, кратко.
- **Порядок** — как в `AdminToolsConfig`.

### `.lang` / `.yml`

- **Плоский формат** — каждая строка `"key": "value"`.
- **Ключи с точками** — **обязательно в кавычках**.
- **MiniMessage** — для цветов и форматирования.
- **Никаких `&`-кодов** — только `<red>`, `<gradient:...>` и т.д.

```yaml
"god.enabled.self": "<green>... <white>Godmode <green>включён</green>."
```

---

## 🔌 Публичный API (`ru.terracraft.api.*`)

Изменения в пакете `ru.terracraft.api.*` **критичны** — их используют другие плагины.

### Правила

1. **Не удаляйте** существующие методы/поля. Только `@Deprecated` + пометка в Javadoc.
2. **Не меняйте** сигнатуры (`return type`, `parameter types`).
3. **Не меняйте** имена пакетов, классов, методов.
4. **Новые методы** — только `default` в интерфейсах (для обратной совместимости).
5. **Breaking changes** — только в мажорной версии (`2.0.0`), с записью в `CHANGELOG`.

### Что считается API

| Пакет | Публичный? |
|-------|-----------|
| `ru.terracraft.api.TerraCoreAPI` | ✅ |
| `ru.terracraft.api.TerraCoreAdminAPI` | ✅ |
| `ru.terracraft.api.dto.*` | ✅ |
| `ru.terracraft.api.event.*` | ✅ |
| `ru.terracraft.core.*` | ❌ (внутренние) |

### JitPack

Публичный API отдаётся через JitPack как **тонкий** JAR (только API + `core.*`, без `bstats`/`configlib`). Подключение в другом плагине:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.luci4ka:TerraCraftCore:1.0.2'
}
```

И **обязательно** в `paper-plugin.yml`:

```yaml
dependencies:
  server:
    TerraCraftCore:
      load: BEFORE
      required: true
      join-classpath: true   # ← критично!
```

> Без `join-classpath: true` классы `TerraCoreAPI` не будут видны в рантайме — получите `NoClassDefFoundError`.

---

## 🧪 Тесты

### Запуск

```bash
# Все тесты
./gradlew test

# Один класс
./gradlew test --tests "ru.terracraft.core.autotest.LangManagerTest"

# С подробным выводом
./gradlew test --console=plain
```

### Что тестировать

- **Новая команда** → тест в `src/test/java/ru/terracraft/core/autotest/<Name>Test.java`.
- **Новый менеджер** → тест на `MockBukkit`.
- **Новый `LangKey`** → `LangManagerTest` автоматически проверит, что ключ есть в `.lang`.
- **Новый метод API** → тест в `TerraCoreAPITest` (если есть).

### Стиль тестов

```java
class MyManagerTest {

    private ServerMock server;
    private TerraCorePlugin plugin;
    private MyManager manager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TerraCorePlugin.class);
        manager = plugin.myManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void doesSomething_whenCondition() {
        // given
        ...
        // when
        ...
        // then
        assertEquals(expected, actual);
    }
}
```

---

## 📝 Коммиты

Используем **Conventional Commits**:

| Префикс | Когда |
|---------|-------|
| `feat:` | Новая фича |
| `fix:` | Багфикс |
| `docs:` | Документация |
| `style:` | Форматирование, пробелы (не код) |
| `refactor:` | Рефакторинг без изменения поведения |
| `test:` | Тесты |
| `chore:` | Обновление зависимостей, конфигов |
| `perf:` | Оптимизация |

**Формат:**
```
<type>(<scope>): <краткое описание>

<подробное описание (опционально)>

<footer (опционально)>
```

**Примеры:**
```
feat(commands): add /compass with BossBar
fix(vanish): restore vanish only if player has permission
docs(readme): update PAPI list
chore(deps): bump configlib to 4.1.0
```

**Правила:**
- Заголовок — до 72 символов.
- Начинается с маленькой буквы.
- Без точки в конце.
- На русском или английском — **единообразно** в рамках PR.

---

## 🔀 Pull Request

1. **Создайте ветку** от `dev`:
   ```bash
   git checkout dev
   git pull upstream dev
   git checkout -b feature/my-feature
   ```

2. **Пишите код**, коммитьте.

3. **Проверьте сборку**:
   ```bash
   ./gradlew clean test build
   ```

4. **Обновите `CHANGELOG.md`** (секция `Unreleased`).

5. **Проверьте, что JAR называется правильно**:
   ```bash
   ls build/libs/
   # TerraCraftCore-1.0.2.jar
   # TerraCraftCore-1.0.2-thin.jar
   ```

6. **Запушьте**:
   ```bash
   git push origin feature/my-feature
   ```

7. **Откройте PR** на GitHub → `dev`.

### Что должно быть в PR

- **Заголовок** — как у коммита (`feat: ...`).
- **Описание** — что, зачем, как тестировать.
- **Скриншоты** — если UI/чат.
- **Чек-лист**:
    - [ ] Собрано (`./gradlew build`)
    - [ ] Тесты проходят (`./gradlew test`)
    - [ ] `CHANGELOG.md` обновлён
    - [ ] `README.md` обновлён (если нужно)
    - [ ] Локализация: добавлены ключи в `en_us.yml` и `ru_ru.yml`
    - [ ] Если затронут API — метка `api` + запись в `CHANGELOG` (breaking changes)
    - [ ] JAR называется `TerraCraftCore-<version>.jar`

---

## 🐛 Сообщение о баге

Используйте [GitHub Issues](https://github.com/luci4ka/TerraCraftCore/issues). Включите:

1. **Версию TerraCraftCore** (`/tc info`).
2. **Версию Paper** и **Java**.
3. **Список плагинов** (если конфликт).
4. **Шаги воспроизведения**:
    1. ...
    2. ...
5. **Ожидаемое поведение**.
6. **Фактическое поведение**.
7. **Лог** — `/logs/latest.log` или скриншот консоли.
8. **Скриншот** (если UI/чат).

---

## 💡 Предложение фичи

1. Проверьте [Issues](https://github.com/luci4ka/TerraCraftCore/issues) — может, уже предложено.
2. Создайте Issue с меткой `enhancement`.
3. Опишите:
    - **Что** хотите.
    - **Зачем** (use case).
    - **Как** (если есть идеи).

---

## 📜 Лицензия

Отправляя PR, вы соглашаетесь, что ваш код будет распространяться под **MIT License** (см. [LICENSE](LICENSE)).

---

## 🙏 Спасибо!

Любой вклад ценен: код, документация, тесты, сообщения о багах, идеи.
