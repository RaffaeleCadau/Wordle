package Client;

// librerie per gli eventi della tastiera e chiudere la finestra
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

// librerie per la grafica
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import java.awt.GridLayout;

import common.Player;
import common.WordleCodici;
import java.util.ArrayList;

// classe che gestisce la grafica del client
public class ClientGui extends JFrame implements KeyListener {
    private WordleClient wordle;
    private JLabel labelBenvenuto, labelRis;
    private JPanel Panel;
    private JTextField[] wordText;
    private JLabel[][] wordLabel;
    private Player player;
    private int Itext = 0;
    private int index = 10;
    private int indexMessage = 0;

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // se la casella di testo non ha il focus allora cerco la casella di testo che
        // ha il focus
        if (!wordText[Itext].hasFocus())
            while (!wordText[Itext].hasFocus()) {
                Itext = (Itext + 1) % wordText.length;
            }
        // se la lettera digitata è una lettera dell'alfabeto
        if (Character.isLetter(arg0.getKeyChar()) && Itext < wordText.length) {
            // scrivo la lettera maiuscola nella casella di testo e poi sposto il cursore
            // alla prossima casella
            wordText[Itext].setText(new String(arg0.getKeyChar() + "").toUpperCase());
            Itext = (Itext + 1) % wordText.length;
            wordText[Itext].requestFocusInWindow();
        }
        // se il carattere digitato è il backspace
        else if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            // cancello la lettera nella casella di testo e poi sposto il cursore alla casella precedente
            wordText[Itext].setText("");
            if(Itext == 0)
                Itext = wordText.length - 1;
            else
                Itext = (Itext - 1) % wordText.length;
            wordText[Itext].requestFocusInWindow();
        }
        //se il carattere digitato sono le frecce
        else if (arg0.getKeyCode() == KeyEvent.VK_LEFT || arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
            //sposto il cursore alla casella precedente o successiva
            if (arg0.getKeyCode() == KeyEvent.VK_LEFT)
                if(Itext == 0)
                    Itext = wordText.length - 1;
                else
                    Itext = (Itext - 1) % wordText.length;
            else
                Itext = (Itext + 1) % wordText.length;
            wordText[Itext].requestFocusInWindow();
        }
        else {
            // se il carattere digitato non è una lettera allora lo cancello
            wordText[Itext].setText("");
        }
    }

    public ClientGui(ConfigClient config) {
        // inizializzo la GUI del client
        super("Client WORDLE");
        setSize(1000, 800);
        setLayout(null);
        // Questa label di benvenuto è sempre visibile
        labelBenvenuto = new JLabel();
        labelBenvenuto.setText("Benvenuto in WORDLE");
        labelBenvenuto.setBounds(0, 0, getWidth(), 90);
        labelBenvenuto.setHorizontalAlignment(JLabel.CENTER);
        labelBenvenuto.setVerticalAlignment(JLabel.CENTER);
        labelBenvenuto.setFont(labelBenvenuto.getFont().deriveFont(16.0f));
        labelBenvenuto.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
        add(labelBenvenuto);
        // Questa label serve per mostrare i messaggi di risposta del server, in
        // particolare quelli di errore. Se non ci sono messaggi di errore non è
        // visibile
        labelRis = new JLabel();
        labelRis.setBounds(0, 90, getWidth(), 90);
        labelRis.setHorizontalAlignment(JLabel.CENTER);
        labelRis.setVerticalAlignment(JLabel.CENTER);
        labelRis.setFont(labelRis.getFont().deriveFont(14.0f));
        labelRis.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
        labelRis.setVisible(false);
        add(labelRis);

        Panel = panelHome((getWidth() - 800) / 2, (getHeight() - 100) / 2, 800, 200);
        add(Panel);
        setVisible(true);
        wordle = new ClientMain(config);

        // alla chiusura della finestra termino il programma
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    wordle.logout();
                } catch (Exception ee) {
                }
                System.exit(0);
            }
        });
    }

    public JPanel panelHome(int x, int y, int width, int height) {
        // form per il login e la registrazione dell'utente
        JPanel panel = new JPanel();
        panel.setBounds(x, y, width, height);
        panel.setBackground(java.awt.Color.LIGHT_GRAY);
        panel.setLayout(new GridLayout(3, 3, 10, 10));

        JLabel usernameLabel = new JLabel();
        usernameLabel.setText("Username");
        usernameLabel.setHorizontalAlignment(JLabel.CENTER);
        usernameLabel.setVerticalAlignment(JLabel.CENTER);
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(14.0f));
        panel.add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setFont(usernameLabel.getFont().deriveFont(14.0f));
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel();
        passwordLabel.setText("Password");
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        passwordLabel.setVerticalAlignment(JLabel.CENTER);
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(14.0f));
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(passwordLabel.getFont().deriveFont(14.0f));
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(loginButton.getFont().deriveFont(14.0f));
        panel.add(loginButton);

        JButton RegisterButton = new JButton("Register");
        RegisterButton.setFont(RegisterButton.getFont().deriveFont(14.0f));
        panel.add(RegisterButton);

        // evento che viene eseguito quando si clicca sul bottone di registrazione
        RegisterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int r = wordle.register(usernameField.getText(), new String(passwordField.getPassword()));
                labelRis.setText(WordleCodici.StringToInt(r));
                labelRis.setVisible(true);
            }
        });

        // evento che viene eseguito quando si clicca sul bottone di login
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int r = wordle.login(usernameField.getText(), new String(passwordField.getPassword()));
                labelRis.setText(WordleCodici.StringToInt(r));
                labelRis.setVisible(true);
                if (r == WordleCodici.OKLogin) {
                    labelRis.setVisible(false);
                    // se il login è andato a buon fine, si passa alla schermata di gioco
                    Panel = panelGame(x, y, width, height);
                    remove(panel);
                    repaint();
                    add(Panel);
                    repaint();
                } else {
                    // se il login non è andato a buon fine, si rimane nella schermata di login
                    // e notifico l'errore all'utente
                    labelRis.setText(WordleCodici.StringToInt(r));
                    labelRis.setVisible(true);
                }
            }
        });

        return panel;
    }

    public JPanel panelGame(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setBounds(x, y, width, height);
        panel.setBackground(java.awt.Color.GRAY);
        panel.setLayout(new GridLayout(2, 2, 0, 0));
        JButton play = new JButton("Gioca");
        play.setFont(play.getFont().deriveFont(14.0f));
        panel.add(play);
        JButton Statistics = new JButton("Le mie statistiche");
        Statistics.setFont(Statistics.getFont().deriveFont(14.0f));
        panel.add(Statistics);
        JButton showSharing = new JButton("Partite condivise");
        showSharing.setFont(showSharing.getFont().deriveFont(14.0f));
        panel.add(showSharing);
        JButton logout = new JButton("Logout");
        logout.setFont(logout.getFont().deriveFont(14.0f));
        panel.add(logout);

        // richiesta di gioco
        play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int r = wordle.play();
                labelRis.setText(WordleCodici.StringToInt(r));
                labelRis.setVisible(true);
                if (r == WordleCodici.OKPlay) {
                    labelRis.setVisible(false);
                    remove(Panel);
                    Panel = panelPlay(x, y, width, height);    
                    repaint();
                    add(Panel);
                    repaint();
                    revalidate();
                    wordText[0].requestFocus();
                }
            }
        });
        // statistiche
        Statistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                int r = wordle.getMeStatistics();
                if(r != WordleCodici.OKMeStatics){
                    labelRis.setText(WordleCodici.StringToInt(r));
                    labelRis.setVisible(true);
                    return;
                }
                player = wordle.getPlayer();
                remove(Panel);
                Panel = panelStatistics(x, 200, width, 600, player);
                repaint();
                add(Panel);
                repaint();
                revalidate();
            }
        });
        // partite condivise
        showSharing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                remove(panel);
                repaint();
                Panel = showSharing(x, 200, width, 600);
                add(Panel);
                repaint();
                revalidate();
            }
        });
        // logout
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    wordle.logout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        return panel;
    }

    public JPanel panelPlay(int x, int y, int width, int height) {
        index =10;
        if (height < 600)
            height = 600;
        JPanel panel = new JPanel();
        panel.setBounds(x, 180, width, height);
        panel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
        panel.setLayout(null);
        JPanel panel1 = new JPanel();
        panel1.setBounds(0, 0, width, 480);
        panel1.setBackground(java.awt.Color.WHITE);
        int lettere = 10, tentativi = 11;
        panel1.setLayout(new GridLayout(tentativi + 1, lettere, 0, 0));
        wordText = new JTextField[lettere];
        wordLabel = new JLabel[tentativi][lettere];

        for (int i = 0; i < tentativi; i++) {
            for (int j = 0; j < lettere; j++) {
                wordLabel[i][j] = new JLabel();
                wordLabel[i][j].setHorizontalAlignment(JLabel.CENTER);
                wordLabel[i][j].setVerticalAlignment(JLabel.CENTER);
                wordLabel[i][j].setFont(wordLabel[i][j].getFont().deriveFont(14.0f));
                wordLabel[i][j].setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
                panel1.add(wordLabel[i][j]);
                wordLabel[i][j].setVisible(false);
                wordLabel[i][j].setOpaque(true);
            }
        }

        for (int i = 0; i < lettere; i++) {
            wordText[i] = new JTextField();
            wordText[i].setFont(wordText[i].getFont().deriveFont(14.0f));
            wordText[i].setHorizontalAlignment(JTextField.CENTER);
            wordText[i].addKeyListener(this);
            panel1.add(wordText[i]);
        }

        panel.add(panel1);
        panel1.setVisible(true);
        JButton button = new JButton("Invia");
        button.setBounds(0, height - 100, width / 2, 100);
        button.setFont(button.getFont().deriveFont(14.0f));
        panel.add(button);
        JButton buttonHome = new JButton("Home");
        buttonHome.setBounds(width / 2, height - 100, width / 2, 100);
        buttonHome.setFont(buttonHome.getFont().deriveFont(14.0f));
        panel.add(buttonHome);

        // invio della parola / condivisione della partita
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                if (button.getText() == "Share") {
                    int r = wordle.share();
                    labelRis.setText(WordleCodici.StringToInt(r));
                    labelRis.setVisible(true);
                    if (r == WordleCodici.OKShare) {
                        remove(Panel);
                        Panel = panelGame(x, y, width, 200);
                        repaint();
                        add(Panel);
                        repaint();
                        revalidate();
                    }
                    return;
                }
                wordText[0].requestFocus();
                String word = "";
                for (int i = 0; i < lettere; i++) {
                    word += wordText[i].getText().toLowerCase();
                }
                int r = wordle.sendWord(word);
                if(r != WordleCodici.gameOver && r != WordleCodici.Win && r != WordleCodici.ErrorWord){
                    labelRis.setText(WordleCodici.StringToInt(r));
                    labelRis.setVisible(true);
                    return;
                }
                String s = wordle.getTesto();
                if (r == WordleCodici.NoPlayWORDLE) {
                    labelRis.setText("Tempo per la patita scaduto");
                    labelRis.setVisible(true);
                    remove(Panel);
                    repaint();
                    Panel = panelGame(x, y, width, 200);
                    add(Panel);
                    repaint();
                    revalidate();
                    return;
                }
                if(r == WordleCodici.Error) return;
                if (r != WordleCodici.wordNotExist) {
                    if (index >= 0) {
                        for (int i = 0; i < lettere; i++) {
                            wordLabel[index][i].setText(wordText[i].getText());
                            wordText[i].setText("");
                        }
                        for (int i = 0; i < lettere; i++) {
                            if (s.charAt(i) == '+') {
                                wordLabel[index][i].setBackground(java.awt.Color.GREEN);
                                wordLabel[index][i].setVisible(true);
                            } else if (s.charAt(i) == 'X') {
                                wordLabel[index][i].setBackground(java.awt.Color.RED);
                                wordLabel[index][i].setVisible(true);
                            } else {
                                wordLabel[index][i].setBackground(java.awt.Color.YELLOW);
                                wordLabel[index][i].setVisible(true);
                            }
                        }

                    } else
                        for (int i = 0; i < lettere; i++) {
                            if (s.charAt(i) == '+')
                                wordText[i].setBackground(java.awt.Color.GREEN);
                            else if (s.charAt(i) == 'X')
                                wordText[i].setBackground(java.awt.Color.RED);
                            else
                                wordText[i].setBackground(java.awt.Color.YELLOW);
                        }
                    index--;
                }
                if (r == WordleCodici.Win || r == WordleCodici.gameOver)
                    button.setText("Share");
            }
        });

        // torna alla home
        buttonHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                remove(Panel);
                repaint();
                Panel = panelGame(x, y, width, 200);
                add(Panel);
                repaint();
                revalidate();
            }
        });

        return panel;
    }

    public JPanel panelStatistics(int x, int y, int width, int height, Player player) {
        JPanel panel = new JPanel();
        panel.setBounds(x, y, width, height);
        panel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
        panel.setLayout(null);

        JLabel labelPartite = new JLabel("Partite giocate: " + player.getPartite());
        labelPartite.setBounds(0, 0, width, 50);
        labelPartite.setHorizontalAlignment(JLabel.CENTER);
        labelPartite.setVerticalAlignment(JLabel.CENTER);
        labelPartite.setFont(labelPartite.getFont().deriveFont(14.0f));
        panel.add(labelPartite);

        JLabel labelVinte = new JLabel("Percentuale di partite vinte: " + player.getPercentualePartiteVinte());
        labelVinte.setBounds(0, 50, width, 50);
        labelVinte.setHorizontalAlignment(JLabel.CENTER);
        labelVinte.setVerticalAlignment(JLabel.CENTER);
        labelVinte.setFont(labelVinte.getFont().deriveFont(14.0f));
        panel.add(labelVinte);

        JLabel streak = new JLabel("Streak: " + player.getStreak());
        streak.setBounds(0, 100, width, 50);
        streak.setHorizontalAlignment(JLabel.CENTER);
        streak.setVerticalAlignment(JLabel.CENTER);
        streak.setFont(streak.getFont().deriveFont(14.0f));
        panel.add(streak);

        JLabel maxStreak = new JLabel("Max streak: " + player.getMaxStreak());
        maxStreak.setBounds(0, 150, width, 50);
        maxStreak.setHorizontalAlignment(JLabel.CENTER);
        maxStreak.setVerticalAlignment(JLabel.CENTER);
        maxStreak.setFont(maxStreak.getFont().deriveFont(14.0f));
        panel.add(maxStreak);

        // create JLabel with scroll
        String s[] = player.getDistributionStrings();
        JList<String> list = new JList<String>(s);
        list.setFont(list.getFont().deriveFont(16.0f));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBounds(0, 200, width, height - 300);
        panel.add(scroll);

        JButton button = new JButton("Home");
        button.setBounds(0, height - 100, width, 100);
        button.setFont(button.getFont().deriveFont(14.0f));
        panel.add(button);
        

        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                remove(panel);
                repaint();
                Panel = panelGame(x, y, width, 200);
                add(Panel);
                repaint();
                revalidate();
            }
        });
        
        return panel;
    }

    public JPanel showSharing(int x, int y, int width, int height) {
        height = height>600?height:600;

        JPanel panel = new JPanel();
        panel.setBounds(x, y, width, height);
        panel.setLayout(null);

        JPanel panel1 = new JPanel();
        panel1.setBounds(0, 0, width, 480);
        int lettere = 10, tentativi = 12;
        panel1.setLayout(new GridLayout(tentativi, lettere, 0, 0));
        wordText = new JTextField[lettere];
        wordLabel = new JLabel[tentativi][lettere];

        for (int i = 0; i < tentativi; i++) {
            for (int j = 0; j < lettere; j++) {
                wordLabel[i][j] = new JLabel();
                wordLabel[i][j].setHorizontalAlignment(JLabel.CENTER);
                wordLabel[i][j].setVerticalAlignment(JLabel.CENTER);
                wordLabel[i][j].setFont(wordLabel[i][j].getFont().deriveFont(14.0f));
                wordLabel[i][j].setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
                panel1.add(wordLabel[i][j]);
                wordLabel[i][j].setVisible(true);
                wordLabel[i][j].setOpaque(true);
            }
        }

        panel.add(panel1);

        JButton buttonHome = new JButton("home");
        buttonHome.setBounds(0, 480, width/3, 100);
        buttonHome.setFont(buttonHome.getFont().deriveFont(14.0f));
        panel.add(buttonHome);

        JButton buttonPrev = new JButton("Prev");
        buttonPrev.setBounds(width/3, 480, width/3, 100);
        buttonPrev.setFont(buttonPrev.getFont().deriveFont(14.0f));
        panel.add(buttonPrev);

        JButton buttonNext = new JButton("Next");
        buttonNext.setBounds(2*width/3, 480, width/3, 100);
        buttonNext.setFont(buttonNext.getFont().deriveFont(14.0f));
        panel.add(buttonNext);

        index = 11;
        indexMessage = 0;
        ArrayList<String> messages = wordle.showSharing();
        if(indexMessage == 0) buttonPrev.setEnabled(false);
        if(indexMessage == messages.size()-1 || messages.size()<2) buttonNext.setEnabled(false);
        if(messages.size() == 0){
            labelRis.setText("Non ci sono messaggi condivisi");
            labelRis.setVisible(true);
        }
        else{
            String s = messages.get(0);
            String[] split = s.split(":");
            labelRis.setText(split[0]);
            labelRis.setVisible(true);
            split = split[1].split("\"");
            for(int i=1; i<split.length; i=i+2){
                String[] str = split[i].split("");
                int j=0;
                for(String c : str){
                    if(c.charAt(0)=='+') wordLabel[index][j].setBackground(java.awt.Color.GREEN);
                    else if(c.charAt(0)=='X') wordLabel[index][j].setBackground(java.awt.Color.RED);
                    else wordLabel[index][j].setBackground(java.awt.Color.YELLOW);
                    wordLabel[index][j].setVisible(true);
                    j++;
                }
                index--;
            }
        }

        buttonHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelRis.setVisible(false);
                remove(panel);
                repaint();
                Panel = panelGame(x, y, width, 200);
                add(Panel);
                repaint();
                revalidate();
            }
        });

        buttonPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexMessage--;
                index = 11;
                for(int i=0; i<tentativi; i++)
                    for(int j=0; j<lettere; j++)
                        wordLabel[i][j].setBackground(java.awt.Color.WHITE);

                buttonNext.setEnabled(true);
                if(indexMessage == 0) buttonPrev.setEnabled(false);
                String s = messages.get(indexMessage);
                String[] split = s.split(":");
                labelRis.setText(split[0]);
                labelRis.setVisible(true);
                split = split[1].split("\"");
                for(int i=1; i<split.length; i=i+2){
                    String[] str = split[i].split("");
                    int j=0;
                    for(String c : str){
                        if(c.charAt(0)=='+') wordLabel[index][j].setBackground(java.awt.Color.GREEN);
                        else if(c.charAt(0)=='X') wordLabel[index][j].setBackground(java.awt.Color.RED);
                        else wordLabel[index][j].setBackground(java.awt.Color.YELLOW);
                        wordLabel[index][j].setVisible(true);
                        j++;
                    }
                    index--;
                }
            }
        });

        buttonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexMessage++;
                index = 11;
                for(int i=0; i<tentativi; i++)
                    for(int j=0; j<lettere; j++)
                        wordLabel[i][j].setBackground(java.awt.Color.WHITE);
                    
                
                buttonPrev.setEnabled(true);
                if(indexMessage == messages.size()-1) buttonNext.setEnabled(false);
                String s = messages.get(indexMessage);
                String[] split = s.split(":");
                labelRis.setText(split[0]);
                labelRis.setVisible(true);
                split = split[1].split("\"");
                for(int i=1; i<split.length; i=i+2){
                    String[] str = split[i].split("");
                    int j=0;
                    for(String c : str){
                        if(c.charAt(0)=='+') wordLabel[index][j].setBackground(java.awt.Color.GREEN);
                        else if(c.charAt(0)=='X') wordLabel[index][j].setBackground(java.awt.Color.RED);
                        else wordLabel[index][j].setBackground(java.awt.Color.YELLOW);
                        wordLabel[index][j].setVisible(true);
                        j++;
                    }
                    index--;
                }
            }
        });

        return panel;
    }
}