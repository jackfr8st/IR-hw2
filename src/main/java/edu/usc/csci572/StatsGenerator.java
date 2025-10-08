package edu.usc.csci572;

import java.io.*;
import java.util.*;

public class StatsGenerator {
    
    public static void main(String[] args) {
        generateReport("fetch_latimes.csv", "visit_latimes.csv", "urls_latimes.csv", 
                      "CrawlReport_latimes.txt", "https://www.latimes.com/", 7);
    }
    
    public static void generateReport(String fetchFile, String visitFile, String urlsFile, String reportFile, String siteName, int numberOfCrawlers) {
        try {
            
            // fetch.csv
            Map<Integer, Integer> statusCodes = new HashMap<>();
            int fetchAttempted = 0;
            int fetchSucceeded = 0;
            int fetchFailed = 0;
            
            BufferedReader fetchReader = new BufferedReader(new FileReader(fetchFile));
            String line = fetchReader.readLine(); 
            
            while ((line = fetchReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    fetchAttempted++;
                    try {
                        int status = Integer.parseInt(parts[1].trim());
                        statusCodes.put(status, statusCodes.getOrDefault(status, 0) + 1);
                        
                        if (status >= 200 && status < 300) {
                            fetchSucceeded++;
                        } else {
                            fetchFailed++;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed line in fetch.csv: " + line);
                    }
                }
            }
            fetchReader.close();
            
            // visit.csv
            Map<String, Integer> contentTypes = new HashMap<>();
            int[] fileSizes = new int[5]; 
            int totalOutlinks = 0;
            
            BufferedReader visitReader = new BufferedReader(new FileReader(visitFile));
            line = visitReader.readLine(); 
            
            while ((line = visitReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    // Size
                    try {
                        int size = Integer.parseInt(parts[1].trim());
                        if (size < 1024) fileSizes[0]++;
                        else if (size < 10 * 1024) fileSizes[1]++;
                        else if (size < 100 * 1024) fileSizes[2]++;
                        else if (size < 1024 * 1024) fileSizes[3]++;
                        else fileSizes[4]++;
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed size in visit.csv: " + line);
                    }
                    
                    // Outlinks
                    try {
                        totalOutlinks += Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed outlinks in visit.csv: " + line);
                    }
                    
                    // Content type
                    String contentType = parts[3].trim();
                    contentTypes.put(contentType, contentTypes.getOrDefault(contentType, 0) + 1);
                }
            }
            visitReader.close();
            
            // urls.csv
            Set<String> uniqueURLs = new HashSet<>();
            Set<String> uniqueInsideURLs = new HashSet<>();
            Set<String> uniqueOutsideURLs = new HashSet<>();
            // int totalURLs = 0;
            
            BufferedReader urlsReader = new BufferedReader(new FileReader(urlsFile));
            line = urlsReader.readLine(); 
            
            while ((line = urlsReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String url = parts[0].trim();
                    String indicator = parts[1].trim();
                    
                    totalURLs++;
                    uniqueURLs.add(url);
                    
                    if ("OK".equals(indicator)) {
                        uniqueInsideURLs.add(url);
                    } else {
                        uniqueOutsideURLs.add(url);
                    }
                }
            }
            urlsReader.close();
            
   
            PrintWriter writer = new PrintWriter(new FileWriter(reportFile));
            writer.println("Name: Ojas Anil Golatkar");
            writer.println("USC ID: 9020-1319-69");
            writer.println("News site crawled: " + siteName);
            writer.println("Number of threads: " + numberOfCrawlers);
            writer.println();
            
            writer.println("Fetch Statistics");
            writer.println("================");
            writer.println("# fetches attempted: " + fetchAttempted);
            writer.println("# fetches succeeded: " + fetchSucceeded);
            writer.println("# fetches failed or aborted: " + fetchFailed);
            writer.println();
            
            writer.println("Outgoing URLs:");
            writer.println("==============");
            writer.println("Total URLs extracted: " + totalOutlinks);
            writer.println("# unique URLs extracted: " + uniqueURLs.size());
            writer.println("# unique URLs within News Site: " + uniqueInsideURLs.size());
            writer.println("# unique URLs outside News Site: " + uniqueOutsideURLs.size());
            writer.println();
            
            writer.println("Status Codes:");
            writer.println("=============");
            List<Integer> sortedCodes = new ArrayList<>(statusCodes.keySet());
            Collections.sort(sortedCodes);
            for (int code : sortedCodes) {
                String description = getStatusDescription(code);
                writer.println(code + " " + description + ": " + statusCodes.get(code));
            }
            writer.println();
            
            writer.println("File Sizes:");
            writer.println("===========");
            writer.println("< 1KB: " + fileSizes[0]);
            writer.println("1KB ~ <10KB: " + fileSizes[1]);
            writer.println("10KB ~ <100KB: " + fileSizes[2]);
            writer.println("100KB ~ <1MB: " + fileSizes[3]);
            writer.println(">= 1MB: " + fileSizes[4]);
            writer.println();
            
            writer.println("Content Types:");
            writer.println("==============");
            List<String> sortedTypes = new ArrayList<>(contentTypes.keySet());
            Collections.sort(sortedTypes);
            for (String type : sortedTypes) {
                writer.println(type + ": " + contentTypes.get(type));
            }
            
            writer.close();
            System.out.println("Report generated: " + reportFile);
            
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getStatusDescription(int code) {
        switch (code) {
            case 200: return "OK";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 307: return "Temporary Redirect";
            case 308: return "Permanent Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default: return "";
        }
    }
}