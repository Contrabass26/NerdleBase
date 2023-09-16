package com.contrabass.nerdlebase;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Nerdle {

    private static final DoubleEvaluator DOUBLE_EVALUATOR = new DoubleEvaluator();

    private static double evaluate(String expression) {
        try {
            return DOUBLE_EVALUATOR.evaluate(expression);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }

    private static double getEntropy(String guess, Collection<String> possibilities) {
        FrequencyMap<String, String> frequencyMap = new FrequencyMap<>(possibilities) {
            @Override
            public void incorporate(String toIncorporate) {
                increment(getFeedback(toIncorporate, guess));
            }
        };
        return frequencyMap.frequencies().stream().mapToDouble(f -> {
            double probability = (double) f / possibilities.size();
            return probability * MathUtil.getInformation(probability);
        }).sum();
    }

    private static String getFeedback(String answer, String guess) {
        int length = answer.length();
        Map<Character, Integer> quota = new HashMap<>();
        char[] feedback = answer.toCharArray();
        for (int i = 0; i < feedback.length; ++i) {
            quota.merge(answer.charAt(i), 1, Integer::sum);
        }
        feedback = new char[length];
        Arrays.fill(feedback, ' ');
        for (int i = 0; i < length; ++i) {
            char c = guess.charAt(i);
            if (!quota.containsKey(c)) {
                feedback[i] = 'B';
            } else if (c == answer.charAt(i)) {
                feedback[i] = 'G';
                quota.merge(c, -1, Integer::sum);
            }
        }
        for (int i = 0; i < length; ++i) {
            char c = guess.charAt(i);
            if (!quota.containsKey(c) || feedback[i] != ' ') continue;
            if (quota.get(c) == 0) {
                feedback[i] = 'B';
            } else {
                feedback[i] = 'Y';
                quota.merge(c, -1, Integer::sum);
            }
        }
        return new String(feedback);
    }

    public static Set<String> getPossibilities(int length, Guess... guesses) {
        // Limitations
        String[] charPossibilities = getBaseCharPossibilities(length); // What the character at each index could be
        Set<Integer> greenOperatorCounts = new HashSet<>(); // How many operators in the right place there are in each guess
        List<Predicate<String>> charCountPredicates = new ArrayList<>();
        for (Guess guess : guesses) {
            if (guess == null) break; // No more guesses
            int greenOperatorCount = 0;
            // Modify charPossibilities based on feedback from previous guess
            Map<Character, Integer> minCharCounts = new HashMap<>(); // The minimum number of each character that there must be
            Map<Character, Boolean> capped = new HashMap<>(); // true if there can be no more of this character than its minCharCounts value
            Set<Character> uniqueGuessChars = new HashSet<>(); // All unique characters in the guess
            for (int i = 0; i < length; i++) {
                char guessChar = guess.guess().charAt(i);
                String guessCharStr = String.valueOf(guessChar);
                switch (guess.feedback().charAt(i)) {
                    case 'G' -> {
                        charPossibilities[i] = guessCharStr;
                        minCharCounts.merge(guessChar, 1, Integer::sum);
                        if (guessChar == '=') {
                            // No other characters can be an equals sign - modify charPossibilities accordingly
                            for (int j = 0; j < charPossibilities.length; j++) {
                                if (j != i) {
                                    charPossibilities[j] = charPossibilities[j].replace("=", "");
                                }
                            }
                        }
                        if ("+-*/".contains(guessCharStr)) {
                            greenOperatorCount++;
                        }
                    }
                    case 'Y' -> {
                        minCharCounts.merge(guessChar, 1, Integer::sum);
                        charPossibilities[i] = charPossibilities[i].replace(guessCharStr, ""); // Can't be this character at this index
                    }
                    case 'B' -> capped.put(guessChar, true); // No more of this character
                }
                uniqueGuessChars.add(guessChar);
            }
            // Get counts for each character
            for (char c : uniqueGuessChars) {
                int minCount = minCharCounts.getOrDefault(c, 0);
                boolean isCapped = capped.getOrDefault(c, false);
                if (minCount == 0 && isCapped) {
                    // None of this character in answer - remove from all charPossibilities
                    for (int i = 0; i < charPossibilities.length; i++) {
                        charPossibilities[i] = charPossibilities[i].replace(String.valueOf(c), "");
                    }
                } else {
                    // Add predicate to ensure that minCount is satisfied
                    charCountPredicates.add(s -> {
                        int count = StringUtil.count(s, c);
                        if (isCapped) {
                            // Must be exactly right
                            return count == minCount;
                        }
                        return count >= minCount;
                    });
                }
            }
            greenOperatorCounts.add(greenOperatorCount);
        }
        // Largest of the values for each guess (or 0)
        int minOperatorCount = greenOperatorCounts.size() == 0 ? 0 : Collections.max(greenOperatorCounts);
        List<Predicate<String>> predicates = getPredicates(charPossibilities);
        predicates.addAll(charCountPredicates);
        // Iterate through each equals position
        Set<String> possibilities = new HashSet<>();
        for (int equalsPos = 0; equalsPos < charPossibilities.length; equalsPos++) {
            // Continue if this index can't be an equals sign
            if (!charPossibilities[equalsPos].contains("=")) {
                continue;
            }
            // Operator configurations
            String[] configs = generateOperatorConfigurations(new int[0], equalsPos, length);
            for (String config : configs) {
                // Continue if it doesn't have enough operators;
                if (StringUtil.count(config, '+') < minOperatorCount) {
                    continue;
                }
                // Get operator positions
                List<Integer> operatorPositions = getOperatorPositions(charPossibilities, equalsPos, config);
                if (operatorPositions == null) continue;
                // Iterate through operators for each position
                int numOperators = operatorPositions.size();
                Loop[] loops = new Loop[numOperators + 1];
                // First loops should just add another operator to the array and call the next loop
                for (int j = 0; j < loops.length - 1; j++) {
                    final int j_ = j;
                    loops[j] = previousOperators -> {
                        char[] newArgs = new char[previousOperators.length + 1];
                        System.arraycopy(previousOperators, 0, newArgs, 0, previousOperators.length);
                        String allowedOperators = StringUtil.filterAllowed(charPossibilities[operatorPositions.get(j_)], "+-*/");
                        for (int k = 0; k < allowedOperators.length(); k++) {
                            newArgs[newArgs.length - 1] = allowedOperators.charAt(k);
                            loops[j_ + 1].run(newArgs);
                        }
                    };
                }
                // Last loop receives the complete array of operators, checks it, then does the final iteration
                final int equalsPos_ = equalsPos;
                loops[loops.length - 1] = operators -> {
                    String[] charSets = new String[equalsPos_]; // Only need charSets for indices before the equals sign - the result is calculated afterwards to minimise iteration
                    int[] maximums = new int[charSets.length]; // The size of the charSet for each index
                    // Modify charSets according to where operators are
                    int operatorsPlaced = 0;
                    for (int k = 0; k < charSets.length; k++) {
                        if (operatorPositions.contains(k)) {
                            // Must be the operator
                            charSets[k] = String.valueOf(operators[operatorsPlaced]);
                            operatorsPlaced++;
                        } else {
                            // Can't be an operator
                            String charSet = StringUtil.filterDisallowed(charPossibilities[k], "+-*/=");
                            if (operatorPositions.contains(k - 1)) {
                                // Can't be a leading zero after an operator
                                charSet = charSet.replace("0", "");
                            }
                            charSets[k] = charSet;
                        }
                        maximums[k] = charSets[k].length();
                    }
                    // Return immediately if any of the charSets are empty - no possibilities
                    if (Arrays.stream(maximums).anyMatch(n -> n == 0)) {
                        return;
                    }
                    // Assemble configuration with operators
                    String operatorConfig = Arrays.stream(charSets).map(s -> s.substring(0, 1)).collect(Collectors.joining()) + "=" + "1".repeat(length - equalsPos_ - 1);
                    if (Operator.isConfigImpossible(operatorConfig)) {
                        return;
                    }
                    Counter counter = new Counter(maximums);
                    while (counter.isRunning()) {
                        String left = counter.substitute(charSets); // Before equals sign
                        String right = MathUtil.truncateDecimals(String.valueOf(evaluate(left))); // After equals sign
                        String possibility = left + "=" + right;
                        // Check predicates
                        boolean succeededPredicates = true;
                        for (Predicate<String> predicate : predicates) {
                            if (!predicate.test(possibility)) {
                                succeededPredicates = false;
                            }
                        }
                        if (succeededPredicates) {
                            possibilities.add(possibility);
                        }
                        counter.increment();
                    }
                };
                loops[0].run();
            }
        }
        // Sort and return
        return possibilities;
    }

    public static List<Map.Entry<String, Double>> sortPossibilities(Collection<String> possibilities) {
        OrderedMap<String, Double> entropyMap = new OrderedMap<>(OrderedMap.reverseComparator(Comparator.comparingDouble(Map.Entry::getValue)));
        for (String possibility : possibilities) {
            entropyMap.put(possibility, getEntropy(possibility, possibilities));
        }
        return entropyMap.sortedEntryList();
    }

    private static List<Integer> getOperatorPositions(String[] charPossibilities, int equalsPos, String config) {
        List<Integer> operatorPositions = new ArrayList<>();
        for (int j = 0; j < config.length(); j++) {
            if (config.charAt(j) == '+') {
                if (j != 0 && j < equalsPos) { // Checks will throw ArrayIndexOutOfBoundsException otherwise
                    // Continue if there are multiple operators next to each other
                    if (config.charAt(j - 1) == '+' || config.charAt(j + 1) == '+') {
                        return null;
                    }
                }
                // Continue if this index can't be an operator
                if (charPossibilities[j].replaceAll("[1234567890]", "").length() == 0) {
                    return null;
                }
                operatorPositions.add(j);
            }
        }
        return operatorPositions;
    }

    private static List<Predicate<String>> getPredicates(String[] charPossibilities) {
        List<Predicate<String>> predicates = new ArrayList<>();
        // Correct length
        predicates.add(s -> s.length() == charPossibilities.length);
        // Negative result
        predicates.add(s -> s.charAt(s.indexOf('=') + 1) != '-');
        // Zeros
        predicates.add(s -> {
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '0' && "+-*/=".contains(String.valueOf(s.charAt(i - 1)))) {
                    return false;
                }
            }
            return true;
        });
        // Decimal result
        predicates.add(s -> !s.contains("."));
        // Must conform to charPossibilities
        predicates.add(s -> {
            for (int i = 0; i < Math.min(charPossibilities.length, s.length()); i++) {
                if (!charPossibilities[i].contains(String.valueOf(s.charAt(i)))) {
                    return false;
                }
            }
            return true;
        });
        return predicates;
    }

    private static String[] generateOperatorConfigurations(int[] given, int equalsPos, int length) {
        if (given.length == 0) {
            return generateOperatorConfigurations(new int[]{-1}, equalsPos, length);
        } else {
            List<String> configs = new ArrayList<>();
            // No more operators
            if (given.length != 1) {
                char[] charArray = new char[length];
                Arrays.fill(charArray, '1');
                charArray[equalsPos] = '=';
                for (int i = 1; i < given.length; i++) {
                    charArray[given[i]] = '+';
                }
                configs.add(new String(charArray));
            }
            // An operator in each other valid position
            for (int i = given[given.length - 1] + 2; i < equalsPos - 1; i++) {
                int[] newGiven = new int[given.length + 1];
                System.arraycopy(given, 0, newGiven, 0, given.length);
                newGiven[newGiven.length - 1] = i;
                configs.addAll(Arrays.asList(generateOperatorConfigurations(newGiven, equalsPos, length)));
            }
            return configs.toArray(new String[0]);
        }
    }

    private static String[] getBaseCharPossibilities(int length) {
        String[] charSets = new String[length];
        for (int i = 0; i < charSets.length; i++) {
            if (i == 0) {
                charSets[i] = "123456789";
            } else if (i == length - 1) {
                charSets[i] = "0123456789";
            } else {
                charSets[i] = "0123456789+-*/=";
            }
        }
        return charSets;
    }

    private interface Loop {

        void run(char... previousOperators);
    }
}
