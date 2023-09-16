package com.contrabass.nerdlebase;

public record Guess(String guess, String feedback) {

    // Feedback code:
    // G = green
    // Y = yellow
    // B = blank

    @Override
    public String toString() {
        return String.format("{%s -> %s}", guess, feedback);
    }
}
