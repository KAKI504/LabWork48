package Server;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import model.Candidate;
import model.CandidateService;

public class VotingServer extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    private final CandidateService candidateService = new CandidateService();
    private static final String VOTE_COOKIE_NAME = "voted_candidate";

    public VotingServer(String host, int port) throws IOException {
        super(host, port);

        registerGet("/", this::handleMainPage);
        registerGet("/votes", this::handleVotesPage);
        registerGet("/vote", this::handleVoteGet);
        registerPost("/vote", this::handleVotePost);

        registerFileHandler(".css", ContentType.TEXT_CSS);
        registerFileHandler(".html", ContentType.TEXT_HTML);
        registerFileHandler(".jpg", ContentType.IMAGE_JPEG);
        registerFileHandler(".jpeg", ContentType.IMAGE_JPEG);
        registerFileHandler(".png", ContentType.IMAGE_PNG);

        System.out.println("Проверка наличия изображений:");
        for (int i = 1; i <= 6; i++) {
            String path = "data/img/" + i + ".jpeg";
            File file = new File(path);
            System.out.println("Файл " + path + " существует: " + file.exists());
        }

        registerStaticResourcesHandler();
    }

    private void handleMainPage(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("candidates", candidateService.getCandidates());

        String userIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (candidateService.hasUserVoted(userIp)) {
            String candidateId = candidateService.getUserVote(userIp);
            Candidate candidate = candidateService.getCandidateById(candidateId);

            Map<String, Object> thankYouData = new HashMap<>();
            thankYouData.put("candidate", candidate);
            thankYouData.put("votesPercentage", candidate.getVotesPercentage(candidateService.getTotalVotes()));

            renderTemplate(exchange, "thankyou.ftlh", thankYouData);
            return;
        }

        renderTemplate(exchange, "candidates.ftlh", data);
    }

    private void handleVotesPage(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("candidates", candidateService.getCandidatesSortedByVotes());
        data.put("totalVotes", candidateService.getTotalVotes());

        renderTemplate(exchange, "votes.ftlh", data);
    }

    private void handleVoteGet(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("candidateId=")) {
            respond404(exchange);
            return;
        }

        String candidateId = query.substring(query.indexOf("=") + 1);
        handleVote(exchange, candidateId);
    }

    private void handleVotePost(HttpExchange exchange) {
        String body = getBody(exchange);
        Map<String, String> formData = parseFormData(body);
        String candidateId = formData.get("candidateId");

        handleVote(exchange, candidateId);
    }

    private void handleVote(HttpExchange exchange, String candidateId) {
        String userIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (candidateService.hasUserVoted(userIp)) {
            String votedCandidateId = candidateService.getUserVote(userIp);
            Candidate candidate = candidateService.getCandidateById(votedCandidateId);

            Map<String, Object> thankYouData = new HashMap<>();
            thankYouData.put("candidate", candidate);
            thankYouData.put("votesPercentage", candidate.getVotesPercentage(candidateService.getTotalVotes()));

            renderTemplate(exchange, "thankyou.ftlh", thankYouData);
            return;
        }

        Candidate candidate = candidateService.getCandidateById(candidateId);
        if (candidate == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("error", "Кандидат с ID " + candidateId + " не найден");
            renderTemplate(exchange, "error.ftlh", data);
            return;
        }

        candidateService.voteForCandidate(candidateId, userIp);

        Cookie voteCookie = Cookie.make(VOTE_COOKIE_NAME, candidateId);
        setCookie(exchange, voteCookie);

        Map<String, Object> thankYouData = new HashMap<>();
        thankYouData.put("candidate", candidate);
        thankYouData.put("votesPercentage", candidate.getVotesPercentage(candidateService.getTotalVotes()));

        renderTemplate(exchange, "thankyou.ftlh", thankYouData);
    }

    private String getBody(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected Map<String, String> parseFormData(String raw) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = raw.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    result.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            String ftlhDir = new File("data/ftlh").getAbsolutePath();
            File dir = new File(ftlhDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Не удалось создать директорию: " + ftlhDir);
                }
            }
            cfg.setDirectoryForTemplateLoading(dir);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            Template temp = freemarker.getTemplate(templateFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
                temp.process(dataModel, writer);
                writer.flush();
                byte[] data = stream.toByteArray();
                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            respond404(exchange);
        }
    }

    protected void redirect303(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(303, 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCookie(HttpExchange exchange, Cookie cookie) {
        String headerValue = cookie.toString();
        exchange.getResponseHeaders().add("Set-Cookie", headerValue);
    }
}