package com.example.syntaxio.ai;

import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Hint;
import java.util.List;

public interface AIService {
    List<Hint> generateHints(Challenge challenge, String userCode, String highlightedLine, String hintType);
    String getServiceName();
    boolean isAvailable();
}
