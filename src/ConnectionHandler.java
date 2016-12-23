import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionHandler extends Thread {
	private  ServerSocket ssocket = null;
	private Socket socket = null;
	private Server server;
	private List<ClientThread> connections ;
	public ConnectionHandler(Server s) {
		this.server=s;
		this.connections = Collections.synchronizedList(new ArrayList<ClientThread>());
	} 
	
	
	public void run()
	 {

			try {	
				ssocket = new ServerSocket();					//serversocket aufsetzen 
				ssocket.bind(new InetSocketAddress(12345));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.exit(-1);
				e.printStackTrace();
			}
			
			
			while(true)
			{
				try {
	                socket = ssocket.accept(); 				// wartet auf neue Verbinungen 
	            } catch (IOException e) {
	                System.out.println("Error creating thread nr " +connections.size() );
	            }
				
				ClientThread clientThread = new ClientThread(socket, server);
				clientThread.start();     
	            connections.add(clientThread);	// neuer Thread  f�r jede verbindung 
	            System.out.println("Anzahl der Connections: " + connections.size());
	           		
			}
	 }
	
	public void removeClientThread(ClientThread clientThread) {
		connections.remove(clientThread);
	}


	public void sendNewMessagesToAll(List<Nachricht> newMessages) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("N " + newMessages.size() +"\n");
		for(Nachricht message : newMessages) {
			stringBuilder.append(message.getTimestamp() + " " + message.getTopic() + "\n");
		}
		String newMessageString = stringBuilder.toString();
		synchronized (connections) {
			for(ClientThread connection : connections) {
				connection.writeToClient(newMessageString);
			}
		}
	}

}
