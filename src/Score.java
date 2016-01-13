import java.io.Serializable;

public class Score implements Serializable{

	private static final long serialVersionUID = 1381221520063759524L;
	private User user;
	private int value;
	
	public Score(User user, int value){
		this.user = user;
		this.value = value;
	}

	public User getUser() {
		return user;
	}

	public int getValue() {
		return value;
	}
	
	
}
