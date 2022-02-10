package com.alpsbte.plotsystem.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ShortLink {
    public static String generateShortLink(String linkToShorten, String apikey, String host) throws IOException {
        String fullURL = URLEncoder.encode(linkToShorten, StandardCharsets.UTF_8.toString());
        URL url = new URL(host + "/rest/v2/short-urls/shorten?apiKey=" + apikey + "&format=txt&longUrl=" + fullURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        con.addRequestProperty("Accept", "*/*");
        con.connect();

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        con.getInputStream().close();
        return content.toString();
    }
}

