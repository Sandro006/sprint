## Création de l'arborescence du projet
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