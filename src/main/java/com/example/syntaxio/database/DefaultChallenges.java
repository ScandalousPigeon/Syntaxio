package com.example.syntaxio.database;

import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.TestCase;

import java.util.List;

final class DefaultChallenges {

    private DefaultChallenges() {
    }

    static List<Challenge> all() {
        return List.of(
                new Challenge(
                        "ch-001",
                        "Sum of Array",
                        "Write a method that takes an array of integers and returns the sum of all elements.\n\n"
                                + "Method signature: `public int sumArray(int[] numbers)`",
                        """
                        public int sumArray(int[] numbers) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Array with positive numbers", "new int[]{1, 2, 3}", "6"),
                                new TestCase("Array with negative numbers", "new int[]{-1, -2, -3}", "-6"),
                                new TestCase("Empty array", "new int[]{}", "0"),
                                new TestCase("Array with one element", "new int[]{42}", "42")
                        ),
                        """
                        public int sumArray(int[] numbers) {
                            int sum = 0;
                            for (int number : numbers) {
                                sum += number;
                            }
                            return sum;
                        }
                        """
                ),
                new Challenge(
                        "ch-002",
                        "Find Maximum",
                        "Write a method that finds and returns the maximum value in an array of integers.\n\n"
                                + "Method signature: `public int findMax(int[] numbers)`\n\n"
                                + "If the array is empty, return `Integer.MIN_VALUE`.",
                        """
                        public int findMax(int[] numbers) {
                            if (numbers.length == 0) {
                                return Integer.MIN_VALUE;
                            }
                            // Your code here
                            return 0;
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Normal array", "new int[]{3, 7, 2, 9, 1}", "9"),
                                new TestCase("All negative", "new int[]{-5, -2, -8, -1}", "-1"),
                                new TestCase("Single element", "new int[]{10}", "10")
                        ),
                        """
                        public int findMax(int[] numbers) {
                            if (numbers.length == 0) {
                                return Integer.MIN_VALUE;
                            }

                            int max = numbers[0];
                            for (int number : numbers) {
                                if (number > max) {
                                    max = number;
                                }
                            }
                            return max;
                        }
                        """
                ),
                new Challenge(
                        "ch-003",
                        "Reverse String",
                        "Write a method that reverses a string.\n\n"
                                + "Method signature: `public String reverseString(String input)`",
                        """
                        public String reverseString(String input) {
                            // Your code here
                            return "";
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Normal string", "\"hello\"", "olleh"),
                                new TestCase("Palindrome", "\"racecar\"", "racecar"),
                                new TestCase("Empty string", "\"\"", "")
                        ),
                        """
                        public String reverseString(String input) {
                            return new StringBuilder(input).reverse().toString();
                        }
                        """
                ),
                new Challenge(
                        "ch-004",
                        "Count Vowels",
                        "Write a method that counts the vowels in a string. Count `a`, `e`, `i`, `o`, and `u`, "
                                + "ignoring case.\n\n"
                                + "Method signature: `public int countVowels(String input)`",
                        """
                        public int countVowels(String input) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Lowercase word", "\"hello\"", "2"),
                                new TestCase("Uppercase word", "\"SYNTAXIO\"", "3"),
                                new TestCase("No vowels", "\"bcdfg\"", "0"),
                                new TestCase("Empty string", "\"\"", "0")
                        ),
                        """
                        public int countVowels(String input) {
                            int count = 0;
                            String vowels = "aeiou";

                            for (char letter : input.toLowerCase().toCharArray()) {
                                if (vowels.indexOf(letter) != -1) {
                                    count++;
                                }
                            }

                            return count;
                        }
                        """
                ),
                new Challenge(
                        "ch-005",
                        "Is Even",
                        "Write a method that returns `true` when a number is even and `false` otherwise.\n\n"
                                + "Method signature: `public boolean isEven(int number)`",
                        """
                        public boolean isEven(int number) {
                            // Your code here
                            return false;
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Positive even number", "4", "true"),
                                new TestCase("Positive odd number", "7", "false"),
                                new TestCase("Zero", "0", "true"),
                                new TestCase("Negative even number", "-2", "true")
                        ),
                        """
                        public boolean isEven(int number) {
                            return number % 2 == 0;
                        }
                        """
                ),
                new Challenge(
                        "ch-006",
                        "Find Minimum",
                        "Write a method that returns the smallest integer in an array.\n\n"
                                + "Method signature: `public int findMin(int[] numbers)`\n\n"
                                + "If the array is empty, return `Integer.MAX_VALUE`.",
                        """
                        public int findMin(int[] numbers) {
                            if (numbers.length == 0) {
                                return Integer.MAX_VALUE;
                            }
                            // Your code here
                            return 0;
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Mixed values", "new int[]{5, -1, 8, 2}", "-1"),
                                new TestCase("All positive values", "new int[]{9, 3, 6}", "3"),
                                new TestCase("Single value", "new int[]{12}", "12"),
                                new TestCase("Empty array", "new int[]{}", "2147483647")
                        ),
                        """
                        public int findMin(int[] numbers) {
                            if (numbers.length == 0) {
                                return Integer.MAX_VALUE;
                            }

                            int min = numbers[0];
                            for (int number : numbers) {
                                if (number < min) {
                                    min = number;
                                }
                            }
                            return min;
                        }
                        """
                ),
                new Challenge(
                        "ch-007",
                        "Count Positive Numbers",
                        "Write a method that counts how many numbers in an array are greater than zero.\n\n"
                                + "Method signature: `public int countPositive(int[] numbers)`",
                        """
                        public int countPositive(int[] numbers) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Mixed values", "new int[]{1, -2, 3, 0}", "2"),
                                new TestCase("No positive values", "new int[]{-1, 0, -3}", "0"),
                                new TestCase("All positive values", "new int[]{4, 5, 6}", "3"),
                                new TestCase("Empty array", "new int[]{}", "0")
                        ),
                        """
                        public int countPositive(int[] numbers) {
                            int count = 0;
                            for (int number : numbers) {
                                if (number > 0) {
                                    count++;
                                }
                            }
                            return count;
                        }
                        """
                ),
                new Challenge(
                        "ch-008",
                        "Double Each Number",
                        "Write a method that returns a new array where every number from the input array is doubled.\n\n"
                                + "Method signature: `public int[] doubleNumbers(int[] numbers)`",
                        """
                        public int[] doubleNumbers(int[] numbers) {
                            // Your code here
                            return new int[]{};
                        }
                        """,
                        "EASY",
                        List.of(
                                new TestCase("Positive numbers", "new int[]{1, 2, 3}", "[2, 4, 6]"),
                                new TestCase("Mixed numbers", "new int[]{-1, 0, 5}", "[-2, 0, 10]"),
                                new TestCase("Single number", "new int[]{8}", "[16]"),
                                new TestCase("Empty array", "new int[]{}", "[]")
                        ),
                        """
                        public int[] doubleNumbers(int[] numbers) {
                            int[] doubled = new int[numbers.length];
                            for (int i = 0; i < numbers.length; i++) {
                                doubled[i] = numbers[i] * 2;
                            }
                            return doubled;
                        }
                        """
                ),
                new Challenge(
                        "ch-009",
                        "Count Words",
                        "Write a method that counts how many words are in a sentence. Words are separated by one "
                                + "or more spaces.\n\n"
                                + "Method signature: `public int countWords(String sentence)`",
                        """
                        public int countWords(String sentence) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Simple sentence", "\"Java is fun\"", "3"),
                                new TestCase("Extra spaces", "\"  spaced   words  \"", "2"),
                                new TestCase("Single word", "\"Syntaxio\"", "1"),
                                new TestCase("Empty sentence", "\"\"", "0")
                        ),
                        """
                        public int countWords(String sentence) {
                            String trimmed = sentence.trim();
                            if (trimmed.isEmpty()) {
                                return 0;
                            }
                            return trimmed.split("\\s+").length;
                        }
                        """
                ),
                new Challenge(
                        "ch-010",
                        "Palindrome Check",
                        "Write a method that returns `true` if a string reads the same forwards and backwards. "
                                + "Ignore spaces and letter case.\n\n"
                                + "Method signature: `public boolean isPalindrome(String input)`",
                        """
                        public boolean isPalindrome(String input) {
                            // Your code here
                            return false;
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Simple palindrome", "\"racecar\"", "true"),
                                new TestCase("Mixed case palindrome", "\"Level\"", "true"),
                                new TestCase("Not a palindrome", "\"hello\"", "false"),
                                new TestCase("Palindrome with a space", "\"nurses run\"", "true")
                        ),
                        """
                        public boolean isPalindrome(String input) {
                            String cleaned = input.replace(" ", "").toLowerCase();
                            int left = 0;
                            int right = cleaned.length() - 1;

                            while (left < right) {
                                if (cleaned.charAt(left) != cleaned.charAt(right)) {
                                    return false;
                                }
                                left++;
                                right--;
                            }

                            return true;
                        }
                        """
                ),
                new Challenge(
                        "ch-011",
                        "Factorial",
                        "Write a method that returns the factorial of a non-negative integer. The factorial of "
                                + "`0` is `1`.\n\n"
                                + "Method signature: `public int factorial(int n)`",
                        """
                        public int factorial(int n) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Zero", "0", "1"),
                                new TestCase("One", "1", "1"),
                                new TestCase("Five", "5", "120"),
                                new TestCase("Seven", "7", "5040")
                        ),
                        """
                        public int factorial(int n) {
                            int result = 1;
                            for (int i = 2; i <= n; i++) {
                                result *= i;
                            }
                            return result;
                        }
                        """
                ),
                new Challenge(
                        "ch-012",
                        "Fizz Buzz Value",
                        "Write a method that returns `FizzBuzz` for numbers divisible by both 3 and 5, `Fizz` "
                                + "for numbers divisible by 3, `Buzz` for numbers divisible by 5, and otherwise "
                                + "the number as text.\n\n"
                                + "Method signature: `public String fizzBuzz(int number)`",
                        """
                        public String fizzBuzz(int number) {
                            // Your code here
                            return "";
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Divisible by 3 and 5", "15", "FizzBuzz"),
                                new TestCase("Divisible by 3", "9", "Fizz"),
                                new TestCase("Divisible by 5", "10", "Buzz"),
                                new TestCase("Not divisible by 3 or 5", "7", "7")
                        ),
                        """
                        public String fizzBuzz(int number) {
                            if (number % 15 == 0) {
                                return "FizzBuzz";
                            }
                            if (number % 3 == 0) {
                                return "Fizz";
                            }
                            if (number % 5 == 0) {
                                return "Buzz";
                            }
                            return String.valueOf(number);
                        }
                        """
                ),
                new Challenge(
                        "ch-013",
                        "Remove Duplicates",
                        "Write a method that removes duplicate numbers from an array while keeping the first "
                                + "time each number appears.\n\n"
                                + "Method signature: `public int[] removeDuplicates(int[] numbers)`",
                        """
                        public int[] removeDuplicates(int[] numbers) {
                            // Your code here
                            return new int[]{};
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Repeated values", "new int[]{1, 2, 2, 3, 1}", "[1, 2, 3]"),
                                new TestCase("All the same", "new int[]{4, 4, 4}", "[4]"),
                                new TestCase("Already unique", "new int[]{5, 6, 7}", "[5, 6, 7]"),
                                new TestCase("Empty array", "new int[]{}", "[]")
                        ),
                        """
                        public int[] removeDuplicates(int[] numbers) {
                            LinkedHashSet<Integer> unique = new LinkedHashSet<>();
                            for (int number : numbers) {
                                unique.add(number);
                            }

                            int[] result = new int[unique.size()];
                            int index = 0;
                            for (int number : unique) {
                                result[index] = number;
                                index++;
                            }
                            return result;
                        }
                        """
                ),
                new Challenge(
                        "ch-014",
                        "Sum Digits",
                        "Write a method that returns the sum of the digits in an integer. Negative numbers should "
                                + "be treated as positive.\n\n"
                                + "Method signature: `public int sumDigits(int number)`",
                        """
                        public int sumDigits(int number) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Several digits", "1234", "10"),
                                new TestCase("Negative number", "-506", "11"),
                                new TestCase("Zero", "0", "0"),
                                new TestCase("Single digit", "8", "8")
                        ),
                        """
                        public int sumDigits(int number) {
                            int remaining = Math.abs(number);
                            int sum = 0;

                            while (remaining > 0) {
                                sum += remaining % 10;
                                remaining /= 10;
                            }

                            return sum;
                        }
                        """
                ),
                new Challenge(
                        "ch-015",
                        "Merge Sorted Arrays",
                        "Write a method that merges two sorted integer arrays into one sorted array.\n\n"
                                + "Method signature: `public int[] mergeSorted(int[] first, int[] second)`",
                        """
                        public int[] mergeSorted(int[] first, int[] second) {
                            // Your code here
                            return new int[]{};
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Same length arrays", "new int[]{1, 3, 5}, new int[]{2, 4, 6}", "[1, 2, 3, 4, 5, 6]"),
                                new TestCase("First array empty", "new int[]{}, new int[]{1, 2}", "[1, 2]"),
                                new TestCase("Negative values", "new int[]{-3, 0}, new int[]{-2, 4}", "[-3, -2, 0, 4]"),
                                new TestCase("Second array empty", "new int[]{7, 8}, new int[]{}", "[7, 8]")
                        ),
                        """
                        public int[] mergeSorted(int[] first, int[] second) {
                            int[] merged = new int[first.length + second.length];
                            int firstIndex = 0;
                            int secondIndex = 0;
                            int mergedIndex = 0;

                            while (firstIndex < first.length && secondIndex < second.length) {
                                if (first[firstIndex] <= second[secondIndex]) {
                                    merged[mergedIndex] = first[firstIndex];
                                    firstIndex++;
                                } else {
                                    merged[mergedIndex] = second[secondIndex];
                                    secondIndex++;
                                }
                                mergedIndex++;
                            }

                            while (firstIndex < first.length) {
                                merged[mergedIndex] = first[firstIndex];
                                firstIndex++;
                                mergedIndex++;
                            }

                            while (secondIndex < second.length) {
                                merged[mergedIndex] = second[secondIndex];
                                secondIndex++;
                                mergedIndex++;
                            }

                            return merged;
                        }
                        """
                ),
                new Challenge(
                        "ch-016",
                        "Longest Word",
                        "Write a method that returns the longest word in a sentence. If multiple words tie, return "
                                + "the first one. If the sentence has no words, return an empty string.\n\n"
                                + "Method signature: `public String longestWord(String sentence)`",
                        """
                        public String longestWord(String sentence) {
                            // Your code here
                            return "";
                        }
                        """,
                        "MEDIUM",
                        List.of(
                                new TestCase("Clear longest word", "\"Java makes coding fun\"", "coding"),
                                new TestCase("Tie keeps first word", "\"one three five\"", "three"),
                                new TestCase("Single word", "\"Syntaxio\"", "Syntaxio"),
                                new TestCase("Only spaces", "\"   \"", "")
                        ),
                        """
                        public String longestWord(String sentence) {
                            String trimmed = sentence.trim();
                            if (trimmed.isEmpty()) {
                                return "";
                            }

                            String longest = "";
                            for (String word : trimmed.split("\\s+")) {
                                if (word.length() > longest.length()) {
                                    longest = word;
                                }
                            }
                            return longest;
                        }
                        """
                ),
                new Challenge(
                        "ch-017",
                        "Fibonacci Number",
                        "Write a method that returns the nth Fibonacci number. Use `0` for the first number and "
                                + "`1` for the second number.\n\n"
                                + "Method signature: `public int fibonacci(int n)`",
                        """
                        public int fibonacci(int n) {
                            // Your code here
                            return 0;
                        }
                        """,
                        "HARD",
                        List.of(
                                new TestCase("First number", "0", "0"),
                                new TestCase("Second number", "1", "1"),
                                new TestCase("Seventh number", "7", "13"),
                                new TestCase("Tenth number", "10", "55")
                        ),
                        """
                        public int fibonacci(int n) {
                            if (n <= 1) {
                                return n;
                            }

                            int previous = 0;
                            int current = 1;
                            for (int i = 2; i <= n; i++) {
                                int next = previous + current;
                                previous = current;
                                current = next;
                            }
                            return current;
                        }
                        """
                ),
                new Challenge(
                        "ch-018",
                        "Balanced Brackets",
                        "Write a method that returns `true` if every opening bracket has the correct closing "
                                + "bracket. Check `()`, `[]`, and `{}` and ignore other characters.\n\n"
                                + "Method signature: `public boolean hasBalancedBrackets(String input)`",
                        """
                        public boolean hasBalancedBrackets(String input) {
                            // Your code here
                            return false;
                        }
                        """,
                        "HARD",
                        List.of(
                                new TestCase("Nested brackets", "\"([])\"", "true"),
                                new TestCase("Wrong order", "\"([)]\"", "false"),
                                new TestCase("Expression with letters", "\"a+(b*c)-{d/e}\"", "true"),
                                new TestCase("Missing closing bracket", "\"((()\"", "false")
                        ),
                        """
                        public boolean hasBalancedBrackets(String input) {
                            Deque<Character> stack = new ArrayDeque<>();

                            for (char character : input.toCharArray()) {
                                if (character == '(' || character == '[' || character == '{') {
                                    stack.push(character);
                                } else if (character == ')' || character == ']' || character == '}') {
                                    if (stack.isEmpty()) {
                                        return false;
                                    }

                                    char opening = stack.pop();
                                    if (character == ')' && opening != '(') {
                                        return false;
                                    }
                                    if (character == ']' && opening != '[') {
                                        return false;
                                    }
                                    if (character == '}' && opening != '{') {
                                        return false;
                                    }
                                }
                            }

                            return stack.isEmpty();
                        }
                        """
                ),
                new Challenge(
                        "ch-019",
                        "Anagram Check",
                        "Write a method that returns `true` when two strings contain the same letters in a "
                                + "different order. Ignore spaces and letter case.\n\n"
                                + "Method signature: `public boolean areAnagrams(String first, String second)`",
                        """
                        public boolean areAnagrams(String first, String second) {
                            // Your code here
                            return false;
                        }
                        """,
                        "HARD",
                        List.of(
                                new TestCase("Simple anagram", "\"listen\", \"silent\"", "true"),
                                new TestCase("Spaces and case", "\"Dormitory\", \"dirty room\"", "true"),
                                new TestCase("Different letters", "\"java\", \"javascript\"", "false"),
                                new TestCase("Longer phrase", "\"A gentleman\", \"Elegant man\"", "true")
                        ),
                        """
                        public boolean areAnagrams(String first, String second) {
                            String cleanedFirst = first.replaceAll("\\s+", "").toLowerCase();
                            String cleanedSecond = second.replaceAll("\\s+", "").toLowerCase();

                            if (cleanedFirst.length() != cleanedSecond.length()) {
                                return false;
                            }

                            char[] firstLetters = cleanedFirst.toCharArray();
                            char[] secondLetters = cleanedSecond.toCharArray();
                            Arrays.sort(firstLetters);
                            Arrays.sort(secondLetters);

                            return Arrays.equals(firstLetters, secondLetters);
                        }
                        """
                ),
                new Challenge(
                        "ch-020",
                        "Two Sum Indices",
                        "Write a method that returns the indices of the first pair of numbers that add up to "
                                + "the target. If no pair exists, return `{-1, -1}`.\n\n"
                                + "Method signature: `public int[] twoSum(int[] numbers, int target)`",
                        """
                        public int[] twoSum(int[] numbers, int target) {
                            // Your code here
                            return new int[]{-1, -1};
                        }
                        """,
                        "HARD",
                        List.of(
                                new TestCase("Pair at the start", "new int[]{2, 7, 11, 15}, 9", "[0, 1]"),
                                new TestCase("Pair in the middle", "new int[]{3, 2, 4}, 6", "[1, 2]"),
                                new TestCase("No matching pair", "new int[]{1, 2, 3}, 7", "[-1, -1]"),
                                new TestCase("Duplicate values", "new int[]{3, 3}, 6", "[0, 1]")
                        ),
                        """
                        public int[] twoSum(int[] numbers, int target) {
                            for (int i = 0; i < numbers.length; i++) {
                                for (int j = i + 1; j < numbers.length; j++) {
                                    if (numbers[i] + numbers[j] == target) {
                                        return new int[]{i, j};
                                    }
                                }
                            }
                            return new int[]{-1, -1};
                        }
                        """
                )
        );
    }
}
