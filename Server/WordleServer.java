package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Server.Task.TaskPlay;
import Server.Task.TaskRegister;
import Server.User.PlayUser;
import common.WordleCodici;

public class WordleServer {
    private Selector selector;
    private static ConcurrentHashMap<String, PlayUser> map;
    private static ConcurrentHashMap<String, PlayUser> tempUser;
    private ThreadPoolExecutor pool;
    //lista di ByteBuffer
    private static ConcurrentLinkedDeque<ByteBuffer> list=new ConcurrentLinkedDeque<ByteBuffer>();

    private static MulticastSocket socketMulticast;
    private static InetAddress multicastAddr;

    public WordleServer(InetSocketAddress address, String dict, int time)throws Exception{

        //preparo la socketChannel e la registro nel selettore
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(address);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT, null);

        //inizializzo le ConcurrentHashMap per gli utenti che hanno fatto il login e per quelli che devo ancora salvare su file
        map = new ConcurrentHashMap<String, PlayUser>();
        tempUser = new ConcurrentHashMap<String, PlayUser>();
        PlayUser.setMap(tempUser);

        //ThreadPool
        pool=new ThreadPoolExecutor(10, 20, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        //setto la mappa dei utente che hanno fatto il login ai Task
        TaskRegister.setMap(map);
        TaskPlay.set_map(map);

        WordleWord.setTime(time);
        WordleWord w = new WordleWord(dict);
    }

    public void runServer(){
        Set<SelectionKey> setKey;
        Iterator<SelectionKey> iterator;
        while(true){
            try {
                if(selector.select()==0) continue;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            //Prendo l’insieme delle connessioni su cui si è verificato un evento voluto, e creo un iteratore per scorrere l’insieme (Set) delle connessioni
            setKey = selector.selectedKeys();
            iterator = setKey.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                if(!key.isValid())key.cancel(); //se la chiave key non è valida viene rimossa del set
                if(key.isConnectable())key.cancel();
                else if(key.isAcceptable()){ //nuovo client, set no Blocking e lo registro nel selettore
                    try {
                        //recupero la ServerSocketChannel dalla key
                        ServerSocketChannel server =(ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ, null);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    finally{
                        iterator.remove(); //tolgo l'iteratore dal set
                    }
                }
                else if(key.isReadable()){ //l'utente mi un inviato un comando
                    //recupero la SocketChannel
                    SocketChannel client = (SocketChannel)key.channel();
                    String username = (String)key.attachment(); //è valido solo se ha fatto il login con successo
                    //prendo un ByteBuffer libero
                    ByteBuffer buffer = null;
                    if(list.isEmpty()) buffer = ByteBuffer.allocate(1024);
                    else buffer = list.getLast();

                    try {
                        //leggo l'azione di un'utente
                        Boolean err=true;
                        buffer.clear();
                        client.read(buffer);
                        buffer.flip();
                        int n = buffer.getInt(); //rappresenta il l'azione dell'utente

                        String userString=null, passString=null, word=null;
                        String[] str=null;
                        if(n>3){
                            if(username==null || map.get(username)==null){//utente ha chiesto un servizio senza aver fatto prima il login
                                buffer.clear();
                                buffer.putInt(WordleCodici.NoLogin);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                err=false;
                            }

                        }
                        if(n==1 || n==2){

                            buffer.compact();
                            buffer.flip();

                            //prendo l'user e la password dal buffer
                            String str1 = new String(buffer.array(), 0, buffer.limit());
                            str= str1.split(" ");
                            buffer.clear();
                            if(str.length!=2){//controllo sul username e password
                                if(str.length==1) buffer.putInt(WordleCodici.emptyPassword);
                                else buffer.putInt(WordleCodici.Error);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                err=false;
                            }
                            else{
                                userString=str[0];
                                passString=str[1];
                                
                                //Se l'utente vuole fare il login mi assicuro che non l'ha fatto prima
                                if(n==2 && username!=null && map.get(username)!=null){
                                    buffer.clear();
                                    buffer.putInt(WordleCodici.ErrorLogin);
                                    buffer.flip();
                                    while(buffer.hasRemaining())
                                        client.write(buffer);
                                    buffer.clear();
                                    err=false;
                                }
                                else if(n==2 && map.get(userString)==null)
                                    key.attach(userString);
                                else if (n==2){
                                    key.attach(null);
                                    buffer.clear();
                                    buffer.putInt(WordleCodici.ErrorLogin);
                                    buffer.flip();
                                    while(buffer.hasRemaining())
                                        client.write(buffer);
                                    buffer.clear();
                                    err=false;
                                }
                            }
                        }
                        if(n==5){//send
                            buffer.compact();
                            buffer.flip();
                            //prendo la word dal buffer
                            word = new String(buffer.array(), 0, buffer.limit());
                            buffer.clear();
                            if(word.length()<1){
                                buffer.putInt(WordleCodici.Error);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                err=false;
                            }
                        }
                        PlayUser p;
                        if(err)
                        switch(n){                      
                            case 1://register(username, password)
                                pool.execute(new TaskRegister(client, buffer, userString, passString));
                            break;
                            case 2://login(username, password)
                                pool.execute(new TaskRegister(client, buffer, userString, passString, true));
                            break;
                            case 3://logout e exit
                                    if(username!=null){
                                    p = map.remove(username);
                                    if(p!=null)
                                        tempUser.put(username, p);
                                    }
                                buffer.clear();
                                buffer.putInt(WordleCodici.OKLogout);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                key.cancel();
                                addFreeByteBuffer(buffer);
                            break;
                            case 4://richiesta di giocare playWORDLE()
                                pool.execute(new TaskPlay(username, client, buffer));
                            break;
                            case 5://l'utente ha inviato una parola
                                pool.execute(new TaskPlay(username, client, buffer, word));
                            break;
                            case 6://sendMeStatics
                                buffer.clear();
                                p = map.get(username);
                                buffer.putInt(WordleCodici.OKMeStatics);
                                buffer.put(PlayUser.serializzazioneUser(p).getBytes());
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                            break;
                            case 7://share
                                buffer.clear();
                                p = map.get(username);
                                if(p.isshare())
                                    buffer.putInt(WordleCodici.OKShare);
                                else buffer.putInt(WordleCodici.NOShare);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                addFreeByteBuffer(buffer);
                                if(p.isshare()){
                                    String strMulticast =username+":\n" + p.share();
                                    DatagramPacket dp = new DatagramPacket(strMulticast.getBytes(), strMulticast.getBytes().length);
                                    dp.setAddress(multicastAddr);
                                    dp.setPort(socketMulticast.getLocalPort());
                                    socketMulticast.send(dp);
                                }
                            break;
                            default:
                                buffer.clear();
                                buffer.putInt(WordleCodici.Error);
                                buffer.flip();
                                while(buffer.hasRemaining())
                                    client.write(buffer);
                                buffer.clear();
                                addFreeByteBuffer(buffer);
                        }
                        else addFreeByteBuffer(buffer);
                    
                    }
                    catch(Exception e) {
                        //se l'utente ha fatto il login e si è disconnesso senza fare il logout
                        //lo rimuovo dalla mappa e lo metto nella lista di utenti temporanei
                        //succede se il programma client crasha o l'utente preme ctrl+c
                        String user = (String)key.attachment();
                        if(user!=null){
                            PlayUser p = map.remove(user);
                            if(p!=null)
                                tempUser.put(user, p);
                        }
                        key.cancel();
                        buffer.clear();
                        addFreeByteBuffer(buffer);
                    }
                    finally{
                        //rimuove la corrente key dal setKey, la key rimane registrata nel selector
                        iterator.remove();
                    }
                    
                }
                

            }
        }
    }

    public static void addFreeByteBuffer(ByteBuffer buffer){
        list.addLast(buffer);
    }
    public static void setPortMulticast(int port, String address) throws IOException{
        multicastAddr = InetAddress.getByName(address); // multicast group address
        InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddr, port); // multicast group
        NetworkInterface netIF = NetworkInterface.getByName("bge0"); // network interface
        socketMulticast = new MulticastSocket(port);
        socketMulticast.joinGroup(multicastGroup, netIF);

    }
    public static ConcurrentHashMap<String,PlayUser> getMapLogin(){return map;}
    public static ConcurrentLinkedDeque<ByteBuffer> listByteBuffers(){return list;}
}
