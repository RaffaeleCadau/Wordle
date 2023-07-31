package Server;

//Classe per le eccezioni di wordle
//consente di "generare" che contengono un intero e una stringa
public class GenericException extends Exception {
    private int code;
    public GenericException(int code){
        super();
        this.code=code;
    }
    public GenericException(int code, String str){
        super(str);
        this.code=code;
    }
    public int getCode(){
        return code;
    }
}
