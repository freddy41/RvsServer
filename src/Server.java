import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server 
{
	public static ArrayList<Nachricht> nachrichten;

	public Server() {
		
		ConnectionHandler handler = new ConnectionHandler(this);// verwaltet die client threads /
		nachrichten = new ArrayList<Nachricht>();				// bekommt this übergeben und gibt das objekt an 
																//die clientthreads  weiter damit diese auch addmessage
																//ausführen können 
		handler.start();										// startet handle.run()
	}
	// hier gibt es potenziell Problem weil mehrere threads auf nachrichten zugreifen. 
	public void addMessage(Nachricht n)
	{
		nachrichten.add(n);
	}
	public synchronized ArrayList<Nachricht> getMessagesSince(Date d)// returnt eine liste mit Nachrichten nach dem Datum
	{																 // bisher noch nicht verwendet 
		ArrayList<Nachricht> erg=new ArrayList<Nachricht>();
		for (Nachricht n: nachrichten) {
		    if(n.getDate().after(d))
		    	erg.add(n);
		}
		return erg;
	}
}
