package com.alpsbte.plotsystem.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Shortlink {

    public static String generateShortlink(String linkToShorten, String apikey, String host) throws IOException {
        String fullURL = URLEncoder.encode(linkToShorten, StandardCharsets.UTF_8.toString());
        URL url = new URL(host + "/rest/v2/short-urls/shorten?apiKey=" + apikey + "&format=txt&longUrl=" + fullURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        con.addRequestProperty("Accept",
                "*/*");
        con.connect();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    public static String generateShortlink(String linkToShorten, String apikey) throws IOException {
        String fullURL = URLEncoder.encode(linkToShorten, StandardCharsets.UTF_8.toString());
        URL url = new URL("https://buildthe.earth/rest/v2/short-urls/shorten?apiKey=" + apikey + "&format=txt&longUrl=" + fullURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        con.addRequestProperty("Accept",
                "*/*");
        con.connect();
        int status = con.getResponseCode();

        Reader streamReader = null;

        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println(content.toString());
            return "Error while fetching link.";
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    public static void main(String[] args) {
        try {
            System.out.println(generateShortlink("https://www.google.com/maps/place/52.59609026693616,13.437796760836301", "0ce04e33-1429-41c7-b6ae-b9e5415b7c29", "https://buildthe.earth"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

