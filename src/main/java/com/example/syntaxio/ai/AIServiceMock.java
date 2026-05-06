package com.example.syntaxio.ai;

import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Hint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIServiceMock implements AIService {
    private Random random = new Random();
    
    @Override
    public List<Hint> generateHints(Challenge challenge, String userCode, String highlightedLine, String hintType) {
        List<Hint> hints = new ArrayList<>();
        
        // Simulate ai processing delay
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        switch (hintType) {
            case "GENERAL":
                hints.add(generateGeneralHint(challenge, userCode, highlightedLine));
                break;
            case "PSEUDOCODE":
                hints.add(generatePseudoCodeHint(challenge));
                break;
            case "DOCUMENTATION":
                hints.add(generateDocumentationHint(challenge));
                break;
            default:
                hints.add(generateGeneralHint(challenge, userCode, highlightedLine));
        }
        
        // Add a low-confidence "warning" hint to demonstrate ai imperfection
        if (random.nextInt(100) < 30) {  // 30% chance
            hints.add(generateLowConfidenceHint(challenge));
        }
        
        return hints;
    }
    
    private Hint generateGeneralHint(Challenge challenge, String userCode, String highlightedLine) {
        String hintText;
        int confidence;
        
        if (challenge.getId().equals("ch-001")) {  // Sum of Array
            if (userCode != null && !userCode.contains("for") && !userCode.contains("while")) {
                hintText = "💡 You need to iterate through the array. Consider using a for loop or enhanced for loop.";
                confidence = 85;
            } else if (userCode != null && userCode.contains("return 0")) {
                hintText = "💡 Remember to calculate the sum by adding each element to a variable, then return that variable.";
                confidence = 90;
            } else {
                hintText = "💡 Initialize a variable to 0, then loop through each element and add it to your variable.";
                confidence = 75;
            }
        } else if (challenge.getId().equals("ch-002")) {  // Find Maximum
            hintText = "💡 Start by assuming the first element is the maximum, then compare each other element.";
            confidence = 80;
        } else {
            hintText = "💡 Break down the problem into smaller steps. What's the first thing you need to do?";
            confidence = 70;
        }
        
        // Add highlighted line context if provided
        if (highlightedLine != null && !highlightedLine.isEmpty()) {
            hintText += "\n\nLooking at: " + highlightedLine;
        }
        
        return new Hint(challenge.getId(), hintText, "GENERAL", confidence);
    }
    
    private Hint generatePseudoCodeHint(Challenge challenge) {
        String pseudoCode;
        
        if (challenge.getId().equals("ch-001")) {
            pseudoCode = """
                PSEUDOCODE:
                
                FUNCTION sumArray(numbers):
                    sum = 0
                    FOR EACH number IN numbers:
                        sum = sum + number
                    END FOR
                    RETURN sum
                END FUNCTION
                """;
        } else if (challenge.getId().equals("ch-002")) {
            pseudoCode = """
                PSEUDOCODE:
                
                FUNCTION findMax(numbers):
                    IF array is empty:
                        RETURN minimum integer value
                    END IF
                    
                    max = numbers[0]
                    FOR EACH number IN numbers:
                        IF number > max:
                            max = number
                        END IF
                    END FOR
                    RETURN max
                END FUNCTION
                """;
        } else {
            pseudoCode = """
                PSEUDOCODE:
                
                FUNCTION solve(input):
                    // Think about the steps needed
                    // 1. Process the input
                    // 2. Perform the main logic
                    // 3. Return the result
                    RETURN result
                END FUNCTION
                """;
        }
        
        return new Hint(challenge.getId(), pseudoCode, "PSEUDOCODE", 65);
    }
    
    private Hint generateDocumentationHint(Challenge challenge) {
        String docs;
        
        if (challenge.getId().contains("array")) {
            docs = """
                📚 RESOURCES:
                
                • Java Arrays Tutorial:
                  https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
                  
                • Java for-each Loop:
                  https://docs.oracle.com/javase/tutorial/java/nutsandbolts/for.html
                """;
        } else if (challenge.getId().contains("string")) {
            docs = """
                📚 RESOURCES:
                
                • Java String API:
                  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html
                  
                • StringBuilder Documentation:
                  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/StringBuilder.html
                """;
        } else {
            docs = """
                📚 RESOURCES:
                
                • Java Tutorials:
                  https://docs.oracle.com/javase/tutorial/
                  
                • Method Signatures:
                  https://www.w3schools.com/java/java_methods.asp
                """;
        }
        
        return new Hint(challenge.getId(), docs, "DOCUMENTATION", 95);
    }
    
    private Hint generateLowConfidenceHint(Challenge challenge) {
        String hintText = "⚠️ UNCERTAIN: This suggestion may not be accurate.\n\n" +
                         "Consider whether you need to handle edge cases like empty arrays or null inputs.\n\n" +
                         "If this doesn't help, try regenerating or asking for a different hint type.";
        
        return new Hint(challenge.getId(), hintText, "GENERAL", 25);
    }
    
    @Override
    public String getServiceName() {
        return "Mock ai Assistant";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }    
}
