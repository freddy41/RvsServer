import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientThread extends Thread {
	private Socket socket;
	private final int nr;
	private Server server;
	private boolean terminate;
	public ClientThread(Socket so, int n , Server s)
	{	this.terminate=false;
		this.server= s;
		this.socket=so;
		this.nr = n;
	}
	public void run(){
		
        InputStream input = null;
        BufferedReader inputReader = null;
        DataOutputStream out = null;
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
                if (line.equals("exit") ) { 		// verbindungsabruchbedingung
                    socket.close();
                    return;
                } else {					// hir müsste man noch die nachricht zusammenstellen und ein neues
                							// message objekt basteln 
                	server.addMessage(new Nachricht(1,  new java.util.Date(),"thema","textthread"));// nur zum testen 
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
		
		
	}
	public void terminate()
	{
		terminate = true ;
	}

	public int getNr() {
		return nr;
	}

	
}
