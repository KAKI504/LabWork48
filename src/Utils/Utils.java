package Utils;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private Utils() {
    }

    public static Map<String, String> parseUrlEncoded(String rawLines, String delimiter) {
        String[] lines = rawLines.split(delimiter);
        Stream<Map.Entry<String, String>> stream = Arrays.stream(lines)
                .map(Utils::decode)
                .filter(Optional::isPresent)
                .map(Optional::get);
        return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
    }

    static Optional<Map.Entry<String, String>> decode(String kv) {
        if (!kv.contains("=")) return Optional.empty();
        String[] pairs = kv.split("=");
        if (pairs.length != 2) return Optional.empty();
        Charset utf8 = StandardCharsets.UTF_8;
        String key = URLDecoder.decode(pairs[0], utf8);
        String value = URLDecoder.decode(pairs[1], utf8);
        return Optional.of(Map.entry(key.strip(), value.strip()));
    }
}