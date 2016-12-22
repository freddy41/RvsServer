import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread {
	private Socket socket;
	private final int nr;
	private Server server;
	private boolean terminate;
	private InputStream input;
	private BufferedReader inputReader;
	private DataOutputStream out;
	
	public ClientThread(Socket so, int n , Server s)
	{	this.terminate=false;
		this.server= s;
		this.socket=so;
		this.nr = n;
		input = null;
	    inputReader = null;
	    out = null;
	}
	
	public void run() {
        try {
            input = socket.getInputStream();
            inputReader = new BufferedReader(new InputStreamReader(input));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        
        String line;
        while (!terminate) {				//siehe aufgabenblatt 
            try {
                line = inputReader.readLine();
                if(line==null) {disconnet("X"); return;}
                if(line.isEmpty()) {errorMessage("Leerzeile eingegeben"); continue;}
                System.out.println("line: " + line);
                //alle möglichen Client-Anfragen überprüfen
                switch(line.charAt(0)) {
                case 'W': returnMessagesByTimestamp(line); break;
                case 'P': writeMessages(line); break;
                case 'T': returnMessagesByTopic(line); break;
                case 'L': returnListOfChangedTopics(line); break;
                case 'X': disconnet(line); break;
                default: errorMessage("Befehl unbekannt"); break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }		
		
	}
	
	//sendet Liste aller Nachrichten nach einem bestimmten Zeitpunkt an den Client
	private void returnMessagesByTimestamp(String line) throws IOException {
		System.out.println("retunrMessagesByTimestamp");
		//Anfrage-Format prüfen
		Pattern pattern = Pattern.compile("W (\\d+)");
	    Matcher matcher = pattern.matcher(line);
		if(!matcher.matches()) {errorMessage("Format inkorrekt"); return;}
		System.out.println("Format überprüft");
	    int timestamp = Integer.parseInt(matcher.group(1)); //spezifizierten Zeitpunkt aus der Anfrage einlesen
	    System.out.println("Timestamp ausgelesen");
	    List<Nachricht> result = server.getMessagesSince(timestamp); //Liste aller Nachrichten seit diesem Zeitpunkt anfordern
	    System.out.println("Liste aufgebaut");
	    writeMessageList(result); //Ergebnis an den Client schicken
	    System.out.println("Liste gesendet");
	}
	
	//speichert eine Liste von empfangenen Nachrichten
	private void writeMessages(String line) throws IOException {
		System.out.println("writeMessageMethode aufgerufen");
		//Anfrage-Format überprüfen
		if(!line.equals("P")) {errorMessage("Format inkorrekt"); return;}
		line = inputReader.readLine();
		if(!line.matches("\\d+")) {errorMessage("Format inkorrekt"); return;}
		int numOfMessages = Integer.parseInt(line); //Anzahl der zu speichernden Nachrichten auslesen
		System.out.println("numMessages: " + numOfMessages);
		//Nachrichten einlesen
		for(int i=0; i<numOfMessages; i++) {
			line = inputReader.readLine();
			if(!line.matches("\\d+")) {errorMessage("Format inkorrekt"); return;} //Format der ersten Nachrichten-Zeile überprüfen
			int numLines = Integer.parseInt(line); //Anzahl der Zeilen einlesen
			System.out.println("numLines: " + numLines);
			//Format der zweiten Nachrichten-Zeile überprüfen
			Pattern pattern = Pattern.compile("\\d+ (.+)");
			Matcher matcher = pattern.matcher(inputReader.readLine());
			if(!matcher.matches()) {errorMessage("Format inkorrekt"); return;}
			String topic = matcher.group(1); //Thema der Nachricht einlesen
			System.out.println("topic: " + topic);
			//Text der Nachricht einlesen
			String text = "";
			for(int j=0; j<numLines-1; j++) {
				text = text + inputReader.readLine() + "\n";
			}
			System.out.println("text: " + text);
			int timestamp = (int) (System.currentTimeMillis()/1000); //Timestamp erstellen
			System.out.println("timestamp: " + timestamp);
			server.addMessage(new Nachricht(numLines, timestamp, topic, text)); //neue Nachricht mit allen eingelesenen Daten speichern
		}	
	}
	
	//sendet eine Liste aller Nachrichten mit einem spezifizierten Thema an den Client
	private void returnMessagesByTopic(String line) throws IOException {
		//Anfrage-Format überprüfen
		Pattern pattern = Pattern.compile("T (.+)");
	    Matcher matcher = pattern.matcher(line);
		if(!matcher.matches()) {errorMessage("Format inkorrekt"); return;}
	    String topic = matcher.group(1); //Thema einlesen
	    List<Nachricht> result = server.getMessagesByTopic(topic); //Liste aller Nachrichten mit diesem Thema anfordern
	    writeMessageList(result); //Ergebnis an den Client schicken
	}
	
	//schickt eine Liste von Nachrichten an den Client
	private void writeMessageList(List<Nachricht> messageList) throws IOException {
		out.writeChars(Integer.toString(messageList.size())+"\n"); //Anzahl der Nachrichten schreiben
	    for (Nachricht n: messageList) {
	    	out.writeChars(Integer.toString(n.getNumLines())+"\n"); //Anzahl der Zeilen der Nachricht Schreiben
	    	out.writeChars(Integer.toString(n.getTimestamp())+" "+n.getTopic()+"\n"); //Timestamp und Thema der Nachricht schreiben
			out.writeChars(n.getText()); //Text der Nachricht schreiben
	    }
	}
	
	//sendet eine Liste der x zuletzt geänderten Themen an den Client
	private void returnListOfChangedTopics(String line) throws IOException {
		System.out.println("returnListOfChangedTopics Methode");
		int numTopics;
		//Anfrage-Format überprüfen
		Pattern pattern = Pattern.compile("L (\\d+)");
	    Matcher matcher = pattern.matcher(line);
	    System.out.println("line = " + line);
	    System.out.println("line.equals = " + line.equals("L"));
	    System.out.println("line matches = " + matcher.matches());
	    if(!(line.equals("L")||matcher.matches())) {errorMessage("Format inkorrekt"); return;}
		if(line.equals("L")) numTopics = server.nachrichten.size(); //die maximale Anzahl der Themen entspricht der Anzahl aller Nachrichten
		else numTopics = Integer.parseInt(matcher.group(1)); //Anzahl einlesen, falls vom Client angegeben
	    List<Nachricht> result = server.getMessagesByChangedTopic(numTopics); //Liste der Themen anfordern
	    //Liste schreiben
	    out.writeChars(Integer.toString(result.size())+"\n");
	    for (Nachricht n: result) {
	    	out.writeChars(Integer.toString(n.getTimestamp())+" "+n.getTopic()+"\n");
	    }			
	}
	
	//beendet die Verbindung
	private void disconnet(String line) throws IOException {
		if(line.equals("X")) {
			socket.close(); //Socket schließen
			server.closeConnection(this); //Client aus der Liste der Connections entfernen
			terminate();
			System.out.println("connection closed");
		} else {
			errorMessage("Format inkorrekt");
		}  	
	}
	
	private void errorMessage(String msg) throws IOException {
		out.writeChars("E " + msg + "\n"); //Fehlermeldung ausgeben
	}
	
	public void terminate() {
		terminate = true ;
	}

	//unbenutzt, evtl. streichen
	public int getNr() {
		return nr;
	}

	
}
