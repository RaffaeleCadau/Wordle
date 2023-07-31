package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class TaskMulticast implements Runnable {
    private MulticastSocket multicastClient;
    private ArrayList<String> messaggi;

    TaskMulticast(MulticastSocket multicastClient, ArrayList<String> messaggi) throws Exception{
        this.messaggi=messaggi;
        this.multicastClient=multicastClient;
    }
    @Override
    public void run() {
        try {
            while(!ClientMain.exit){
                //aspetto i messaggi multicast dal server e li salvo nel arrayList
                DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
                multicastClient.receive(dp);
                String s=new String(dp.getData());
                synchronized(messaggi){
                    messaggi.add(s);
                }
            }
        }catch(IOException e){}
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
