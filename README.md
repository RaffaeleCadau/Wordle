#Wordle

Il gioco di Wordle è un gioco di parole in cui il giocatore deve indovinare una parola di 10 lettere, estratta casualmente, entro un massimo di 12 tentativi.
Wordle è scritto in java con architettura client-server, il client consente all’utente di interagire con il gioco attraverso un’interfaccia utente intuitiva.
1. Apri il terminale e naviga fino alla cartella "Wordle" dove si trovano i
file del progetto.
2. Compila il server eseguendo il seguente comando:
$ javac -cp library/gson-2.10.jar Server/*.java Server/Task/*.java
Server/User/*.java common/*.java -d bin
3. Compila il client eseguendo il comando successivo:
$ javac -cp library/gson-2.10.jar Client/*.java common/*.java -d bin
4. Al termine dell’esecuzione dei comandi, verrà creata una nuova cartella
chiamata "bin" nella directory "Wordle". All’interno di questa cartella
troverai i file compilati con estensione ".class".
5. Ora puoi creare l’eseguibile jar del server eseguendo il seguente coman-
do:
$ jar cmvf MANIFESTserver.MF server.jar library/gson-2.10.jar -C bin
Server/ -C bin/ common/
6. Successivamente, crea l’eseguibile jar del client con il comando:
$ jar cmvf MANIFESTclient.MF client.jar library/gson-2.10.jar -C bin
Client/ -C bin common/
7. A questo punto, hai generato due file JAR: "server.jar" e "client.jar".
Per eseguire il server, utilizza il seguente comando:
$ java -jar server.jar [file di configurazione]
8. Per eseguire il client, in un altro terminale, utilizza il comando:
$ java -jar client.jar [file di configurazione]
