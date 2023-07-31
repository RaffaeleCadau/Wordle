package Server.Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import Server.WordleServer;
import Server.User.PlayUser;
import Server.User.User;
import common.WordleCodici;

//Questo thread soddisfa le richieste di register e login
public class TaskRegister implements Runnable {
    private ByteBuffer buffer;
    private SocketChannel client;
    private String userString, passString;
    private boolean login;
    private static ConcurrentHashMap<String, PlayUser> map;
    public TaskRegister(SocketChannel client, ByteBuffer buffer, String userString, String passString, boolean login){
        this.buffer=buffer;
        this.client=client;
        this.userString=userString;
        this.passString=passString;
        this.login=login;
    }
    public TaskRegister(SocketChannel client, ByteBuffer buffer, String userString, String passString){
        this(client, buffer, userString, passString, false);
    }
    @Override
    public void run(){
        try{
            if(!login){ //procedo alla registrazione dell'utente se userString e passString non sono vuote
                if(userString=="")
                    buffer.putInt(WordleCodici.usernameNotAvailable);
                else if(passString=="") buffer.putInt(WordleCodici.emptyPassword);
                else{
                    int cod = User.signIn(userString, passString);
                    buffer.putInt(cod);
                }
            }
            else{ //l'utente vuole fare il login
                //se non ha già fatto il login verifico le credenziali
                if(map.get(userString)==null){
                    int cod=User.login(userString, passString);
                    if(cod == WordleCodici.OKLogin){
                        //se le credenziali sono corrette allora recupero le statistiche 
                        PlayUser p = PlayUser.newPlayUser(userString);
                        map.put(userString, p);
                    }
                    buffer.putInt(cod);
                }
                else buffer.putInt(WordleCodici.ErrorLogin);
            }
            //rispondo al client
            buffer.flip();
            while(buffer.hasRemaining())
                try {
                    client.write(buffer);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            buffer.clear();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        finally{
            WordleServer.addFreeByteBuffer(buffer);
        }
    }
    public static void setMap(ConcurrentHashMap<String,PlayUser> map){
        TaskRegister.map=map;
    }
}
