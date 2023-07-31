package Server;


import java.lang.reflect.Type;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import Server.User.PlayUser;
import Server.User.User;

//classe main del server
public class ServerMain {
    public static void main(String[] args)throws Exception {

        //hook per salvare i dati dei giocatori quando il server viene chiuso
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run()
            {
                PlayUser.savePlayUser(WordleServer.getMapLogin());
                ConcurrentLinkedDeque<ByteBuffer> l = WordleServer.listByteBuffers();
                while(!l.isEmpty()) l.removeFirst();
            }
        });

        //leggo il file di configurazione del server usando la classe ConfigServer
        FileReader reader =null;
        if(args.length>0)
            reader = new FileReader(args[0]);
        else reader = new FileReader("configServer.json");

        ConfigServer configServer = ConfigServer.newConfigServer(reader);
        System.out.println(configServer);
        reader.close();
        //inizializzo i file per salvare i dati relativi ai giocatori
        PlayUser.setFile(configServer.getFilePlayer()); //Qui le statistiche
        User.setFile(configServer.getFileLogin());//Qui le credenziali d'accesso

        InetSocketAddress address =new InetSocketAddress(configServer.getAddress(), configServer.getPort());
        WordleServer wordle = new WordleServer(address,configServer.getDict(),configServer.getTime());
        WordleServer.setPortMulticast(configServer.getPortMulticast(), configServer.getAddressMulticast());
        wordle.runServer();
    }
}

//oggetto config
class ConfigServer{
    private String address;
    private int port;
    private String addressMulticast;
    private int portMulticast;
    private String fileLogin;
    private String filePlayer;
    private String dict;
    private int time;

    public static ConfigServer newConfigServer(FileReader reader){
        //leggo il file di configurazione in formato json
        String jsonConfigServer=JsonParser.parseReader(reader).toString();

        //"deserializzo" l'oggetto di tipo ConfigServer usando la reflection
        Gson gson = new Gson();
        Type configServer = new TypeToken<ConfigServer>(){}.getType();
        ConfigServer cServer = gson.fromJson(jsonConfigServer, configServer);
        return cServer;
        
    }

    
    @Override
    public String toString() {
        return "{\n \taddress: "+ address+"\n \tport: "+port+"\n \taddressMulticast: "+ addressMulticast+"\n \tportMulticast: "+portMulticast+"\n \tfileLogin: "+ fileLogin+"\n \tfilePlayer: "+filePlayer+"\n \tdict: "+dict+"\n \ttime: "+time+"\n}";
    }


    public String getAddress(){return address;}
    public int getPort(){return port;}

    public String getAddressMulticast(){return addressMulticast;}
    public int getPortMulticast(){return portMulticast;}

    public int getTime(){return time;}
    public String getFileLogin(){return fileLogin;}
    public String getFilePlayer(){return filePlayer;}
    public String getDict(){return dict;}
}