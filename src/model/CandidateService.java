package model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class CandidateService {
    private List<Candidate> candidates;
    private final Map<String, String> userVotes = new HashMap<>();
    private static final String DATA_FILE = "data/json/candidates.json";

    public CandidateService() {
        loadCandidatesFromJson();
    }

    private void loadCandidatesFromJson() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> candidatesData = gson.fromJson(reader, type);

            candidates = new ArrayList<>();
            for (int i = 0; i < candidatesData.size(); i++) {
                Map<String, String> data = candidatesData.get(i);
                candidates.add(new Candidate(
                        String.valueOf(i + 1),
                        data.get("name"),
                        data.get("photo")
                ));
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке данных кандидатов: " + e.getMessage());
            candidates = new ArrayList<>();
        }
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public Candidate getCandidateById(String id) {
        return candidates.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean hasUserVoted(String userIp) {
        return userVotes.containsKey(userIp);
    }

    public String getUserVote(String userIp) {
        return userVotes.get(userIp);
    }

    public Candidate voteForCandidate(String candidateId, String userIp) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate == null) {
            return null;
        }

        candidate.incrementVotes();
        userVotes.put(userIp, candidateId);
        return candidate;
    }

    public int getTotalVotes() {
        return candidates.stream().mapToInt(Candidate::getVotes).sum();
    }

    public List<Candidate> getCandidatesSortedByVotes() {
        return candidates.stream()
                .sorted(Comparator.comparing(Candidate::getVotes).reversed())
                .collect(Collectors.toList());
    }

    public String removeUserVote(String userIp) {
        return userVotes.remove(userIp);
    }
}