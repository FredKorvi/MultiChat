# MultiChat

Плагин для Spigot 1.12.2 (Java 8) с системой авторизации, гибкими чатами, гильдиями, заданиями и модерацией.

## Возможности
- Авторизация с безопасным хешированием паролей (BCrypt) и SQLite.
- Чаты: глобальный/локальный/гильдейский, форматирование через `chat.yml` и PlaceholderAPI.
- Гильдии: банк, ранги, приглашения, бонусы, правила, чат гильдии.
- Гильдейский MOTD (сообщение дня) и меню управления.
- Задания гильдии с прогрессом и наградами.
- Модерация: баны, муты, варны, фриз и т.д.
- Авто‑броадкасты.
- Переключаемые функции через `config.yml` (`settings.features.*`).

## Требования
- **Spigot/Paper 1.12.2**
- **Java 8**
- **Vault** (обязательно)
- **PlaceholderAPI** (опционально)
- **LuckPerms** (опционально, префиксы)
- **Citizens** (опционально)

## Сборка
```bash
mvn -q package
```
Готовый файл будет в `target/MultiChat.jar`.

> В текущей среде сборка может быть недоступна из‑за ограничений доступа к Maven Central.

## Установка
1. Скопируйте `MultiChat.jar` в папку `plugins/`.
2. Установите **Vault** и экономику.
3. Запустите сервер — конфиги будут созданы автоматически.
4. Настройте `config.yml`, `messages.yml`, `chat.yml`, `guilds.yml`, `quests.yml`.

## Команды (основные)
- `/register <pass> <pass>` — регистрация
- `/login <pass>` — вход
- `/g` — меню гильдии
- `/g create|invite|accept|kick|leave|info|top|bank|bonus|promote|demote|chat|mutechat|rules|motd|reliability|quest|takequest|refusequest`
- `/broadcast on|off|interval|list|add|remove|reload`
- `/rules` — правила сервера

## Права
- `multichat.*` — все права
- `multichat.auth.*` — авторизация
- `multichat.chat.*` — чат
- `multichat.guild.*` — гильдии
- `multichat.broadcast.*` — броадкасты
- `multichat.mod.*` — модерация

## PlaceholderAPI
Доступные плейсхолдеры:
- `%multichat_guild%`
- `%multichat_guild_rank%`
- `%multichat_guild_rank_id%`
- `%multichat_guild_level%`
- `%multichat_guild_points%`
- `%multichat_guild_currency%`
- `%multichat_prefix%`
- `%multichat_chat_channel%`

## Примечания
- Если надежность игрока в гильдии = 0, брать квесты нельзя.
- Надежность восстанавливается за 3 дня.
- КД между заданиями зависит от надежности: 100 → 2 часа, 50 → 5 часов.
- MOTD гильдии можно менять через `/g motd set <text>` (просмотр через `/g motd view`). 
- В меню гильдии есть пункт помощи со списком команд.
