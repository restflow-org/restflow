package org.restflow.beans;

import java.util.Random;

public class SlowMultiplier {

	public int a, b, c;

	public void step() throws InterruptedException {
      c = a * b;
      Random rand = new Random();
      int delay = rand.nextInt(30);
      System.err.println(delay);
      Thread.sleep(delay);
	}
	
}