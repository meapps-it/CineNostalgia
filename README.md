# CineNostalgia

App Android nativa Kotlin + Jetpack Compose dedicata al cinema e alla nostalgia.

Package ID stabile: `com.meapps.cinenostalgia`.

## Funzioni V1

- Home, ricerca TMDB, risultati e scheda film
- cast con età all'uscita e età attuale/alla morte quando disponibile
- sezione Allora e oggi senza immagini inventate
- location curate con apertura nell'app mappe del telefono
- trama, curiosità, spoiler nascosti e provider italiani
- preferiti offline persistenti con Room
- tema riutilizzabile ME Apps, senza font incorporati

## Configurazione TMDB

La chiave non va inserita nel repository. In locale usare `TMDB_API_KEY` in
`~/.gradle/gradle.properties`; su Codemagic configurare la variabile ambiente
segreta `TMDB_API_KEY`.

Senza chiave l'app apre una modalità dimostrativa offline limitata a **Ritorno al futuro**.
I dati editoriali demo (location, curiosità e spoiler) sono separati dai dati API e
andranno collegati a fonti verificabili prima della pubblicazione commerciale.

## Build

```bash
./gradlew testDebugUnitTest assembleDebug bundleRelease
```

Questo prodotto usa l'API TMDB ma non è approvato o certificato da TMDB.

## Codemagic

Creare il gruppo `cinenostalgia_secrets` e aggiungere `TMDB_API_KEY` come variabile
protetta. Il workflow `android-v1` produce APK debug, AAB release e report dei test.
