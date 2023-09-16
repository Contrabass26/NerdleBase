package com.contrabass.nerdlebase;

import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NerdleTest {

    @Test void generationAndSorting() {
        // Generation
        Set<String> possibilities = Nerdle.getPossibilities(8, new Guess("48-32=16", "YBBYBGBB"));
        assertEquals(54, possibilities.size());
        // Sorting
        Map.Entry<String, Double> best = Nerdle.sortPossibilities(possibilities).get(0);
        assertEquals("39+54=93", best.getKey());
        assertEquals(5.15, Precision.round(best.getValue(), 2));
        System.out.println();
    }
}
