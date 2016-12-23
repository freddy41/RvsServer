import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread {
	private Socket socket;
	private Server server;
	private boolean terminate;
	private InputStream input;
	private BufferedReader inputReader;
	private DataOutputStream out;

	public ClientThread(Socket so, Server s) {
		this.terminate = false;
		this.server = s;
		this.socket = so;
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
		while (!terminate) {
			try {
				line = inputReader.readLine();
				if (line == null) {
					disconnet("X");
					return;
				}
				if (line.isEmpty()) {
					errorMessage("Leerzeile eingegeben");
					continue;
				}
				System.out.println("line: " + line);
				// alle möglichen Client-Anfragen überprüfen
				switch (line.charAt(0)) {
				case 'W':
					returnMessagesByTimestamp(line);
					break;
				case 'P':
					readMessages(line);
					break;
				case 'T':
					returnMessagesByTopic(line);
					break;
				case 'L':
					returnListOfChangedTopics(line);
					break;
				case 'X':
					disconnet(line);
					break;
				default:
					errorMessage("Befehl unbekannt");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

	}

	// sendet Liste aller Nachrichten nach einem bestimmten Zeitpunkt an den
	// Client
	private void returnMessagesByTimestamp(String line) throws IOException {
		System.out.println("entered returnMessagesByTimestamp-method");
		// Anfrage-Format prüfen
		Pattern pattern = Pattern.compile("W (\\d+)");
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			errorMessage("Format inkorrekt");
			return;
		}
		// spezifizierten Zeitpunkt aus der Anfrage einlesen
		int timestamp = Integer.parseInt(matcher.group(1));
		// Liste aller Nachrichten seit diesem Zeitpunkt anfordern
		List<Nachricht> result = server.getMessagesSince(timestamp);
		writeMessageList(result); // Ergebnis an den Client schicken
	}

	// speichert eine Liste von empfangenen Nachrichten
	private void readMessages(String line) throws IOException {
		System.out.println("entered readMessages-method");
		// Anfrage-Format überprüfen
		if (!line.equals("P")) {
			errorMessage("Format inkorrekt");
			return;
		}
		line = inputReader.readLine();
		if (!line.matches("\\d+")) {
			errorMessage("Format inkorrekt");
			return;
		}
		// Anzahl der zu speichernden Nachrichten auslesen
		int numOfMessages = Integer.parseInt(line);
		System.out.println("numMessages: " + numOfMessages);
		List<Nachricht> newMessages = new ArrayList<Nachricht>();
		// Nachrichten einlesen
		for (int i = 0; i < numOfMessages; i++) {
			line = inputReader.readLine();
			if (!line.matches("\\d+")) {
				errorMessage("Format inkorrekt");
				return;
			} // Format der ersten Nachrichten-Zeile überprüfen
			int numLines = Integer.parseInt(line); // Anzahl der Zeilen einlesen
			System.out.println("numLines: " + numLines);
			// Format der zweiten Nachrichten-Zeile überprüfen
			Pattern pattern = Pattern.compile("\\d+ (.+)");
			Matcher matcher = pattern.matcher(inputReader.readLine());
			if (!matcher.matches()) {
				errorMessage("Format inkorrekt");
				return;
			}
			String topic = matcher.group(1); // Thema der Nachricht einlesen
			System.out.println("topic: " + topic);
			// Text der Nachricht einlesen
			String text = "";
			for (int j = 0; j < numLines - 1; j++) {
				text = text + inputReader.readLine() + "\n";
			}
			System.out.println("text: " + text);
			// Timestamp erstellen
			int timestamp = (int) (System.currentTimeMillis() / 1000);
			System.out.println("timestamp: " + timestamp);
			Nachricht newMessage = new Nachricht(numLines, timestamp, topic, text);
			server.addMessage(newMessage);
			newMessages.add(newMessage);
		}
		server.sendNewMessagesToAll(newMessages);
	}

	// sendet eine Liste aller Nachrichten mit einem spezifizierten Thema an den
	// Client
	private void returnMessagesByTopic(String line) throws IOException {
		System.out.println("entered returnMessagesByTopic-method");
		// Anfrage-Format überprüfen
		Pattern pattern = Pattern.compile("T (.+)");
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			errorMessage("Format inkorrekt");
			return;
		}
		String topic = matcher.group(1); // Thema einlesen
		// Liste aller Nachrichten mit diesem Thema anfordern
		List<Nachricht> result = server.getMessagesByTopic(topic);
		writeMessageList(result); // Ergebnis an den Client schicken
	}

	// schickt eine Liste von Nachrichten an den Client
	private void writeMessageList(List<Nachricht> messageList) throws IOException {
		System.out.println("entered writeMessageList-method");
		synchronized (out) {
			// Anzahl der Nachrichten schreiben
			out.writeChars(Integer.toString(messageList.size()) + "\n");
			for (Nachricht n : messageList) {
				// Anzahl der Zeilen der Nachricht Schreiben
				out.writeChars(Integer.toString(n.getNumLines()) + "\n");
				// Timestamp und Thema der Nachricht schreiben
				out.writeChars(Integer.toString(n.getTimestamp()) + " " + n.getTopic() + "\n");
				out.writeChars(n.getText()); // Text der Nachricht schreiben
			}
		}
	}

	// sendet eine Liste der x zuletzt geänderten Themen an den Client
	private void returnListOfChangedTopics(String line) throws IOException {
		System.out.println("entered returnListOfChangedTopics-method");
		int numTopics;
		// Anfrage-Format überprüfen
		Pattern pattern = Pattern.compile("L (\\d+)");
		Matcher matcher = pattern.matcher(line);
		if (!(line.equals("L") || matcher.matches())) {
			errorMessage("Format inkorrekt");
			return;
		}
		if (line.equals("L"))
			// die maximale Anzahl der Themen entspricht der Anzahl aller
			// Nachrichten
			numTopics = server.getNachrichtenSize();
		else
			// Anzahl einlesen, falls vom Client angegeben
			numTopics = Integer.parseInt(matcher.group(1));
		// Liste der Themen anfordern
		List<Nachricht> result = server.getMessagesByChangedTopic(numTopics);
		// Liste schreiben
		synchronized (out) {
			out.writeChars(Integer.toString(result.size()) + "\n");
			for (Nachricht n : result) {
				out.writeChars(Integer.toString(n.getTimestamp()) + " " + n.getTopic() + "\n");
			}
		}
	}

	// beendet die Verbindung
	private void disconnet(String line) throws IOException {
		System.out.println("entered disconnet-method");
		if (line.equals("X")) {
			socket.close(); // Socket schließen
			// Client aus der Liste der Connections entfernen
			server.closeConnection(this);
			terminate();
			System.out.println("connection closed");
		} else {
			errorMessage("Format inkorrekt");
		}
	}

	private void errorMessage(String msg) throws IOException {
		System.out.println("entered errorMessage-method");
		synchronized (out) {
			out.writeChars("E " + msg + "\n"); // Fehlermeldung ausgeben
		}
	}

	public void terminate() {
		System.out.println("entered terminate-method");
		terminate = true;
	}

	public void writeToClient(String string) {
		System.out.println("entered writeToClient-method");
		try {
			synchronized (out) {
				out.writeChars(string);
			}
		} catch (IOException e) {
			e.printStackTrace(); // Client vermutlich disconnected
		}
	}

}
