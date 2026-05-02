package com.AI;

import com.Model.Challenge;
import com.Model.Hint;
import java.util.List;

public interface AIService {
    List<Hint> generateHints(Challenge challenge, String userCode, String highlightedLine, String hintType);
    String getServiceName();
    boolean isAvailable();
}
