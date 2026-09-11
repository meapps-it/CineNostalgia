# CineNostalgia

App Android nativa Kotlin + Jetpack Compose dedicata al cinema e alla nostalgia.

Package ID stabile: `com.meapps.cinenostalgia`.

## Funzioni V1

- Home, ricerca TMDB per titolo o attore, risultati e scheda film
- cast con età all'uscita e età attuale/alla morte quando disponibile
- sezione Allora e oggi senza immagini inventate
- location curate con apertura nell'app mappe del telefono
- trama, curiosità, spoiler nascosti e provider italiani
- preferiti offline persistenti con Room
- tema riutilizzabile ME Apps, senza font incorporati
- dimensione del testo regolabile e persistente, applicata sopra le impostazioni di sistema
- cataloghi Anni 70, 80, 90 e 2000 con caricamento progressivo da TMDB
- navigazione Indietro coerente con il tasto di sistema Android
- arricchimento automatico da Wikipedia per trama estesa, spoiler e produzione
- location con coordinate da Wikidata e apertura nell'app mappe
- schede attore con biografia Wikipedia, dati anagrafici e filmografia
- ricerca e schede dedicate anche alle serie TV

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

Aggiungere `TMDB_API_KEY` come variabile protetta nell'editor del workflow per
attivare il catalogo online. La build funziona anche senza la variabile, usando la
modalità demo. Il workflow `android-v1` produce APK debug, AAB release e report dei test.
