import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server 
{
	private static List<Nachricht> nachrichten;
	public static ConnectionHandler handler; // verwaltet die client threads
	public static void main(String[] args) {
		
		Server s = new Server();
	}

	public Server() {
		
		handler = new ConnectionHandler(this);
		nachrichten = Collections.synchronizedList(new ArrayList<Nachricht>()); //Server an Connection Handler übergeben
												
		handler.start(); //startet handler.run()
	}
	// hier gibt es potenziell Problem weil mehrere threads auf nachrichten zugreifen. 
	public void addMessage(Nachricht n) {
		nachrichten.add(n);
	}
	
	public List<Nachricht> getMessagesSince(int timestamp)// returnt eine liste mit Nachrichten nach dem Datum
	{																 // bisher noch nicht verwendet 
		List<Nachricht> result =new ArrayList<Nachricht>();
		synchronized (nachrichten) {
			for (Nachricht n: nachrichten) {
			    if(n.getTimestamp()>=timestamp) result.add(n);
			}
		}
		return result;
	}
	
	public List<Nachricht> getMessagesByTopic(String topic) {
		List<Nachricht> result = new ArrayList<Nachricht>();
		synchronized (nachrichten) {
			for (Nachricht n: nachrichten) {
			    if(n.getTopic().equals(topic)) result.add(n);
			}
		}
		result.sort((Nachricht o1, Nachricht o2) -> {
			return o2.getTimestamp()-o1.getTimestamp();
		});
		return result;
	}
	
	public List<Nachricht> getMessagesByChangedTopic(int numTopics) {
		
		List<Nachricht> result = new ArrayList<Nachricht>();
		Set<String> containedTopics = new HashSet<String>();
		
		synchronized (nachrichten) {
			nachrichten.sort((Nachricht o1, Nachricht o2) -> {
				return o2.getTimestamp()-o1.getTimestamp();
			});
						
			for(int i=0; i<nachrichten.size(); i++) {
				if(!containedTopics.contains(nachrichten.get(i).getTopic())) {
					result.add(nachrichten.get(i));
					containedTopics.add(nachrichten.get(i).getTopic());
					if(containedTopics.size()>=numTopics) break;
				}		
			}
		}
		return result;
	}
	
	public void closeConnection(ClientThread clientThread) {
		handler.removeClientThread(clientThread);
	}
	
	public int getNachrichtenSize() {
		return nachrichten.size();
	}

	public void sendNewMessagesToAll(List<Nachricht> newMessages) {
		handler.sendNewMessagesToAll(newMessages);		
	}
	
	
}
