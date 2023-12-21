import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Search {
	
	static final WordList list = new WordList();
	private ArrayList<ArrayList<String>> paths;
	
	public Search() {
		
	}
	
	public Hashtable<String, ArrayList<String>> full_search(String startingWord) {
		// Sets the path variable to the paths from a given word to every 
		// other possible words
		
		this.paths = new ArrayList<>();
		this.paths.add(new ArrayList<String>());
		this.paths.get(0).add(startingWord);
		
		@SuppressWarnings("unchecked")
		ArrayList<String> remainingWords = (ArrayList<String>) list.wordArray.clone();
		
		Hashtable<String, ArrayList<String>> seenWords = new Hashtable<>();
		seenWords.put(startingWord, this.paths.get(0));
		
		while (!this.paths.isEmpty()) {
			// Extend the smallest path
			ArrayList<String> path = this.paths.remove(0);
			String lastWord = path.get(path.size() - 1);
			
			for (String connectedWord : Search.list.wordTable.get(lastWord)) {
				if (seenWords.containsKey(connectedWord)) {
					continue;
				}
				
				@SuppressWarnings("unchecked")
				ArrayList<String> newPath = (ArrayList<String>) path.clone();
				newPath.add(connectedWord);
				
				// Add the new path to the paths list
				this.paths.add(newPath);
				
				seenWords.put(connectedWord, newPath);
				remainingWords.remove(connectedWord);
			}
		}
		
		return seenWords;
	}
	
	public ArrayList<ArrayList<String>> findLongesPaths() {
		// Finds the longest paths
		
		int longestLength = 0;
		ArrayList<ArrayList<String>> longestPaths = new ArrayList<>();
		
		Iterator<String> graphWords = this.full_search("test").keys().asIterator();
		
		int count = 0;
		
		// For each word you need to check
		while (graphWords.hasNext()) {
			String word = graphWords.next();
			Iterator<ArrayList<String>> pathsFromWord = 
					this.full_search(word).elements().asIterator();
			
			while (pathsFromWord.hasNext()) {
				ArrayList<String> pathToCheck = pathsFromWord.next();
				
				if (pathToCheck.size() >= longestLength) {
					if (pathToCheck.size() > longestLength) {
						// Reset the longest length path if you've found a longer one
						longestLength = pathToCheck.size();
						longestPaths = new ArrayList<>();
					}
				
					longestPaths.add(pathToCheck);
				}
			}
			
			count++;
			if (count % 10 == 0)
			System.out.println(Integer.toString(count) + ":\t" + word);
		}
		
		return longestPaths;
	}
	
	public static void main(String[] args) {
		Search s = new Search();
		
		if (args.length == 0)
			// Find the longest optimal path between two words
			for (ArrayList<String> path : s.findLongesPaths()) {
				System.out.println(path);
			}
		
		if (args.length == 1) {
			long start = System.nanoTime();
			
			for (int i = 0; i < 100; i++) {
				s.full_search("test");
			}
			
			long end = System.nanoTime();
			System.out.println("Time(ns) : " + String.valueOf(end - start));
		}
		
		else if (args.length == 2) {
			for (String word : args) {
				// Check that
				if (!Search.list.is_word(word)) {
					System.err.println(word + " is not in my word list!");
					System.exit(1);
				}
			}
			
			Hashtable<String, ArrayList<String>> paths = s.full_search(args[0]);
			if (!paths.containsKey(args[1])) {
				System.err.println("Can't find " + args[1] + " from " + args[0] + "!");
				System.exit(1);
			}
			
			ArrayList<String> path = paths.get(args[1]);
			System.out.println("Length :" + Integer.toString(path.size() - 1));
			System.out.println("  Path :" + path.toString());
		}
	}

}
