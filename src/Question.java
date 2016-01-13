import java.io.Serializable;

public class Question implements Serializable{

	private static final long serialVersionUID = 7584983293412403367L;
	
	private String question, answer;
	
	public Question(String question, String answer) {
		this.question = question;
		this.answer = answer;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	
}
