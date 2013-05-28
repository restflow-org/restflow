package org.restflow.beans;

import java.util.Random;

public class SlowAdder {

	public int a, b, c, d;

	public void step() throws InterruptedException {
        d = c + a + b;
        Random rand = new Random();
        int delay = rand.nextInt(30);
        Thread.sleep(delay);
	}
}