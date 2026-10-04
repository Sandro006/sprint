# main
 Framework
I) sprint0:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet

II) sprint1:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet
   -> mettez vos controllers dans un sous-repertoire "Controller" puis annoter "@AnnotationController"

III) sprint2:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet
   -> annoter vos methodes par "@AnnotationMethode"

IV) sprint3:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet
   -> mettez vos controllers dans un sous-repertoire "Controller" puis annoter "@Controller"
   -> annoter vos methodes par "@Get"
   -> Retourner votre fonction en String

V) sprint4:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet
   -> mettez vos controllers dans un sous-repertoire "Controller" puis annoter "@Controller"
   -> annoter vos methodes par "@Get"
   -> Retourner votre fonction en String et en ModelView

VI) sprint5:
   -> ajouter le .jar du framework dans le sous-repertoire lib de votre projet
   -> mettez vos controllers dans un sous-repertoire "Controller" puis annoter "@Controller"
   -> annoter vos methodes par "@Get"
   -> Retourner votre fonction en String et ne ModelView (pas d'autres)
   -> Il ne doit pas y avoir des methodes de meme url
   -> Les controllers doivent etre dans le package "Controller"
   -> Le package "Controller" ne doit pas etre vide
   
VII) sprint6:
   -> les autres fonctionnalites restent inchanger
   -> On peut gerer maitenant un formulaire en creant une annotation @RequestParam(nom_champ_du_formulaire) ou bien le nom du parametre

VIII) sprint7:
   -> Mais si les champs du formulaire sont beaucoup, il n'est pas pratique de les recuperé un a un dans le controller avec l'annotation @RequestParam
   -> Dans ce cas, on a cree une annotation @ObjectParam(nomChamps[0]); dans le coté developpeur, les noms des champs doivent etre de type comme le suivant se le developpeur utilise une annotation @ObjectParam pour recuperer les champs du formulaire: <input type="text" name="nomClasse.nomAttribut">
   -> On a aussi geré une exception que si l'un des parametres des fonctions pour gerer le formulaire n'est pas annoté @RequestParam ou @ObjectParam..on leve une exception.(Aléa)

VIV) sprint8:
   -> les autres fonctionnalites restent inchanger
   -> On va gerer une session en facilitant le plus possible le travail du developpeur
   -> On cree une classe CustomerSession avec attribut HashMap et methodes(add, get, update, delete) 


