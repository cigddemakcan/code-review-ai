package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.GitHubPRRequest;
import com.example.codereviewai.dto.response.GitHubPRReviewResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubPRService {

    private final RestTemplate restTemplate;

    @Value("${github.token}")
    private String githubToken;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    public GitHubPRReviewResponse reviewPR(GitHubPRRequest request) {
        log.info("Reviewing PR: {}", request.getPrUrl());
        String prUrl = request.getPrUrl();

        Pattern pattern = Pattern.compile("github\\.com/([^/]+)/([^/]+)/pull/(\\d+)");
        Matcher matcher = pattern.matcher(prUrl);
        if (!matcher.find()) {
            log.error("Invalid GitHub PR URL: {}", prUrl);
            throw new RuntimeException("Invalid GitHub PR URL");
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        int prNumber = Integer.parseInt(matcher.group(3));

        log.debug("Fetching PR diff for {}/{} PR#{}", owner, repo, prNumber);
        String diff = getPRDiff(owner, repo, prNumber);

        log.debug("Calling OpenRouter API for PR review");
        String rawReview = callOpenRouterApi(diff);

        rawReview = rawReview.replaceAll("```json", "").replaceAll("```", "").trim();

        GitHubPRReviewResponse response = new GitHubPRReviewResponse();
        response.setPrUrl(prUrl);
        response.setRepository(owner + "/" + repo);
        response.setPrNumber(prNumber);
        response.setCreatedAt(LocalDateTime.now());

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(rawReview);
            response.setSummary(node.get("summary").asText());
            response.setBugs(mapper.convertValue(node.get("bugs"), List.class));
            response.setSecurityIssues(mapper.convertValue(node.get("securityIssues"), List.class));
            response.setPerformanceIssues(mapper.convertValue(node.get("performanceIssues"), List.class));
            response.setSuggestions(mapper.convertValue(node.get("suggestions"), List.class));
        } catch (Exception e) {
            log.warn("Could not parse PR review as JSON, using raw content");
            response.setSummary(rawReview);
        }

        log.info("PR review completed for: {}", prUrl);
        return response;
    }

    private String getPRDiff(String owner, String repo, int prNumber) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls/" + prNumber + "/files";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> files = response.getBody();
        StringBuilder diffBuilder = new StringBuilder();

        if (files != null) {
            for (Map<String, Object> file : files) {
                String filename = (String) file.get("filename");
                String patch = (String) file.get("patch");
                if (patch != null) {
                    diffBuilder.append("File: ").append(filename).append("\n");
                    diffBuilder.append(patch).append("\n\n");
                }
            }
        }
        return diffBuilder.toString();
    }

    private String callOpenRouterApi(String diff) {
        String url = "https://openrouter.ai/api/v1/chat/completions";
        String prompt = "You are a code review assistant. Review the following GitHub PR diff. " +
                "Return ONLY a valid JSON object with no additional text, no markdown, no code blocks. " +
                "The JSON must have exactly these fields: " +
                "summary (string), bugs (array of strings), securityIssues (array of strings), " +
                "performanceIssues (array of strings), suggestions (array of strings). " +
                "PR Diff:\n" + diff;

        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openRouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:8080");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        List<Map> choices = (List<Map>) body.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }
}