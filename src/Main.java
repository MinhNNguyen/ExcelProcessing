import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Main {
	
	// Regular expression to check the cell name in the format of a number following
	// a character. Example: A1, a1, b10, B10
	private static final String CELLNAME_REGEX = "^[a-zA-Z]{1}[0-9]+$";
	private static final String INT_REGEX = "^[0-9]+$";

	public static void main(String[] args) {
		
		// Declaration of map variables
		
		HashMap<String, String> map = new HashMap<>();
	    HashMap<String, Integer> values = new HashMap<>();
	    HashMap<String, String> dependency = new HashMap<>();
	    HashMap<String, ArrayList<String>> requiredMap = new HashMap<>();
		
		// Reading excel input from stdin
		
	    Scanner sc = new Scanner(System.in);
	    int numLines = sc.nextInt();
	    sc.nextLine();
	    for ( int i = 0; i < numLines; i++) {
		      String cell = sc.nextLine();
		      String cellContent = sc.nextLine();
		      map.put(cell, cellContent);
		    }
		sc.close();
	    
	    // Differentiating between cell that has number and cells 
		// that contains formula involves values of other cells
		
	    for ( String key: map.keySet()) {
	    	String cellContent = map.get(key);
	    	if (cellContent.matches("INT_REGEX")) {
	    		values.put( key, Integer.parseInt(cellContent));
	    	}
	    	else {
	    		String[] words = cellContent.split(" ");
	    		ArrayList<String> requiredList = new ArrayList<String>();
	    		for (String word: words) {
	    			if ( word.matches(CELLNAME_REGEX)) {
	    				requiredList.add(word);
	    			}
	    		}
	    		requiredMap.put(key, requiredList);
	    	}
	    }
	    
	    // Processing each cell, finding their values or if they have circular dependency 
	    
	    HashSet<String> processingList = new HashSet<String>();
	    for ( String key: map.keySet()) {
	    	findValueDFS(map, values, requiredMap, dependency, processingList, key);
	    }
	    
	    String[] cells = map.keySet().toArray(new String[map.size()]);
	    Arrays.sort(cells);
	    
	    for (String cell : cells) {
	    	if (values.containsKey(cell))
	    		System.out.println(cell + ": " + values.get(cell));
	    	else if (dependency.containsKey(cell)) 
	    		System.out.println(cell + ": " + dependency.get(cell));
	    }
	}
	
	/*
	 * Using depth first search to find out if there is any circular dependency
	 * in the existing grid. Then calculate the value of those grids who does
	 * not have any circular dependency
	 */
	static void findValueDFS( HashMap<String, String> map,
			HashMap<String, Integer> values, HashMap<String,
			ArrayList<String>> requiredMap, HashMap<String, String> dependency,
			HashSet<String> processingList, String key) {
		if ( values.containsKey(key))
			return;
		
		if ( processingList.contains(key)) {
			String output = "Circular dependency detected between";
			for (String elem: processingList) {
				output += " " + elem;
			}
			for (String elem: processingList) {
				dependency.put(elem, output);
			}
			return;
		}
		
		processingList.add(key);
		for (String requiredKey : requiredMap.get(key)) {
			findValueDFS( map, values, requiredMap,
					dependency, processingList, requiredKey);
		}
		
		// calculate using requiredKey and formula
		if (!dependency.containsKey(key))
			values.put(key, calculateValue(map, values, key));
			
		processingList.remove(key);
	}
	
	/**
	 * Function to calculate the value of the excel grid based on the formula
	 * and values of other cells
	 * @param map HashMap that contains the cell name and its corresponding input
	 * @param values HashMap that contains all values available from other cell
	 * @param key the key of the cell to be calculated
	 * @return the value of the cell that is corresponding to key
	 */
	static int calculateValue( HashMap<String, String> map,
			HashMap<String, Integer> values, String key) {
		String cellDetail = map.get(key);
		int firstNum = 0, secondNum = 0;
		String[] words = cellDetail.split(" ");
		if ( words.length == 0 )
			return 0;
		if (words[0].matches(INT_REGEX))
			firstNum = Integer.parseInt(words[0]);
		else
			firstNum = values.get(words[0]);
			
		/** The previous calculation will have the first
		 * element being the result then if the forumla has not ended, it will
		 *  use the result of the previous calculation in place of first number
		 *  and keep calculating like normal
		 */
		
		for ( int index = 1; index < words.length; index+=2 ) {
			if (words[index].matches(INT_REGEX))
				secondNum = Integer.parseInt(words[index]);
			else
				secondNum = values.get(words[index]);
			
			// Checking what operator is in used
			switch(words[index + 1]) {
				case "+":
					firstNum += secondNum;
					break;
				case "-":
					firstNum -= secondNum;
					break;
				case "/":
					firstNum /= secondNum;
					break;
				case "*":
					firstNum *= secondNum;
					break;
				default:
					System.out.println("Error in calculating value of cell");
			}
		}
		return firstNum;
	}
	
}
