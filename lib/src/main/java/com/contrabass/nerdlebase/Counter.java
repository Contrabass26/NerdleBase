package com.contrabass.nerdlebase;

import java.util.Arrays;

public class Counter {

    protected final int[] elements;
    protected final int[] maximums;
    protected boolean done = false;
    protected double iterations = 0;

    public Counter(int[] maximums) {
        elements = new int[maximums.length];
        Arrays.fill(elements, 0);
        this.maximums = maximums;
    }

    public void increment() {
        iterations++;
        for (int i = elements.length - 1; i >= 0; i--) {
            elements[i]++;
            if (elements[i] == Math.abs(maximums[i])) {
                elements[i] = 0;
            } else {
                return;
            }
        }
        done = true;
    }

    public String substitute(String[] charSets) throws StringIndexOutOfBoundsException {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            try {
                string.append(charSets[i].charAt(elements[i]));
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                System.out.println("charSets[i] = " + charSets[i]);
                System.out.println("elements[i] = " + elements[i]);
                System.exit(1);
            }
        }
        return string.toString();
    }

    public boolean isRunning() {
        return !done;
    }
}
