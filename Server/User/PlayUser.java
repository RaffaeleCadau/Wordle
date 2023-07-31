package Server.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import Server.GenericException;
import Server.WordleWord;
import common.*;

//Questa classe estende Player, rappresenta un giocatore lato server
public class PlayUser extends Player {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    //tempUser è ConcurrentHashMap dove ci sono gli user che hanno fatto il logout ma non sono ancora salvati su file.
    private static ConcurrentHashMap<String, PlayUser> tempUser;
    private static String file; //file dove sono salvate le statistiche, ovvero PlayUser, degli utenti
    public static final int maxTentativi =12;

    //questi dati sono persi al logout, meglio al cambio della parola
    //Inoltre quando si serializza l'oggetto PlayUser questi dati sono scartati in quanto transient 
    transient private ArrayList<String> tentativi;
    transient private String word; //è l'ultima parola con per cui l'utente ha giocato
    transient private boolean giocato;

    public PlayUser(int partite, int partiteVinte, int streak, int maxStreak, ArrayList<GameWord> distribution){
        this.partite=partite;
        this.partiteVinte=partiteVinte;
        this.streak=streak;
        this.maxStreak=maxStreak;
        this.distribution=distribution;
        this.tentativi=null;
        this.word=null;
        this.giocato=false;
    }
    public PlayUser(){
        this.partite=0;
        this.partiteVinte=0;
        this.streak=0;
        this.maxStreak=0;
        this.distribution=new ArrayList<GameWord>();
        this.tentativi=null;
        this.word=null;
        this.giocato=false;
    }

    //setta la proprietà static file, se il file non esiste lo crea e ci scrive {}.
    public static void setFile(String file)throws IOException {
        PlayUser.file=file;
        File f = new File(file);
        if(!f.exists()){
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write("{}");
            fw.flush();
            fw.close();
        }
    }

    //Scrive le statistiche di tutti gli utenti sul file, per fare ciò si serve di un file temporaneo temp.json 
    public static void savePlayUser(ConcurrentHashMap<String, PlayUser> map){
        try {
            //controllo se esiste il file temp.json, in caso lo elimino e lo ricreo vuoto
            File ftemp = new File("temp.json");
            ftemp.deleteOnExit();
            ftemp.createNewFile();

            BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(ftemp));
            //uso un BufferedWriter per minimizzare le scritture su disco
            //inizializzo il JsonWriter
            JsonWriter writer = new JsonWriter(bufferedwriter);
            writer.beginObject();

            //ottengo tutte le chiavi della mappa, e per ognuna serializzo il valore Player user
            //map è la ConcurrentHashMap degli utenti effettuato il login
            Enumeration<String> keyList = map.keys();
            while(keyList.hasMoreElements()){
                String key = keyList.nextElement();
                PlayUser p = map.get(key);
                writer.name(key).value(serializzazioneUser(p));
            }

            //la stessa cosa la faccio per gli utenti che hanno fatto il logout, e non sono ancora stati salvati
            keyList = tempUser.keys();
            while(keyList.hasMoreElements()){
                String key = keyList.nextElement();
                PlayUser p = tempUser.get(key);
                writer.name(key).value(serializzazioneUser(p));
            }

            //infine aggiungo anche gli utenti che ci sono nel file, ma non nelle due ConcurrentHashMap
            FileReader freader = new FileReader(PlayUser.file); 
            JsonReader reader = new JsonReader(freader);
            String value=null;
            
            reader.beginObject();
            while(reader.hasNext()){
                String user = reader.nextName();
                if(map.get(user)==null && tempUser.get(user)==null){
                    value = reader.nextString();
                    writer.name(user).value(value);
                }
                else reader.skipValue();
            }
            //inoltre li tolgo dalla ConcurrentHashMap degli utenti che hanno fatto il logout
            keyList = tempUser.keys();
            while(keyList.hasMoreElements()){
                tempUser.remove(keyList.nextElement());
            }

            //chiudo il lettore del file
            reader.close();
            freader.close();

            writer.endObject(); //chiudo l'oggetto serializzato
            //flush e close di writer
            writer.flush();
            writer.close();
            File f = new File(PlayUser.file);
            //rinomino il file temporaneo
            ftemp.renameTo(f);

                
            } catch (IOException e) {
                e.printStackTrace();
            }
        
    }
    public static String serializzazioneUser(PlayUser user){
        return gson.toJson(user);
    }

    //restituisco un oggetto PlayUser, se l'utente ha già giocato in precedenza allora è salvato sul file o sulla ConcurrentHashMap
    public static PlayUser newPlayUser(String username) throws FileNotFoundException, IOException{
        PlayUser player = tempUser.remove(username);
        if(player!=null) return player;
        String value=null;
        synchronized(PlayUser.file){
            //se non lo trovo nella ConcurrentHashMap lo cerco nel file
            FileReader freader = new FileReader(PlayUser.file); 
            JsonReader reader = new JsonReader(freader);
            
            reader.beginObject();
            while(reader.hasNext()){
                String user = reader.nextName();
                if(user.equals(username)){
                    value = reader.nextString();
                    break;
                }
                reader.skipValue();
        }
        reader.close();
        freader.close();
        }
        if(value==null) return new PlayUser();
        //se ho trovato l'utente su file allora lo deserializzo con reflection
        Type playUserType =new TypeToken<PlayUser>(){}.getType();
        player = gson.fromJson(value, playUserType);
        player.giocato=false;
        return player;
    }

    public String addTentativo(String word) throws GenericException {
        if(this.tentativi == null || this.word!=WordleWord.getWord()) throw new GenericException(WordleCodici.NoPlayWORDLE); //Errore l'utente non ha richiesto di giocare 
        if(this.tentativi.size()>=PlayUser.maxTentativi || this.giocato)
            throw new GenericException(WordleCodici.NoPlay); ////Errore l'utente ha già giocato 
        try {
            //costruisco la stringa dei suggerimenti
            String str= WordleWord.suggerimenti(word); 
            tentativi.add(str);
            //l'utente ha finito i 12 tentativi
            if(this.tentativi.size()==PlayUser.maxTentativi){
                this.streak=0;
                giocato = true;
                throw new GenericException(WordleCodici.gameOver,str); 
            }
            return str;
        } catch (GenericException e) {
            //l'utente ha indovinato la parola
            if(e.getCode()==WordleCodici.Win){
                tentativi.add(e.getMessage());
                this.partiteVinte=this.partiteVinte+1;
                this.streak++;
                if(this.streak>this.maxStreak) this.maxStreak=this.streak;
                this.distribution.add(new GameWord(word, this.tentativi.size()));
                giocato=true;

            }
            throw e;
            
        }
    }

    public int getTentativi() {
        return tentativi.size();
    }

    //Il metodo prepara le strutture dati per una nuova partita
    public int newPartita(){
        if(this.word == WordleWord.getWord() && this.tentativi.size()<maxTentativi && !giocato) return WordleCodici.OKPlay; //l'utente non ha finito di giocare, non ha terminato i tentativi
        if(this.word==null || this.word != WordleWord.getWord()){//
            this.word=WordleWord.getWord();
            this.tentativi = new ArrayList<String>();
            giocato=false;
            this.partite=this.partite+1;
            return WordleCodici.OKPlay;
        }
        return WordleCodici.NoPlay;
    }

    public boolean isshare(){return this.giocato;}
    public String share(){
        if(this.giocato)
            return gson.toJson(tentativi);
        else return null;
    }

    //public String getWord(){return this.word;}
    public static void setMap(ConcurrentHashMap<String, PlayUser> map){
        tempUser=map;
    }
}