# Interaktívny čašník

Mobilná aplikácia pre reštauráciu s obsluhou pri stole. Hosť identifikuje stôl priložením telefónu k NFC štítku, objedná si jedlá a nápoje a zaplatí kartou cez platobnú bránu alebo u čašníka. Personál (čašník, kuchyňa, správca) spracúva objednávky v tej istej aplikácii.

Bakalárska práca *Interaktívny čašník pre stravovacie zariadenie ako mobilná aplikácia*, Univerzita Konštantína Filozofa v Nitre, Fakulta prírodných vied a informatiky, 2026. Autor: Viktor Hutta.

## Obsah repozitára

- `server/` – serverová časť (Kotlin, Spring Boot 4, PostgreSQL, REST API)
  - `src/main/resources/db/migration/` – databázové migrácie Flyway vrátane ukážkových dát
  - `src/test/` – integračné a jednotkové testy
  - `http/` – ukážkové požiadavky pre HTTP klienta IntelliJ IDEA
- `android/` – mobilná aplikácia (Kotlin, Jetpack Compose)
- `docker-compose.yml` – spustenie servera s databázou
- `.env.example` – vzor konfigurácie bez tajných údajov

## Spustenie servera

Potrebný je len [Docker Desktop](https://www.docker.com/products/docker-desktop/). Java ani iné nástroje sa inštalovať nemusia.

1. Skopírujte `.env.example` do súboru `.env` a doplňte hodnoty:
   - `POSTGRES_PASSWORD` – ľubovoľné heslo databázy,
   - `JWT_SECRET` – náhodný kľúč na podpisovanie tokenov (32 bajtov v Base64), vygenerujete ho v PowerShelli:
     ```powershell
     $b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
     ```
     alebo v Linuxe či macOS príkazom `openssl rand -base64 32`,
   - `FINBY_PROJECT_ID` a `FINBY_SECRET_KEY` – údaje testovacieho projektu platobnej brány [finby](https://doc.finby.eu/aapi/). Bez nich funguje všetko okrem platby kartou.
2. V koreňovom priečinku repozitára spustite:
   ```
   docker compose up -d --build
   ```
3. Overte, že server beží: http://localhost:8080/api/ping vráti `ok`.

Zastavenie: `docker compose down`. Dáta ostanú uložené. Príkaz `docker compose down -v` zmaže aj databázu a pri ďalšom spustení sa vytvorí znova s ukážkovými dátami.

## Ukážkové dáta

|   Rola  |Používateľ |     Heslo    |
|---------|-----------|--------------|
| čašník  | `casnik`  | `casnik2026` |
| kuchyňa | `kuchyna` | `casnik2026` |
| správca | `spravca` | `casnik2026` |

Stoly 1–3 majú tokeny `dev-token-stol-1` až `dev-token-stol-3`. Účty a tokeny slúžia len na vyskúšanie prototypu.

Platba kartou beží v testovacom prostredí brány, skutočné peniaze sa nestrhávajú. Úspešnú platbu simuluje karta `4200 0000 0000 0000`, ďalšie testovacie karty sú v [dokumentácii brány](https://doc.finby.eu/aapi/) v časti Test cards.

## Testy

Server má integračné a jednotkové testy. Spúšťajú sa v priečinku `server`:

```
./gradlew test
```

Testy potrebujú spustený Docker Desktop. Pri každom spustení si vytvoria vlastnú dočasnú databázu, takže dáta servera sa nezmenia. Platobnú bránu nahrádza simulácia, preto netreba internet ani údaje finby.