import java.io.Serializable;

public class Score implements Serializable{

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
