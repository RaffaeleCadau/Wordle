package common;

public class WordleCodici {

    public final static int UserPassEmpty = 100; //User e pass vuoti
    public final static int RegisterError= 101; //errore registrazione
    public final static int LoginError= 102; //errore login

    public final static int OKLogout = 200;
    public final static int OKLogin = 202; //ok login
    public final static int OKRegister = 203; //ok register
    public final static int OKPlay = 204; //ok play
    public final static int OKMeStatics = 205; //ok MeStatics
    public final static int OKShare = 206; //ok MeShare
    
    public final static int Error= 400; //errore generico, parametri non corretti
    public final static int usernameNotAvailable = 401; //username già usato
    public final static int emptyPassword = 402; //password vuota
    public final static int ErrorPassword = 403; //password sbagliata
    public final static int usernameNotRegister = 404; //l'username non è registrato
    public final static int ErrorLogin = 405; //prima del login è neccessario fare il logout
    public final static int NoLogin = 406; //operazione non permessa prima fare il login
    public final static int NoPlay = 407; //l'utente ha già giocato
    public final static int NoPlayWORDLE = 408; //l'utente non ha richiesto di giocare
    public final static int gameOver = 409; //tentativi finiti, game over
    public final static int ErrorWord = 412; //parola sbagliata 
    public final static int Win=410; //il giocatore ha indovinato la parola
    public final static int wordNotExist = 411;//la parola non è nel dizionario
    public final static int NOShare = 413; //ok MeShare


    public final static int ErrorServer= 500; //errore generico del server, riprovare più tardi

    public static String StringToInt(int cod){
        switch(cod){
            case UserPassEmpty: return new String("Username e password non possono essere vuoti");
            case RegisterError: return new String("Errore registrazione");
            case LoginError: return new String("Errore login");
            //case OK: return new String("Operazione riuscita");
            case OKShare: return new String("Partita condivisa");
            case OKRegister: return new String("Utente registrato con successo");
            case NOShare: return new String("Non hai finito la partita");
            case OKPlay: return new String("Puoi giocare: commando send per fare un tentativo");
            case Error: return new String("Errore generico, parametri non corretti");
            case usernameNotAvailable: return new String("L'username non è disponibile");
            case emptyPassword: return new String("La password è vuota");
            case ErrorPassword: return new String("La password non è corretta");
            case usernameNotRegister: return new String("Prima del login è neccessario registrarsi");
            case ErrorLogin: return new String("Login già fatto");
            case NoLogin: return new String("Si prega di fare prima il login");
            case NoPlay: return new String("Attendere la prossima parola");
            case NoPlayWORDLE: return new String("Si prega di fare prima play");
            case gameOver: return new String("Game over: tentativi esauriti");
            case ErrorWord: return new String("Parola errata");
            case Win: return new String("Parola corretta");
            case wordNotExist: return new String("La parola non è nel dizionario");
            case ErrorServer: return new String("Errore del server");
            default: return new String("Il commando non esiste");
        }


    }
}
