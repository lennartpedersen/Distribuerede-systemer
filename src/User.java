
public class User {
	private String name;
	private Game game;
	private int score;
	
	public User(String name) {
		this.name = name;
		score=0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public int getScore() {
		return score;
	}
	
	public void incrementScore(int i){
		score+=i;
	}
}
