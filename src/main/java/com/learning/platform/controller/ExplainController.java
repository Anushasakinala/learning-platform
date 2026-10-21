package com.learning.platform.controller;

import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/quiz")
public class ExplainController {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-5-5";

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

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("correctLetter", correct);
        out.put("correctText", correctText);

        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            out.put("explanation", "The correct answer is " + correct + ") " + correctText +
                    ". (AI tutor is not configured. Set ANTHROPIC_API_KEY to enable explanations.)");
            return out;
        }

        String prompt = "You are a friendly Java tutor. A student answered a quiz question wrongly. " +
            "In at most 3 short sentences, explain why the correct answer is right and why the student's " +
            "choice is wrong. Plain text only.\n\n" +
            "Topic: " + q.get("topic") + "\n" +
            "Question: " + q.get("qtext") + "\n" +
            "Student chose " + chosen + ": " + chosenText + "\n" +
            "Correct answer " + correct + ": " + correctText;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", MODEL);
        payload.put("max_tokens", 300);
        payload.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            Map<?, ?> body = http.post()
                .uri(API_URL)
                .header("x-api-key", key)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

            List<?> content = (List<?>) body.get("content");
            Map<?, ?> first = (Map<?, ?>) content.get(0);
            out.put("explanation", String.valueOf(first.get("text")));
        } catch (Exception e) {
            out.put("explanation", "The correct answer is " + correct + ") " + correctText +
                    ". (AI explanation unavailable right now.)");
        }
        return out;
    }
}