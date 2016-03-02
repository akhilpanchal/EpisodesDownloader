package com.kumarankit.episodedownloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

public class Main {
    static boolean download_success = false;
    static int failed_count = 0;

    public static void main(String[] args) {
        //Sample for study : http://thewatchseries.to/episode/modern_family_s7_e14.html

        try{
            while(!download_success) {
                String requestURL = "";
                if (args[1].equals("-l"))                     //ED "Modern Family" -l  #download the latest episode of this series
                {
                    requestURL = constructLatestEpURL(args);
                } else if (args.length == 2 && !args[1].equals("-l"))                                         ////Input given as ED "Modern Family" s1-e14
                {
                    requestURL = Constants.WEBPAGE + "/" + Constants.EPISODE + "/" + constructEpURL(args) + Constants.HTML.toLowerCase();
                } else {
                    System.out.println("Input command arguments incorrect" +
                            "\n\n Use following format to download an episode when you know the exact episode::\n" +
                            "EpisodesDownloader \"Modern Family\" s3e12\n\n" +
                            "To get the latest episode, use the following format::\n" +
                            "EpisodesDownloader \"Modern Family\" -l\n");
                    return;
                }

                //create the url
//                System.out.println(requestURL);

                Elements elem = makeRequest(requestURL);

                String encodedLink = getEncodedLink(elem);
//                System.out.println(encodedLink);
                String downloadLink = decodeBase64(encodedLink);
//                System.out.println(downloadLink);

                try {
                    DownloadVideo(downloadLink);
                } catch (IOException e) {
                    System.out.println("Download attempt failed from link " + failed_count + ". Retrying...");
                }
            }
    }catch(Exception e)
    {
        System.out.println("\nError in fetching and downloading file. Verify if the episode exists.");
    }
    }

    private static String constructEpURL(String[] args) {
        //parse the season number and episode number
        return parseSpaces(args[0]) + "_" + parseEpisodeNumber(args[1]);
    }

    private static String parseSpaces(String arg) {
        return arg.replace(" ","_");
    }

    private static String constructLatestEpURL(String[] args) {
        String pageURL = Constants.WEBPAGE + "/" + Constants.SERIES + "/" + parseSpaces(args[0]);
        return getLatestEpisodeLink(makeRequest(pageURL));
    }

    private static String getLatestEpisodeLink(Elements elements) {
        for (Element e : elements) {
            if(e.text().contains("links)") && e.attr("abs:href").contains(Constants.WEBPAGE + "/" + Constants.EPISODE))
            {
                return e.attr("abs:href");
            }
        }
        return "";
    }

    private static String decodeBase64(String encodedString)
    {
        return new String(Base64.getDecoder().decode(encodedString));
    }

    public static String parseEpisodeNumber(String str)
    {
        return str.replace("e","_e");
    }

    private static Elements makeRequest(String constructedURL)
    {
        Document doc = null;
        try {
            doc = Jsoup.connect(constructedURL).get();
        } catch (IOException e) {
            System.out.println("The requested show isn't available.");
        }
        return doc != null ? doc.select("a[href]") : null;
    }

    private static String getEncodedLink(Elements e)
    {
        int skip_count = failed_count;
        for (Element link : e) {
            if(link.attr("abs:href").contains("cale.html?r"))
            {
                if(skip_count == 0) {
                    return link.attr("href").replace("?", "").substring(12);
                }
                else {
                    skip_count--;
                    continue;
                }
            }
        }
        return null;
    }

    private static void DownloadVideo(String url) throws IOException {

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "youtube-dl "+ url);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        System.out.println("\n\n");
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            if (!line.contains("ERROR"))
                System.out.print(line + "\r");
            if(line.contains("ERROR"))
            {
                download_success = false;
                failed_count++;
            }
            else if (line.contains("100%")){
                download_success = true;
            }
        }
    }
}
