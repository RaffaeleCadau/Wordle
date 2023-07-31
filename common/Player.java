package common;

import java.util.ArrayList;

//Rappresenta un giocatore lato client
public class Player {
    protected int partiteVinte;
    protected int partite;
    protected int streak;
    protected int maxStreak;
    protected ArrayList<GameWord> distribution;

    @Override
    public String toString() {
        return "numero partite giocate: "+partite+"\npercentuale di partite vinte: "+partiteVinte+"\nstreak: "+streak+"\nmaxStreak: "+maxStreak+"\ndistribution: "+distribution;
    }

    public int getPartite() {
        return partite;
    }
    public int getPercentualePartiteVinte() {
        double r = partiteVinte>0?(((double)partiteVinte/(double)partite))*100:0;
        return  (int)r;
    }
    public int getStreak() {
        return streak;
    }
    public int getMaxStreak() {
        return maxStreak;
    }
    public ArrayList<GameWord> getDistribution() {
        return distribution;
    }
    public String[] getDistributionStrings() {
        String[] s = new String[distribution.size()];
        for(int i=0; i<distribution.size(); i++){
            s[i]=distribution.get(i).toString();
        }
        return s;
    }
}
