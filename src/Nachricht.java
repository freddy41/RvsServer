

public class Nachricht {
	private int numLines ;
	private int timestamp;
	private String topic;
	private String text;
	
	public Nachricht(int z, int ti, String to, String te)
	{
		this.numLines = z;
		this.timestamp = ti;
		this.topic= to;
		this.text= te;
		
	}

	public int getNumLines() {
		return numLines;
	}

	public void setNumLines(int zeilenzahl) {
		this.numLines = zeilenzahl;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	
}
