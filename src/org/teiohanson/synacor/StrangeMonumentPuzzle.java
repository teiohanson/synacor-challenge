package org.teiohanson.synacor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StrangeMonumentPuzzle {
	
	static final Logger logger = LogManager.getLogger(StrangeMonumentPuzzle.class);
	
	public static final Map<Integer, String> coins = new HashMap<Integer, String>();
	static {
		coins.put(2, "red");
		coins.put(3, "corroded");
		coins.put(5, "shiny");
		coins.put(7, "concave");
		coins.put(9, "blue");
	}
	
	public static void solve() {
		StringBuilder sbCoinOrder = new StringBuilder();
		List<Integer[]> coinPermutations = new ArrayList<Integer[]>(); 
		
		permutate(coins.keySet().toArray(new Integer[0]), 0, coinPermutations);
		for(Integer[] permutation : coinPermutations) {
			if(permutation[0] + permutation[1] * Math.pow(permutation[2], 2) + Math.pow(permutation[3], 3) - permutation[4] == 399) {
				for(Integer integer : permutation) {
					sbCoinOrder.append(coins.get(integer) + " ");
				}
				logger.info("Coin Order: " + sbCoinOrder.toString());
			}
		}
	}
	
	private static void permutate(Integer[] array, int index, List<Integer[]> permutationList) {
		if(index == array.length) {
			permutationList.add(array);
		}
		
		for(int i = index; i < array.length; i++) {
			Integer[] permutation = array.clone();
			permutation[index] = array[i];
			permutation[i] = array[index];
			permutate(permutation, index + 1, permutationList);
		}
	}
}
