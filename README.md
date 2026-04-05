# z2a — Zapret2 for Android

DPI-обход для Android с премиальным UI и полной интеграцией движка [zapret2](https://github.com/bol-van/zapret2).

Android-версия проектов [z2k](https://github.com/necronicle/z2k) (Keenetic) и [z2w](https://github.com/necronicle/z2w) (Windows).

## Возможности

- **Автоциркулярная ротация стратегий** — per-domain персистентность, UCB-алгоритм выбора, cooldown
- **45+ TCP-стратегий** — fake, multisplit, hostfakesplit, fakeddisorder, IP fragmentation, TLS morphing
- **12+ QUIC-стратегий** — z2k_quic_morph_v2, z2k_timing_morph, z2k_ipfrag3
- **8 профилей** — RKN, YouTube TCP/QUIC, Google Video, Discord Voice/STUN, Custom
- **Lua-скриптовая система** — zapret-lib, zapret-antidpi, zapret-auto, z2k-autocircular, z2k-modern-core, locked
- **85+ fake-блобов** — TLS ClientHello, QUIC Initial, STUN, SYN
- **125K+ доменов** — актуальные списки RKN, YouTube, Discord
- **Silent Fallback** — обнаружение тихих блокировок
- **RST-фильтр** — блокировка поддельных RST от пассивного DPI
- **Режим Austerus** — обход для всего TCP/443

## UI

- Material Design 3
- Dark theme
- Анимированная кнопка подключения с пульсирующим свечением
- Профили с переключателями
- Реалтайм логи автоциркуляра с цветовой кодировкой
- Bottom Navigation: Главная | Профили | Логи | Настройки

## Как это работает

1. `VpnService` создаёт TUN-интерфейс — перехватывает весь трафик устройства
2. Трафик направляется через движок nfqws2
3. Движок применяет Lua-стратегии десинхронизации для обхода DPI
4. Автоциркуляр автоматически ротирует стратегии при обнаружении блокировки
5. Успешные стратегии запоминаются per-domain (state.tsv)

**z2a — НЕ VPN.** Трафик идёт напрямую к серверу, без промежуточных туннелей. Реальный IP не скрывается.

## Требования

- Android 7.0+ (API 24)
- Нативный бинарник nfqws2 (arm64-v8a / armeabi-v7a / x86_64) — добавляется отдельно в `app/src/main/jniLibs/`

## Сборка

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Релизы

Автоматическая сборка APK при создании тега:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Структура

```
app/src/main/
├── assets/
│   ├── lua/           — 6 Lua-скриптов (autocircular, modern-core, antidpi, etc.)
│   ├── files/         — 16+ fake-блобов (TLS, QUIC, STUN)
│   ├── hostlists/     — списки доменов (RKN, YouTube, Discord, whitelist)
│   └── profiles.default.txt — конфигурация профилей
├── java/com/z2a/
│   ├── engine/        — EngineManager, AutocircularState, StrategyConfig, ProfileConfig
│   ├── vpn/           — Z2aVpnService, VpnManager, BootReceiver
│   ├── data/          — ProfileRepository, SettingsRepository, models
│   └── ui/            — Jetpack Compose screens, theme, components
└── res/
```

## Лицензия

MIT

## Автор

[necronicle](https://github.com/necronicle)
