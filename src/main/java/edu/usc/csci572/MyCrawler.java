    package edu.usc.csci572;

    import java.io.FileWriter;
    import java.io.IOException;
    import java.util.Set;
    import java.util.regex.Pattern;

    import edu.uci.ics.crawler4j.crawler.Page;
    import edu.uci.ics.crawler4j.parser.HtmlParseData;
    import edu.uci.ics.crawler4j.url.WebURL;

    public class MyCrawler extends edu.uci.ics.crawler4j.crawler.WebCrawler {

        private static final String rootDomain = "latimes.com";
        // private static final String rootURL = "https://www.latimes.com/";

        private static final String fetchCSV = "fetch_latimes.csv";
        private static final String visitCSV = "visit_latimes.csv";
        private static final String urlsCSV = "urls_latimes.csv";

        private static final Pattern rejects = Pattern.compile(".*(\\.(css|js|json|xml|" + "mp3|mp4|wav|avi|mov|mpeg|"+ "zip|tar|gz|rar|7z|" + "exe|dmg|pkg|deb))$");
        // private static final Pattern accepts = Pattern.compile(".*(\\.(html|htm|pdf|doc|docx|" + "jpg|jpeg|png|gif|bmp|svg))$");

        static{
            try{
                FileWriter fetchWriter = new FileWriter(fetchCSV);
                fetchWriter.write("URL,Status\n");
                fetchWriter.close();

                FileWriter visitWriter = new FileWriter(visitCSV);
                visitWriter.write("URL,Size(Bytes),Outlinks,Content-Type\n");
                visitWriter.close();

                FileWriter urlsWriter = new FileWriter(urlsCSV);
                urlsWriter.write("URL,Indicator\n");
                urlsWriter.close();    

                System.out.println("CSV files created with the headers.");
                
            } catch (IOException e){
                System.err.println("Error initializing CSV files: " + e.getMessage());
            }
        }

        private synchronized void writeToFetchCSV(String url, int status) {
            try (FileWriter writer = new FileWriter(fetchCSV, true)) {
                String escapedURL = url.replace(",", "-");
                writer.write(String.format("%s,%d\n", escapedURL, status));
                writer.close();
            } catch (IOException e) {
                System.err.println("Error writing to " + fetchCSV + ": " + e.getMessage());
            }
        }

        private synchronized void writeToVisitCSV(String url, int size, int outlinks, String contentType) {
            try (FileWriter writer = new FileWriter(visitCSV, true)) {
                String escapedURL = url.replace(",", "-");
                String escapedContentType = contentType != null ? contentType.replace(",", "-") : "unknown";
                writer.write(String.format("%s,%d,%d,%s\n", escapedURL, size, outlinks, escapedContentType));
                writer.close();
            } catch (IOException e) {
                System.err.println("Error writing to " + visitCSV + ": " + e.getMessage());
            }
        }

        private synchronized void writeToUrlsCSV(String discoveredURL, String indicator) {
            try (FileWriter writer = new FileWriter(urlsCSV, true)) {
                String escapedURL = discoveredURL.replace(",", "-");
                writer.write(String.format("%s,%s\n", escapedURL, indicator));
                writer.close();
            } catch (IOException e) {
                System.err.println("Error writing to " + urlsCSV + ": " + e.getMessage());
            }
        }

        @Override
        protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
            String url = webUrl.getURL();
            
            writeToFetchCSV(url, statusCode);
        }

        @Override
        public boolean shouldVisit(Page referringPage, WebURL url){
            String href = url.getURL().toLowerCase();
            
            if(!href.contains(rootDomain)) return false ;

            if(rejects.matcher(href).matches()) return false ;
            
            return true;
        }

        @Override
        public void visit(Page page){
            
            String url = page.getWebURL().getURL();
            int size = page.getContentData().length;
            String contentType = page.getContentType();

            if(contentType != null && contentType.contains(";")){
                contentType = contentType.split(";")[0].trim();
            }

            int numOfOutLinks = 0;
            Set<WebURL> links = null;

            if(page.getParseData() instanceof HtmlParseData){
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                links = htmlParseData.getOutgoingUrls();
                numOfOutLinks = links.size();   
            }

            writeToVisitCSV(url, size, numOfOutLinks, contentType);

            if(links != null){
                for(WebURL link : links){
                    String discoveredURL = link.getURL();

                    String indicator = discoveredURL.toLowerCase().contains(rootDomain)? "OK" : "N_OK";
                    writeToUrlsCSV(discoveredURL, indicator);
                }
            }
        }

    }
