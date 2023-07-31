package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

import Server.User.PlayUser;
import common.WordleCodici;

public class WordleWord implements Runnable{
    private static ArrayList<String> words;
    private static String word;
    private static int time = 5;
    private static ScheduledThreadPoolExecutor  scheduler;
    //legge il dizionario e lo carica su words
    public WordleWord(String file){
        words = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line=reader.readLine())!=null)
                words.add(line);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //schedulo il thread per cambiare la parola ora e ogni time secondi
        scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.scheduleAtFixedRate(this, 0, time, TimeUnit.SECONDS);
    }
    public static void setTime(int t){WordleWord.time=t;}

    @Override
    public void run() {
        WordleWord.ChoseWord();
        System.out.println(word);
        PlayUser.savePlayUser(WordleServer.getMapLogin());
        ConcurrentLinkedDeque<ByteBuffer> l = WordleServer.listByteBuffers();
        //se ci sono troppi ByteBuffer li elimino, al massimo ne restano il quanti gli utenti
        while(l.size()>WordleServer.getMapLogin().size()) l.getLast();
    }
    //scelgo una parola a caso da words
    protected static void ChoseWord(){
        Random rm = new Random();
        while(true){
            int n = rm.nextInt(words.size());
            if(word != words.get(n)) {
                word = words.get(n);
                break;
            }
        }
    }
    //Questo metodo confronta la parola della send con la word
    public static String suggerimenti(String word) throws GenericException{
        if(word==null) return null;
        //uso la ricerca binaria per trovare la parola
        if(Collections.binarySearch(words, word)<0) throw new GenericException(WordleCodici.wordNotExist);//parola non presente nel dizionario
        StringBuilder strBuilder = new StringBuilder(word);//parola dell'utente
        StringBuilder wordBuilder = new StringBuilder(WordleWord.word);//parola corrente 
        for(int i=0; i<word.length() && i<WordleWord.word.length(); i++)
            if(word.codePointAt(i)==WordleWord.word.codePointAt(i)){
                //nelle posizioni in cui i caratteri sono identici ci mette un + e sostituisce il carattere della parola corrente con -
                strBuilder.setCharAt(i, '+');
                wordBuilder.setCharAt(i, '-');
            }
        for(int i=0; i<strBuilder.length(); i++)
            if(strBuilder.codePointAt(i)!='+'){
                for(int j=0; j<wordBuilder.length(); j++)
                    if(strBuilder.codePointAt(i)==wordBuilder.codePointAt(j)){// la lettera è giusta ma è nella posizione sbagliata 
                        strBuilder.setCharAt(i, '?');
                        wordBuilder.setCharAt(j, '-'); 
                    }
                if(strBuilder.codePointAt(i)!='?') //la lettera non è presente nella parola
                    strBuilder.setCharAt(i, 'X');
            }
        if(word.equals(WordleWord.word)) throw new GenericException(WordleCodici.Win, strBuilder.toString());//parola giusta
        return strBuilder.toString();
    }
    public static String getWord(){return word;}
}
