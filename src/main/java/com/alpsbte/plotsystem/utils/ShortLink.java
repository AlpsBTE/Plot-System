package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ShortLink {
    private ShortLink() {}

    public static @NotNull String generateShortLink(String linkToShorten) throws IOException, URISyntaxException {
        String apikey = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY);
        String host = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST);
        String fullURL = URLEncoder.encode(linkToShorten, StandardCharsets.UTF_8);
        URI uri = new URI(host + "/rest/v2/short-urls/shorten?apiKey=" + apikey + "&format=txt&longUrl=" + fullURL);
        HttpsURLConnection con = (HttpsURLConnection) uri.toURL().openConnection();
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

