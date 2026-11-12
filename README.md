# KupimyTo (Android, Kotlin + Jetpack Compose)

Aplikacja rodzinna: zadania, listy zakupów, kalendarz wydarzeń, powiadomienia.
Model danych 1:1 z `database_kupimyto.sql` (users, families, familymembers, tasks,
shoppinglists, shoppinglistitems, events, notifications).

## Uruchomienie
1. Otwórz folder w Android Studio (Ladybug lub nowsze), poczekaj na Gradle Sync.
2. Uruchom na emulatorze. Domyślnie działa na danych demo (`USE_FAKE_DATA = true`);
   zaloguj się jako `anna@example.com` + dowolne hasło.

## Podłączenie prawdziwej bazy
Aplikacja Android nie łączy się z MariaDB bezpośrednio (to niebezpieczne i niewykonalne
na produkcji) - potrzebny jest backend REST stojący przed bazą.

1. Zaimplementuj endpointy z `data/remote/KupimytoApi.kt` (JSON z nazwami kolumn z bazy,
   daty `yyyy-MM-dd HH:mm:ss`, `is_purchased`/`is_read` jako true/false, enumy tekstem:
   `owner|adult|child|member`, `todo|in_progress|done`, `low|medium|high`).
2. Logowanie: `POST auth/login` zwraca `{ "token": "...", "user": {...} }`;
   token jest dołączany jako `Authorization: Bearer ...`.
   Hasła hashuje backend do `users.password_hash` (np. bcrypt/argon2).
3. W `app/build.gradle.kts` ustaw `USE_FAKE_DATA = false` i `API_BASE_URL`.
4. Na produkcji używaj HTTPS i usuń `usesCleartextTraffic` z AndroidManifest.xml.

## Struktura
- `data/model` - klasy odpowiadające tabelom
- `data/repository` - interfejs `KupimytoRepository` + `FakeKupimytoRepository`
- `data/remote` - Retrofit (`KupimytoApi`) + `RemoteKupimytoRepository`
- `ui` - ekrany Compose i `AppViewModel`
