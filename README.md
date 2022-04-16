<p align="center">
  <img src="https://inf-git.th-rosenheim.de/sep-wif-22/datavis/-/wikis/resources/datavis_logo.png" />
</p>

Datavis ist ein Projekt im Rahmen der **Software Engineering Praxis (WIF)** Veranstaltung in Zusammenarbeit mit **Ericsson**, mit dem Ziel Antennenmodelle zusammen mit ihrere Feldstärke in einer AR-Applikation darzustellen. 

## Getting Started
### Verzeichnisstruktur
    .
    ├── Application                  # tbd
    │   ├── src                 
    │   ├── test         
    │   └── gui                 
    ├── Documents                    # Documentation files 
    │   └── Images                   
    ├── Data                         # Data provide by Ericsson
    └── README.md
    
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
tbd
