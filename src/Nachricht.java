import java.util.Date;

public class Nachricht {
	private int zeilenzahl ;
	private Date date;
	private String thema;
	private String text;
	
	public Nachricht(int z, Date d, String th, String te)
	{
		this.zeilenzahl = z;
		this.date = d;
		this.thema= th;
		this.text= te;
		
	}

	public int getZeilenzahl() {
		return zeilenzahl;
	}

	public void setZeilenzahl(int zeilenzahl) {
		this.zeilenzahl = zeilenzahl;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getThema() {
		return thema;
	}

	public void setThema(String thema) {
		this.thema = thema;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	
}
