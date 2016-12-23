import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server {
	private static List<Nachricht> nachrichten; // verwaltet die Nachrichten
	public static ConnectionHandler handler; // verwaltet die ClientThreads

	public static void main(String[] args) {

		Server s = new Server();
	}

	public Server() {

		handler = new ConnectionHandler(this);
		// Server an Connection Handler übergeben
		nachrichten = Collections.synchronizedList(new ArrayList<Nachricht>());

		handler.start(); // startet handler.run()
	}

	// fügt eine neue Nachricht der Liste hinzu
	public void addMessage(Nachricht n) {
		nachrichten.add(n);
	}

	// gibt eine Liste aller Nachrichten ab einem spezifizierten Zeitpunkt
	// zurück
	public List<Nachricht> getMessagesSince(int timestamp) {
		List<Nachricht> result = new ArrayList<Nachricht>();
		synchronized (nachrichten) {
			for (Nachricht n : nachrichten) {
				if (n.getTimestamp() >= timestamp)
					result.add(n);
			}
		}
		return result;
	}

	// gibt eine Liste aller Nachrichten zu einem bestimmten Thema zurück
	public List<Nachricht> getMessagesByTopic(String topic) {
		List<Nachricht> result = new ArrayList<Nachricht>();
		synchronized (nachrichten) {
			for (Nachricht n : nachrichten) {
				if (n.getTopic().equals(topic))
					result.add(n);
			}
		}
		// erstellte Liste nach timestamp sortieren
		result.sort((Nachricht o1, Nachricht o2) -> {
			return o2.getTimestamp() - o1.getTimestamp();
		});
		return result;
	}

	// gibt eine Liste von Nachrichten zurück, die von den <numTopicc> zuletzt
	// geänderten Themen die jeweils jüngste Nachricht enthält.
	public List<Nachricht> getMessagesByChangedTopic(int numTopics) {

		List<Nachricht> result = new ArrayList<Nachricht>();
		// speichert alle bereits enthaltenen Themen
		Set<String> containedTopics = new HashSet<String>();
		// alle Nachrichten nach timestamp sortieren
		synchronized (nachrichten) {
			nachrichten.sort((Nachricht o1, Nachricht o2) -> {
				return o2.getTimestamp() - o1.getTimestamp();
			});

			for (int i = 0; i < nachrichten.size(); i++) {
				if (!containedTopics.contains(nachrichten.get(i).getTopic())) {
					result.add(nachrichten.get(i));
					containedTopics.add(nachrichten.get(i).getTopic());
					// überprüfen, ob die gewünschte Anzahl von geänderten
					// Themen erreicht wurde
					if (containedTopics.size() >= numTopics)
						break;
				}
			}
		}
		return result;
	}

	// entfernt ClientThread aus der Liste der Connections
	public void closeConnection(ClientThread clientThread) {
		handler.removeClientThread(clientThread);
	}

	// gibt Anzahl der gespeicherten Nachrichten zurück
	public int getNachrichtenSize() {
		return nachrichten.size();
	}

	// schickt neue Nachrichten an alle Clients
	public void sendNewMessagesToAll(List<Nachricht> newMessages) {
		handler.sendNewMessagesToAll(newMessages);
	}

}
