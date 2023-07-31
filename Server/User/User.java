package Server.User;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import common.WordleCodici;
//La classe User gestisce gli username e le relative password
public class User{
    private static String file;
    private static JsonObject fileObject;
    
    //setta il file Username e password degli utenti
    //se il file non esiste lo crea e ci scrive {}
    public static void setFile(String file)throws Exception{
        User.file=file;

        File f = new File(file);
        if(!f.exists()){
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write("{}");
            fw.flush();
            fw.close();
        }
        //leggo il file
        FileReader fReader = new FileReader(f);
        fileObject=JsonParser.parseReader(fReader).getAsJsonObject();
        fReader.close();
    }
    //confronta user e passString, con i dati scritti nel file
    public static int login(String user, String passString){
        synchronized(fileObject){
            Map<String,JsonElement> mappa =fileObject.asMap();
            //System.out.println(mappa.getClass());
            if(mappa.get(user)==null) return WordleCodici.usernameNotRegister; //l'user non è registrato
            if(mappa.get(user).getAsString().equals(passString)) return WordleCodici.OKLogin;
            else return WordleCodici.ErrorPassword;//la password non corrisponde
        }
    }
    //registrazione di un utente
    public static int signIn(String user, String passString){
        if(passString =="" || passString==null) return WordleCodici.emptyPassword;//password vuota
        synchronized(fileObject){
            Map<String,JsonElement> mappa =fileObject.asMap();
            if(mappa.get(user)!=null) return WordleCodici.usernameNotAvailable;//user è già registrato
            fileObject.addProperty(user, passString);
            //se è andato tutto a buon fine allora aggiorno il file
            try (FileWriter f = new FileWriter(file)){
                f.write(fileObject.toString().toCharArray());
                f.flush();
                return WordleCodici.OKRegister;
            } catch (Exception e){
                System.out.println(e.getMessage());
                return WordleCodici.ErrorServer;
            }
        }
        
    }
    
}
