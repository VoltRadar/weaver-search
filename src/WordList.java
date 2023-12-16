// Word list source: https://www.diginoodles.com/projects/eowl

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Hashtable;

public class WordList {
	ArrayList<String> wordArray;
	private Hashtable<String, Boolean> wordTable;
	
	public WordList() {
		this.wordArray = new ArrayList<>();
		this.wordTable = new Hashtable<>();
		this.read_file();
		this.create_table();
	}
	
	public void read_file() {
		// Open and load the words into the word array
		File wordsFile = new File("weaver_words.txt");
		
		try {
			Scanner wordsReader = new Scanner(wordsFile);
			
			while (wordsReader.hasNextLine()) {
				this.wordArray.add(wordsReader.nextLine());
			}
			
			wordsReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.print("Words list not found!");
			System.exit(1);
		}
	}
	
	public void create_table() {
		// Creates the Hashtable of the words used. Value of Hash table
		// entries can be ignored
		for (String word : this.wordArray) {
			this.wordTable.put(word, true);
		}
	}
	
	public Boolean is_word(String word) {
		// Returns if word is in the word list
		return this.wordTable.containsKey(word);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		WordList list = new WordList();
		
		System.out.println(list.is_word("null"));
		System.out.println(list.is_word("abba"));
		System.out.println(list.is_word("abcd"));
		System.out.println(list.is_word("aaaa"));
	}
}
