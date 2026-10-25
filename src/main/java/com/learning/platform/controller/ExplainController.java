package com.learning.platform.controller;

import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/quiz")
public class ExplainController {

    private final JdbcTemplate jdbc;
    private final RestClient http = RestClient.create();

    public ExplainController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record ExplainRequest(Long questionId, String choice) {}

    private static String optionText(Map<String, Object> q, String letter) {
        return switch (letter.toUpperCase()) {
            case "A" -> (String) q.get("opt_a");
            case "B" -> (String) q.get("opt_b");
            case "C" -> (String) q.get("opt_c");
            case "D" -> (String) q.get("opt_d");
            default -> "(no answer)";
        };
    }

    @PostMapping("/explain")
    public Map<String, Object> explain(@RequestBody ExplainRequest req) {
        Map<String, Object> q = jdbc.queryForMap(
            "SELECT q.question_text AS qtext, q.option_a AS opt_a, q.option_b AS opt_b, " +
            "q.option_c AS opt_c, q.option_d AS opt_d, q.correct AS correct, t.name AS topic " +
            "FROM questions q JOIN topics t ON t.id = q.topic_id WHERE q.id = ?", req.questionId());

        String correct = (String) q.get("correct");
        String chosen = req.choice() == null ? "-" : req.choice();
        String correctText = optionText(q, correct);
        String chosenText = optionText(q, chosen);
        String fallback = "The correct answer is " + correct + ") " + correctText + ".";

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("correctLetter", correct);
        out.put("correctText", correctText);

        String key = System.getenv("GEMINI_API_KEY");
        key = key == null ? "" : key.trim();
        if (key.isEmpty()) {
            out.put("explanation", fallback);
            return out;
        }

        String model = System.getenv("GEMINI_MODEL");
        if (model == null || model.isBlank()) model = "gemini-3.5-flash";

        String prompt = "You are a friendly Java tutor. A student answered a quiz question wrongly. " +
            "In at most 3 short sentences, explain why the correct answer is right and why the student's " +
            "choice is wrong. Plain text only.\n\n" +
            "Topic: " + q.get("topic") + "\n" +
            "Question: " + q.get("qtext") + "\n" +
            "Student chose " + chosen + ": " + chosenText + "\n" +
            "Correct answer " + correct + ": " + correctText;

        Map<String, Object> payload = Map.of("contents",
            List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        try {
            Map<?, ?> body = http.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model.trim() + ":generateContent")
                .header("x-goog-api-key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

            List<?> candidates = (List<?>) body.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            out.put("explanation", String.valueOf(((Map<?, ?>) parts.get(0)).get("text")).trim());
        } catch (Exception e) {
            System.err.println("AI error: " + e);
            out.put("explanation", fallback);
        }
        return out;
    }
}