package com.litbikes.util;

import java.util.concurrent.ThreadLocalRandom;

public class NumberUtil {

	public static int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
	
}
