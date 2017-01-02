import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionHandler extends Thread {
	private final int port = 12345;
	private ServerSocket ssocket = null;
	private Socket socket = null;
	private Server server;
	private List<ClientThread> connections; // Liste alle verbundenen Clients

	public ConnectionHandler(Server s) {
		this.server = s;
		this.connections = Collections.synchronizedList(new ArrayList<ClientThread>());
	}

	public void run() {

		try {
			ssocket = new ServerSocket(); // serversocket aufsetzen
			ssocket.bind(new InetSocketAddress(port));

		} catch (IOException e) {
			try {
				ssocket.close();		//sicher ist sicher koennte ja noch offen sein 
			} catch (IOException e1) {
				e1.printStackTrace();
				
			}
			System.err.println("Failed to create socket at port:"+ port );
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				socket = ssocket.accept(); // wartet auf neue Verbinungen
			} catch (IOException e) {
				System.err.println("Error creating thread nr " + connections.size());
			}

			ClientThread clientThread = new ClientThread(socket, server);
			clientThread.start();
			connections.add(clientThread); // neuer Thread fuer jede verbindung
			System.out.println("Anzahl der Connections: " + connections.size());

		}
	}

	// entfernt Client aus der Liste der Connections
	public void removeClientThread(ClientThread clientThread) {
		connections.remove(clientThread);
	}

	// schickt eine Liste der neuen Nachrichten an alle Clients
	public void sendNewMessagesToAll(List<Nachricht> newMessages) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("N " + newMessages.size() + "\n");
		// alle neuen Nachrichten zu einem String konkatenieren
		for (Nachricht message : newMessages) {
			stringBuilder.append(message.getTimestamp() + " " + message.getTopic() + "\n");
		}
		String newMessageString = stringBuilder.toString();
		// String an alle Clients schicken
		synchronized (connections) {
			for (ClientThread connection : connections) {
				connection.writeToClient(newMessageString);
			}
		}
	}

}
