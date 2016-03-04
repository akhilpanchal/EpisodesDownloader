package com.kumarankit.episodedownloader;

import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Akhil Panchal on 3/1/2016.
 * EpisodeDownloader.java:  Responsible for downloading
 *                          the requested episode from the URLs given by a Video Source
 */
public class EpisodeDownloader {

    private boolean download_success;
    private int failCount;
    private VideoSource videoSource;

    public EpisodeDownloader() {
        videoSource = new WatchSeries();
    }

    public void downLoadVideo(String[] args) {

        try{
            List<String> targetURLs;
            if (args[1].equals("-l"))                     //ED "Modern Family" -l  #download the latest episode of this series
                targetURLs = videoSource.GetLatestEpisodeUrls(args[0]);
            else if (args.length == 2 && !args[1].equals("-l"))                                         ////Input given as ED "Modern Family" s1-e14
                targetURLs = videoSource.GetRequestUrlsFromShow(args[0], args[1]);
            else {
                System.out.println("Input command arguments incorrect" +
                        "\n\n Use following format to download an episode when you know the exact episode::\n" +
                        "EpisodesDownloader \"Modern Family\" s3e12\n\n" +
                        "To get the latest episode, use the following format::\n" +
                        "EpisodesDownloader \"Modern Family\" -l\n");
                return;
            }

            if(targetURLs.size() == 0) {
                System.out.println("\n No links found for the given show. Verify if the episode exists.\n");
                return;
            }
            while(!download_success) {
                try {
                    DownloadVideo(targetURLs.get(failCount));
                } catch (IOException e) {
                    System.out.println("Download attempt failed from link " + failCount + ". Retrying...");
                }
            }
        }catch(Exception e)
        {
            System.out.println("\nError in fetching and downloading file.\n");
        }

    }

    private void DownloadVideo(String url) throws IOException
    {
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
            System.out.println(line);
            if(line.contains("ERROR"))
            {
                download_success = false;
                failCount++;
            }
            else if (line.contains("100%")){
                download_success = true;
            }
        }
    }
}
