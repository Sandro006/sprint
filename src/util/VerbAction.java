package util;

public class VerbAction {
    String verb;
    String action;
    public VerbAction(String verb, String action) {
        this.verb = verb;
        this.action = action;
    }
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public boolean testVerbAction(String verb,String action){
        if(this.verb.equalsIgnoreCase(verb) && this.action.equals(action)){
            return true;
        }
        return false;
    }
}
