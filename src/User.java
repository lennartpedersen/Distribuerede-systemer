import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = -7730630657091260564L;
	
	private String name;
	private Game game;
	private boolean spectator;
	
	public User(String name) {
		this.name = name;
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

	public boolean isSpectator() {
		return spectator;
	}

	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}
}
