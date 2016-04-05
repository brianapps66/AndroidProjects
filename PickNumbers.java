package com.parent.luckynumbers;

import java.util.ArrayList;
import java.util.Collections;

public class PickNumbers {
	ArrayList<Integer> result = new ArrayList<Integer>();
	ArrayList<Integer> range = new ArrayList<Integer>();

	PickNumbers(int x, int y) {

		for (int i = 1; i <= x; i++) {
			range.add(i); // Makes array from 1 to x
		}

		System.out.println(range.toString());
		
		for (int i = 0; i < y; i++) {
			int a =  (int) (Math.random() * ((range.size())));
			result.add(range.get(a));
			range.remove(a);
		}

		Collections.sort(result);
		//return;
	}
}
