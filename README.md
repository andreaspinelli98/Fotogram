# Fotogram
Progetto realizzato per il corso di Mobile Computing (a.a. 2025/26).

Prototipo di client Android per un social network basato sulla condivisione di immagini. Sviluppato in Kotlin e Jetpack Compose, con Mapbox per la gestione delle mappe. Sviluppato con Android Studio.

<table>
  <tr>
    <td><img src="docs/screenshots/Bacheca.png" width="200"/></td>
    <td><img src="docs/screenshots/Mappa.png" width="200"/></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/CreaPost.png" width="200"/></td>
    <td><img src="docs/screenshots/Profilo.png" width="200"/></td>
  </tr>
</table>

## Feature principali
- Registrazione e login
- Bacheca con feed dei post degli utenti seguiti e non
- Creazione post (con immagine, testo e posizione opzionale)
- Dettagli amico (profilo, follow/unfollow, post pubblicati)
- Visualizzazione posizione dei post su mappa
- Gestione profilo utente

## Schema di navigazione
![Schema di navigazione](docs/diagrams/Navigazione.png)

## Documentazione
- [Specifica del progetto](docs/SpecificaProgetto.pdf)
- [Documentazione completa](docs/Documentazione.pdf)

## Setup
- Android Studio
- minSdk 36 / targetSdk 36
- Richiede una API key Mapbox da inserire in res/values/mapbox_access_token.xml
