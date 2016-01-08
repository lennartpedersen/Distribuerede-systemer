
public class User {
	private String name;
	private Game game;
	
	public User(String name, Game game) {
		this.name = name;
		this.game = game;
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
}
