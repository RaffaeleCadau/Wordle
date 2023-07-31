package Client;

import java.io.IOException;
import java.util.ArrayList;

import common.Player;

public interface WordleClient {
    public int login(String username, String password);
    public int register(String username, String password);
    public void logout() throws IOException;

    public int play();
    public int sendWord(String word);
    public int getMeStatistics();
    public Player getPlayer();
    public String getTesto();
    public int share();
    public ArrayList<String> showSharing();
}
