<p align="center">
  <img src="https://inf-git.th-rosenheim.de/sep-wif-22/datavis/-/wikis/resources/datavis_logo.png" />
</p>

Datavis ist ein Projekt im Rahmen der **Software Engineering Praxis (WIF)** Veranstaltung in Zusammenarbeit mit **Ericsson**, mit dem Ziel Antennenmodelle zusammen mit ihrere Feldstärke in einer AR-Applikation darzustellen.

## Getting Started
### Verzeichnisstruktur
    .
    ├── app                          # Andorid Studio Application
    │   ├── src
    │   |   ├── main
    |   |   ├── test
    │   |   └── androidTest
    |   └── miscellaneous scripts
    ├── Data                         # Data provide by Ericsson
    ├── miscellaneous scripts
    ├── .gitignore
    └── README.md                    # You are here

### Git Workflow
- Primär Branches: **prod**, **dev**
- Push auf Primär Branches ist nicht möglich
- Hierarchie: **prod**, **dev**, **feature/bug/fix branches**
  - ALLE neuen branches müssen auf **dev** basieren und durch Merge Request in **dev** gemerged werden.
  - In **prod** wird nur von **dev** gemerged
- Merge Requests
  - Merge in **dev**: 2 Augen Prinzip (nicht Autor selbst)
  - Merge in **prod**: 4 Augen Prinzip (nicht Autor selbst)
  - Fast forward Merge

### Installation

Vorbedinung: 
- Android Studio Vesion 3.1 oder höher mit Android SDK Platform Version 7.0 (API-Level 24) oder höher.
- Smartohone/Tablet mit Anddroid 7 (API-Level 24) oder höher.  
- AR Core fähiges Smartohone/Tablet. Link zur Liste der AR unterstützenden Geräte  https://developers.google.com/ar/devices

Erste Installation: 
- Android Studio Installieren. 
- datavis Projekt Clonen: Neu -> Neues Projekt von VCS -> von Version control: Git 
- Clone Link ist auffindbar unter https://inf-git.th-rosenheim.de/sep-wif-22/datavis 
- datavis Projekt in Android Studio öffnen. 
- Datei -> Synchronisiere Gradle Datein im Projekt (Eng: File -> Sync Project with Gradle Files)


Installation auf einem Android Gerät
- Android Gerät per USB mit dem Rechner verbinden. 
- In der Meüleiste sollte nun das Gerät im DropDown "Verfügbare Geräte" angezeigt werden. 
- Klick auf den "Play" Button in der Menüleiste. 
- datavis App wird auf dem verbundenem Gerät Installiert. 

Info: 
Die datavis Applikation wird nicht über den Google PlayStore verteilt. 
Daher kann es vorkommen, dass die Installieren von Applikationen aus Unbekannten Quellen erlaubt werden muss.

Dies kann sich je nach Android Version Unterscheiden. 
Über die folgende Google Beispiel Suche kann herausgefunden werden wie 
die Installieren von Applikationen aus Unbekannten Quellen erlaubt werden kann.

Link: Beispiel Google Suche: android 7 installation von apps aus unbekannten quellen zulassen
https://www.google.com/search?q=android+7+installation+von+apps+aus+unbekannten+quellen+zulassen

