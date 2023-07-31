package Client;

import common.Player;
import common.WordleCodici;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

// librerie per interagire con le socket e per multicast
import java.net.Socket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;


// librerie per interagire con oggetti json e deserializzazione
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.Type;

/*
* classe che contiene il main del client,
* implementa la logica del client
*/
public class ClientMain implements WordleClient {
    private Socket socket=null;
    private DataInputStream in;
    private DataOutputStream out;
    private String testo;
    private Player player=null;
    private ArrayList<String> messaggi = new ArrayList<String>();
    private  MulticastSocket multicastClient = null;
    private InetSocketAddress multicastGroup = null;
    private NetworkInterface netIF = null;
    public static boolean exit=false;
    private Thread thread;

    
    //main del client
    public static void main(String[] args) {
        //leggo il file di configurazione, se non viene passato come argomento leggo il file di default
        try {
            FileReader reader;
            if(args.length>0) reader=new FileReader(args[0]);
            else reader=new FileReader("configClient.json");
            ConfigClient config = ConfigClient.newConfigClient(reader);
            reader.close();

            ClientGui gui=new ClientGui(config);
        } catch (Exception e) {
            System.out.println("Errore nella lettura del file di configurazione");
            System.exit(1);
        }
    }

    // costruttore del client
    public ClientMain(ConfigClient config){
        try {
            //creo il socket del client
            socket=new Socket(config.getAddress(), config.getPort());
            //creo gli stream di input e output
            in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            InetAddress multicastAddr = InetAddress.getByName(config.getAddressMulticast()); // multicast group address
            multicastGroup = new InetSocketAddress(multicastAddr, config.getPortMulticast()); //multicast group
            netIF = NetworkInterface.getByName("bge0"); // network interface
            multicastClient = new MulticastSocket(config.getPortMulticast()); // create a multicast client socket
            multicastClient.joinGroup(multicastGroup, netIF); // join multicast group

            //inizializzo il thread che resta in ascolto di messaggi multicast
            TaskMulticast t = new TaskMulticast(multicastClient, messaggi);
            thread = new Thread(t);

        } catch (Exception e) {
            System.out.println("Errore nella creazione del socket:"+ e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public int login(String username, String password){
        int ris = WordleCodici.LoginError;
        if(username=="" || password=="") return WordleCodici.UserPassEmpty;
        try{
            out.writeInt(2);
            String msg = username+" "+password;
            out.write(msg.getBytes());
            out.flush();
            ris = in.readInt();
            if(ris == WordleCodici.OKLogin) thread.start(); //se il login va a buon fine avvio il thread che ascolta i messaggi multicast
            return ris;
        }catch(Exception e){
            return ris;
        }
    }

    @Override
    public int register(String username, String password){
        int ris = WordleCodici.RegisterError;
        if(username=="" || password=="") return WordleCodici.UserPassEmpty;
        try{
            out.writeInt(1);
            String msg = username+" "+password;
            out.write(msg.getBytes());
            out.flush();
            ris = in.readInt();
            return ris;
        }catch(Exception e){
            return ris;
        }

    }

    @Override
    public void logout() throws IOException{
        int r = 0;
        while(r!=WordleCodici.OKLogout){
            out.writeInt(3);
            out.flush();
            r = in.readInt();
        }
        out.close();
        in.close();
        socket.close();

        ClientMain.exit=true;
        if(!multicastClient.isClosed()){
            multicastClient.leaveGroup(multicastGroup, netIF);
            multicastClient.close();
        }
    }

    @Override
    public int play(){
        int ris = WordleCodici.NoPlay;
        try{
            out.writeInt(4);
            out.flush();
            ris = in.readInt();
            return ris;
        }catch(Exception e){
            return ris;
        }
    }


    @Override
    public int getMeStatistics(){
        int ris = WordleCodici.NoLogin;
        try{
            out.writeInt(6);
            out.flush();
            ris = in.readInt();
            // aspetto la stringa della serializzazione del player e la deserializzo con reflection
            
            if(ris == WordleCodici.OKMeStatics){
                byte[] buffer=new byte[1024];
                in.read(buffer);
                String s = new String(buffer);
                try{                  
                Type playerType = new TypeToken<Player>(){}.getType();
                // senza JsonReader si ottiene il seguente errore:
                // Use JsonReader.setLenient(true) to accept malformed JSON at line ...
                JsonReader jr =  new JsonReader(new StringReader(s));
                player = new Gson().fromJson(jr, playerType);
                }catch(Exception e){
                    System.out.println("Errore nella deserializzazione del player "+ e.getMessage());
                }
            }
            return ris;
        }catch(Exception e){
            return ris;
        }
    }

    @Override
    public Player getPlayer(){
        return player;
    }

    @Override
    public int sendWord(String word){
        int ris = WordleCodici.NoPlay;
        try{
            out.writeInt(5);
            out.write(word.getBytes());
            out.flush();
            ris = in.readInt();
            if(ris == WordleCodici.ErrorWord || ris == WordleCodici.gameOver || ris == WordleCodici.Win){
                byte[] buffer=new byte[1024];
                in.read(buffer);
                testo = new String(buffer);
            }
            return ris;
        }catch(Exception e){
            return ris;
        }
    }
    @Override
    public int share(){
        int ris = WordleCodici.NOShare;
        try{
            out.writeInt(7);
            out.flush();
            ris = in.readInt();
            return ris;
        }catch(Exception e){
            return ris;
        }
    }

    @Override
    public ArrayList<String> showSharing(){
        return messaggi;
    }

    public String getTesto(){
        return testo;
    }
}


//permette di acquisire i dati di configurazione del client dal file di configurazione
class ConfigClient{
    private String address;
    private int port;
    private String addressMulticast;
    private int portMulticast;
    

    public static ConfigClient newConfigClient(FileReader reader){
        //leggo il file di configurazione in formato json
        String jsonConfigClient=JsonParser.parseReader(reader).toString();

        //"deserializzo" l'oggetto di tipo ConfigClient usando la reflection
        Gson gson = new Gson();
        Type ConfigClient = new TypeToken<ConfigClient>(){}.getType();
        ConfigClient cClient = gson.fromJson(jsonConfigClient, ConfigClient);
        return cClient;
        
    }


    public String getAddress(){return address;}
    public int getPort(){return port;}

    public String getAddressMulticast(){return addressMulticast;}
    public int getPortMulticast(){return portMulticast;}
}