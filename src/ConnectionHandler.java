import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionHandler extends Thread {
	private  ServerSocket ssocket = null;
	private Socket socket = null;
	private int threadnr=0;
	private Server server;
	private ArrayList<ClientThread> connections ;
	public ConnectionHandler(Server s) {
		this.server=s;
		this.connections = new ArrayList<ClientThread>();
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
	           
	            connections.add(new ClientThread(socket, threadnr, server));	// neuer Thread  f�r jede verbindung 
	            connections.get(connections.size()-1).start();			// neuen Thread starten 
	            threadnr++;
	            System.out.println(connections.size());
	           
			
			}
	 }
	public ArrayList<ClientThread> getTconnections()
	{
		return connections;
	}
	
	public void removeClientThread(ClientThread clientThread) {
		connections.remove(clientThread);
	}

}
