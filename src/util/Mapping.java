package util;

public class Mapping {
    private String controlleur;
    private String methode;
    private VerbAction verbAction;
    public Mapping() {
    }
    public Mapping(String controlleur, String methode, VerbAction verbAction) {
        this.controlleur = controlleur;
        this.methode = methode;
        this.verbAction = verbAction;
    }
    public String getControlleur() {
        return controlleur;
    }
    public void setControlleur(String controlleur) {
        this.controlleur = controlleur;
    }
    public String getMethode() {
        return methode;
    }
    public void setMethode(String methode) {
        this.methode = methode;
    }
    public VerbAction getVerbAction() {
        return verbAction;
    }
    public void setVerbAction(VerbAction verbAction) {
        this.verbAction = verbAction;
    }
    
}
