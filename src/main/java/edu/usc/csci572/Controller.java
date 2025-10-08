package edu.usc.csci572;

import java.io.File;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
            System.out.println("Cleared old crawl storage: " + directory.getAbsolutePath());
        }
    }
    public static void main(String[] args) throws Exception {
        
        String crawlStorageFolder = "./data/crawl";
        int numberOfCrawlers = 7;

        deleteDirectory(new File(crawlStorageFolder));
        System.out.println("Starting fresh crawl...");

        //cofigure crawler
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxPagesToFetch(10000);
        config.setMaxDepthOfCrawling(16);
        config.setPolitenessDelay(1000);
        config.setIncludeBinaryContentInCrawling(true);
        config.setResumableCrawling(false);

        //instantiate controller
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        //create controller
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        controller.addSeed("https://www.latimes.com/");

        //start crawling
        controller.start(MyCrawler.class, numberOfCrawlers);
        System.out.println("Crawling completed.");

    }
}
