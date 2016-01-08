import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionDB {

	/*public static void main(String[] args) throws FileNotFoundException, IOException {
		List<Question> myList = getQuestions(5);
		for (Question q : myList) {
			System.out.println("Q: " + q.getQuestion());
			System.out.println("A: " + q.getAnswer() + "\n");
		}
	}*/
	
	/*
	 * Reservoir sampling algorithm for randomly choosing k samples from a list
	 * of n items, where n is either a very large or unknown number.
	 * - https://en.wikipedia.org/wiki/Reservoir_sampling
	 */

	protected List<Question> getQuestions(int quantity) {

		Random r = new Random();
		int count = 0, randomNumber = 0;
		
		List<Question> qaList = new ArrayList<Question>(quantity);
		File qFile = new File("Questions.txt"), 
			 aFile = new File("Answers.txt");
		String qLine = null, aLine = null;
        BufferedReader qIn, aIn;
        
		try {
			qIn = new BufferedReader(new FileReader(qFile));
			aIn = new BufferedReader(new FileReader(aFile));
			
			while ((qLine = qIn.readLine()) != null) {
					aLine = aIn.readLine();

				count++;

				if (count <= quantity) 
					qaList.add(new Question(qLine, aLine));
				
				else if ((randomNumber = (int) r.nextInt(count)) < quantity) 
					qaList.set(randomNumber, new Question(qLine, aLine));
			}
			qIn.close();
			aIn.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return qaList;
	}
}