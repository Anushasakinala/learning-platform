package com.learning.platform.controller;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private static final int TOTAL_QUESTIONS = 10;

    private final JdbcTemplate jdbc;

    public QuizController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Answer(Long questionId, String choice) {}
    public record Submission(List<Answer> answers) {}

    @PostMapping("/next")
    public Map<String, Object> next(@RequestBody Submission sub) {
        List<Answer> answers = sub.answers() == null ? new ArrayList<>() : sub.answers();

        Map<Long, Map<String, Object>> byId = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT q.id, t.name AS topic, q.question_text AS text, " +
                "q.option_a AS a, q.option_b AS b, q.option_c AS c, q.option_d AS d, " +
                "q.correct, q.difficulty FROM questions q JOIN topics t ON t.id = q.topic_id")) {
            byId.put(((Number) r.get("id")).longValue(), r);
        }

        int level = 2;
        Boolean lastCorrect = null;
        Set<Long> done = new HashSet<>();

        for (Answer a : answers) {
            Map<String, Object> q = byId.get(a.questionId());
            if (q == null) continue;
            done.add(a.questionId());
            boolean ok = a.choice() != null && a.choice().equalsIgnoreCase((String) q.get("correct"));
            lastCorrect = ok;
            level = ok ? Math.min(3, level + 1) : Math.max(1, level - 1);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lastCorrect", lastCorrect);

        List<Map<String, Object>> candidates = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> e : byId.entrySet()) {
            if (!done.contains(e.getKey())) candidates.add(e.getValue());
        }

        if (done.size() >= TOTAL_QUESTIONS || candidates.isEmpty()) {
            result.put("finished", true);
            return result;
        }

        final int target = level;
        Collections.shuffle(candidates);
        candidates.sort(Comparator.comparingInt(
                (Map<String, Object> m) -> Math.abs(((Number) m.get("difficulty")).intValue() - target)));
        Map<String, Object> chosen = candidates.get(0);

        Map<String, Object> question = new LinkedHashMap<>();
        question.put("id", chosen.get("id"));
        question.put("topic", chosen.get("topic"));
        question.put("text", chosen.get("text"));
        question.put("a", chosen.get("a"));
        question.put("b", chosen.get("b"));
        question.put("c", chosen.get("c"));
        question.put("d", chosen.get("d"));

        result.put("finished", false);
        result.put("number", done.size() + 1);
        result.put("total", TOTAL_QUESTIONS);
        result.put("level", level);
        result.put("question", question);
        return result;
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Submission sub) {
        Map<Long, Map<String, Object>> key = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT q.id, t.id AS tid, t.name AS tname, q.correct " +
                "FROM questions q JOIN topics t ON t.id = q.topic_id")) {
            key.put(((Number) r.get("id")).longValue(), r);
        }

        TreeMap<Long, int[]> stats = new TreeMap<>();
        Map<Long, String> names = new HashMap<>();
        int score = 0;
        int total = 0;

        for (Answer a : sub.answers()) {
            Map<String, Object> k = key.get(a.questionId());
            if (k == null) continue;
            long tid = ((Number) k.get("tid")).longValue();
            names.put(tid, (String) k.get("tname"));
            int[] s = stats.computeIfAbsent(tid, x -> new int[2]);
            s[1]++;
            total++;
            if (a.choice() != null && a.choice().equalsIgnoreCase((String) k.get("correct"))) {
                s[0]++;
                score++;
            }
        }

        List<Map<String, Object>> topics = new ArrayList<>();
        List<Map<String, Object>> path = new ArrayList<>();
        int step = 1;

        for (Map.Entry<Long, int[]> e : stats.entrySet()) {
            int[] s = e.getValue();
            int percent = Math.round(100f * s[0] / s[1]);
            String status = percent >= 80 ? "STRONG" : percent >= 50 ? "DEVELOPING" : "WEAK";

            Map<String, Object> t = new LinkedHashMap<>();
            t.put("topic", names.get(e.getKey()));
            t.put("correct", s[0]);
            t.put("total", s[1]);
            t.put("percent", percent);
            t.put("status", status);
            topics.add(t);

            if (!status.equals("STRONG")) {
                int limit = status.equals("WEAK") ? 10 : 1;
                List<Map<String, Object>> lessons = jdbc.queryForList(
                    "SELECT title, minutes FROM lessons WHERE topic_id = ? ORDER BY lesson_level LIMIT ?",
                    e.getKey(), limit);
                for (Map<String, Object> l : lessons) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("step", step++);
                    p.put("topic", names.get(e.getKey()));
                    p.put("lesson", l.get("title"));
                    p.put("minutes", l.get("minutes"));
                    path.add(p);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("total", total);
        result.put("topics", topics);
        result.put("learningPath", path);
        return result;
    }
}