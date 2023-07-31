package common;

public class GameWord{
    private  String word;
    private int tentativi;
    public GameWord(String word, int tentativi){
        this.word=word;
        this.tentativi=tentativi;
    }
    
    @Override
    public String toString() {
        return word+": "+tentativi;
    }
}