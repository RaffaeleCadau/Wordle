package Server.Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import Server.GenericException;
import Server.User.PlayUser;
import common.WordleCodici;

//codice del thread per gestire le richieste di play e send
public class TaskPlay implements Runnable {
    private static ConcurrentHashMap<String, PlayUser> map;
    private String username;
    private SocketChannel client;
    private ByteBuffer buffer;
    private String word;

    public TaskPlay(String username, SocketChannel client, ByteBuffer buffer){
        this.username=username;
        this.word=null;
        this.buffer=buffer;
        this.client=client;
    }

    public TaskPlay(String username, SocketChannel client, ByteBuffer buffer, String word){
        this(username, client, buffer);
        this.word=word;
    }

    @Override
    public void run() {
        try {
            //recupero il PlayerUser
            PlayUser p = map.get(username);
            if(p==null){
                buffer.clear();
                buffer.putInt(WordleCodici.ErrorServer);
                buffer.flip();
                while(buffer.hasRemaining())
                    client.write(buffer);
            }
            else{
                //l'utente ha chiesto di poter fare una partita
                if(this.word==null){
                    int cod = p.newPartita();
                    buffer.clear();
                    buffer.putInt(cod);
                    buffer.flip();
                    while(buffer.hasRemaining())
                        client.write(buffer);
                }
                else{ //l'utente ha fatto la send con una parola
                    int cod;
                    try{
                        //confronto la parola dell'utente e invio una risposta
                        String sugerimento = p.addTentativo(word);
                        cod = WordleCodici.ErrorWord;
                        buffer.clear();
                        buffer.putInt(cod);
                        buffer.put(sugerimento.getBytes());
                    }
                    catch(GenericException e){
                        cod = e.getCode();
                        buffer.clear();
                        buffer.putInt(cod);
                        //in caso di fine gioco invio anche le statistiche
                        if(cod == WordleCodici.gameOver || cod == WordleCodici.Win){
                            buffer.put(e.getMessage().getBytes());
                            buffer.put(PlayUser.serializzazioneUser(p).getBytes());
                        }

                    }
                    finally{
                        buffer.flip();
                        while(buffer.hasRemaining())
                            client.write(buffer);
                    }
                
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void set_map(ConcurrentHashMap<String, PlayUser> map){
        TaskPlay.map=map;
    }
}
