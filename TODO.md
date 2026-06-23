### -Création de l'arborescence du projet

src/
controller /
framework /
FrontController.java
views /

## Note:

- chaque url peut etre associé à une servlet différente
- le FrontController:
  - recoit tous les appels
  - regarde l'URL demandé
  - décide quel controleur doit etre executé
  - renvoie la réponse

## Sprint 1

* Creation de notation "Controller"
* Creation de plusieurs Controller avec la notation
* Creation d'une methode qui scanne une package et donne tout les class avec la notation
* Afficher ces classe dans frontController

## Sprint 3

### 3.1
  - Map(url,Class)
    - @GetMapping("/test","GET")
  -  faire une classe pour savoir si l'url est get ou post (dans le cas où 2 methodes avec le meme url?)
### 3.2
  - Exécuter le methode (à tjr instancier)
