// Word list source: https://www.diginoodles.com/projects/eowl

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class WordList {
	private static String letters = "qwertyuiopasdfghjklzxcvbnm";
	private static String wordListName = "weaver_words_4.txt";
	
	ArrayList<String> wordArray;
	
	// Hash table mapping words to connected words
	Hashtable<String, ArrayList<String>> wordTable;
	
	public WordList() {
		this.wordArray = new ArrayList<>();
		this.wordTable = new Hashtable<>();
		
		this.read_file();
		
		this.create_table();
		this.make_connections();
	}
	
	private void read_file() {
		// Open and load the words into the word array
		
		
		// Find the word list
		String currentDir = System.getProperty("user.dir");
		Path wordListPath = FileSystems.getDefault().getPath(currentDir);
		
		while (wordListPath != null) {
			boolean found = false;
			
			for (File file : wordListPath.toFile().listFiles()) {
				if (file.toString().contains(WordList.wordListName)) {
					wordListPath = file.toPath();
					found = true;
					break;
				}
			}
			
			if (found) {
				break;
			} else {
				wordListPath = wordListPath.getParent();
			}
		}
		
		if (wordListPath == null) {
			System.err.println("Couldn't find words file!");
			System.exit(1);
		}
		
		File wordsFile = new File(wordListPath.toString());
		
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
	
	private ArrayList<String> connected_words(String word) {
		// Calculates the words differing by one letter from the input word
		
		ArrayList<String> connectedWords = new ArrayList<>();
		
		for (int letterIndex=0; letterIndex < word.length(); letterIndex++) {
			StringBuffer wordBuffer = new StringBuffer(word);
			
			for (int i=0; i < WordList.letters.length(); i++) {
				wordBuffer.deleteCharAt(letterIndex);
				wordBuffer.insert(letterIndex, WordList.letters.charAt(i));
				
				String newWord = wordBuffer.toString();
				if (this.is_word(newWord) && !word.equals(newWord))
					connectedWords.add(newWord);
			}
		}
		
		return connectedWords;
	}
	
	private void make_connections() {
		// Make all the connections of the graph
		
		Enumeration<String> words = this.wordTable.keys();
		while (words.hasMoreElements()) {
			String word = words.nextElement();
			this.wordTable.put(word, this.connected_words(word));
		}
	}
	
	private void create_table() {
		// Creates the Hash table of the words from wordsArray
		for (String word : this.wordArray) {
			this.wordTable.put(word, new ArrayList<String>());
		}
	}
	
	public Boolean is_word(String word) {
		// Returns if word is in the word list
		return this.wordTable.containsKey(word);
	}
	
	public void displayTable() {
		Iterator<Entry<String, ArrayList<String>>> graph = 
				this.wordTable.entrySet().iterator();
		
		while (graph.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = graph.next();
			
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		WordList list = new WordList();
		
		list.displayTable();
	}
}
