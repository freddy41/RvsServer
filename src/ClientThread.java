import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
import java.util.ArrayList;

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
	
	public void run(){
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
                switch(line.charAt(0)) {
                case 'W': returnMessagesByTimestamp(line); break;
                case 'P': writeMessages(line); break;
                case 'T': returnMessagesByTopic(line); break;
                case 'L': returnListOfChangedTopics(line); break;
                case 'X': disconnet(line); break;
                default: invalidRequest(); break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }		
		
	}
	
	private void returnMessagesByTimestamp(String line) throws IOException{
		Pattern intAfterW = Pattern.compile("W (\\d+)");
	    Matcher matcher = intAfterW.matcher(line);
		if(matcher.matches()){
	        int timestamp = Integer.parseInt(matcher.group(1));
	        ArrayList<Nachricht> result = server.getMessagesSince(timestamp);
	        out.writeChars(Integer.toString(result.size())+"\n");
	        for (Nachricht n: result) {
			    out.writeChars(Integer.toString(n.getNumLines())+"\n");
			    out.writeChars(Integer.toString(n.getTimestamp())+" "+n.getTopic()+"\n");
			    out.writeChars(n.getText());
			}
		} else {
			invalidRequest();
		}
		
	}
	
	private void writeMessages(String line) throws IOException {
		System.out.println("writeMessageMethode aufgerufen");
		if(!line.matches("P")) {invalidRequest(); return;}
		line = inputReader.readLine();
		if(!line.matches("\\d+")) {invalidRequest(); return;}
		int numOfMessages = Integer.parseInt(line);
		System.out.println("numMessages: " + numOfMessages);
		for(int i=0; i<numOfMessages; i++) {
			line = inputReader.readLine();
			if(!line.matches("\\d+")) {invalidRequest(); return;}
			int numLines = Integer.parseInt(line);
			System.out.println("numLines: " + numLines);
			Pattern pattern = Pattern.compile("\\d+ (.+)");
			Matcher matcher = pattern.matcher(inputReader.readLine());
			if(!matcher.matches()) {invalidRequest(); return;}
			String topic = matcher.group(1);
			System.out.println("topic: " + topic);
			String text = "";
			for(int j=0; j<numLines-1; j++) {
				text = text + inputReader.readLine() + "\n";
			}
			System.out.println("text: " + text);
			int timestamp = (int) (System.currentTimeMillis()/1000);
			System.out.println("timestamp: " + timestamp);
			server.addMessage(new Nachricht(numLines, timestamp, topic, text));
		}	
	}
	
	private void returnMessagesByTopic(String line) {
		// TODO Auto-generated method stub
		
	}
	private void returnListOfChangedTopics(String line) {
		// TODO Auto-generated method stub
		
	}
	private void disconnet(String line) throws IOException {
		if(line.matches("X")) {
			socket.close();
			System.out.println("connection closed"); //Test-Ausgabe	
		} else {
			invalidRequest();
		}  	
	}
	private void invalidRequest() {
		// TODO Auto-generated method stub
		
	}
	
	//unbenutzt, evtl. streichen
	public void terminate()
	{
		terminate = true ;
	}

	public int getNr() {
		return nr;
	}

	
}
