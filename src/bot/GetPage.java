package bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class GetPage {
    /* the CASE_INSENSITIVE flag accounts for
     * sites that use uppercase title tags.
     * the DOTALL flag accounts for sites that have
     * line feeds in the title text */
    private static final Pattern TITLE_TAG = Pattern.compile("\\<title>(.*?)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    private static final Pattern AUTHOR_TAG = Pattern.compile("/user/(.*?)\">");
    private static final Pattern DURATION = Pattern.compile("itemprop=\"duration\" content=\"PT(.*?)S\">", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    private static final Pattern WIKIERROR = Pattern.compile("<b>Wikipedia does not have an article with this exact name.</b>");
    private static final Pattern TWEETMSG = Pattern.compile(",\"id\":(.*?),\"text\":\"(.*?)\",");
    private static final Pattern TWEETAUTHOR = Pattern.compile("Twitter / (.*?):");
    private static final Pattern TWEETTIME = Pattern.compile("js-nav\" title=\"(.*?)\"");
    private static final Pattern THREAD_TITLE = Pattern.compile("<head><title>(.*?)</title><meta");
    private static final Pattern THREAD_UPVOTES = Pattern.compile("class=\"upvotes\"><span class='number'>(.*?)</span>");
    private static final Pattern THREAD_DOWNVOTES = Pattern.compile("class=\"downvotes\"><span class='number'>(.*?)</span>");
    private static final Pattern GENERIC_TITLE = Pattern.compile("<title>(.*?)</title>");
    
    static String title="";
    static String author="";
    static String duration="";
    static String urlPassed="";
    static String wikiError="";
    static String tweet="";
    static String upvotes="";
    static String downvotes="";
    int errorID = 0;
    //1 = not valid HTML
 
    /**
     * @param url the HTML page
     * @throws IOException
     */
    public GetPage(String url) throws IOException {
    	errorID=0;
        URL u = new URL(url);
        URLConnection conn = u.openConnection();
 
        // ContentType is an inner class defined below
        ContentType contentType = getContentTypeHeader(conn);
        if (!contentType.contentType.equals("text/html"))
            errorID = 1; // don't continue if not HTML
        else {
            // determine the charset, or use the default
            Charset charset = getCharset(contentType);
            if (charset == null)
                charset = Charset.defaultCharset();
 
            // read the response body, using BufferedReader for performance
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            int n = 0, totalRead = 0;
            char[] buf = new char[1024];
            StringBuilder content = new StringBuilder();
            int byteLength=0;
            if(url.contains("youtube.com")){
            	byteLength = 15360;
            }else if(url.contains("wikipedia.org")){
            	byteLength = 5120;
            }else if(url.contains("imgur.com")){
            	byteLength = 1024;
            }else if(url.contains("twitter.com")){
            	byteLength = conn.getContentLength();
            }else if(url.contains("reddit.com")){
            	byteLength = 25600;
            }else if(url.contains("forum/show")){
            	byteLength = 25600;
            }
            // read until EOF or first byteLength characters
            while (totalRead < byteLength && (n = reader.read(buf, 0, buf.length)) != -1) {
                content.append(buf, 0, n);
                totalRead += n;
            }
            reader.close();
            // extract the title
            if(url.contains("youtube")){
            	title="";
            	author="";
            	duration="";
            	Matcher titleMatch = TITLE_TAG.matcher(content);
	            Matcher authorMatch = AUTHOR_TAG.matcher(content);
	            Matcher durationMatch = DURATION.matcher(content);
	            if (titleMatch.find()) {
	                title = titleMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	            }
	            if(authorMatch.find()){
	            	author = authorMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	            }
	            if(durationMatch.find()){
	            	duration = durationMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	            }
            }else if(url.contains("wikipedia")){
            	wikiError="";
            	Matcher wikiMatch = WIKIERROR.matcher(content);
            	if(wikiMatch.find()){
            		wikiError = "Wikipedia does not have an article with this exact name.";
            		return;
            	}
            	urlPassed=url;
            }else if(url.contains("imgur")){
            	title="";
            	Matcher imgurTitle = TITLE_TAG.matcher(content);
            	if(imgurTitle.find()){
            		title = imgurTitle.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            }else if(url.contains("twitter")){
            	tweet="";
            	author="";
            	duration="";
            	Matcher messageMatch = TWEETMSG.matcher(content);
            	Matcher authorMatch = TWEETAUTHOR.matcher(content);
            	Matcher timeMatch = TWEETTIME.matcher(content);
            	if(authorMatch.find()){
            		author = authorMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            	if(timeMatch.find()){
            		duration = timeMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            	if(messageMatch.find()){
            		tweet = messageMatch.group(2).replaceAll("[\\s\\<>]+", " ").trim();
            	}      	
            }else if(url.contains("reddit")){
            	title="";
            	upvotes="";
            	downvotes="";
            	Matcher titleMatch = THREAD_TITLE.matcher(content);
            	Matcher upvoteMatch = THREAD_UPVOTES.matcher(content);
            	Matcher downvoteMatch = THREAD_DOWNVOTES.matcher(content);
            	if(titleMatch.find()){
            		title = titleMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            	if(upvoteMatch.find()){
            		upvotes = upvoteMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            	if(downvoteMatch.find()){
            		downvotes = downvoteMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            }else if(url.contains("forum/show")){
            	title="";
            	Matcher titleMatch = GENERIC_TITLE.matcher(content);
            	if(titleMatch.find()){
            		title = titleMatch.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            	}
            	
            }
        }
    }
    public int getError(){
    	return errorID;
    }
    public String getTitle(){
    	if(title.length()==0){
    		return "of unknown title.";
    	}
    	return strReplace(title);
    }
    public String getAuthor(){
    	if(author.length()==0){
    		return "an unknown author.";
    	}
    	return strReplace(author);
    }
    public String getDuration(){
    	if(duration.length()==0){
    		return "??";
    	}
    	return strReplace(duration);
    }
    public String getWikiError(){
    	if(wikiError.length()==0){
    		return urlPassed;
    	}
    	return strReplace(wikiError);
    }
    public String getTweet(){
    	if(tweet.length()==0){
    		return "unknown tweet";
    	}
    	return strReplace(tweet);
    }
    public String getVotes(){
    	if(upvotes.length()==0 || downvotes.length()==0){
    		return "unknown karma.";
    	}
    	return upvotes + "↑  " + downvotes + "↓";
    }
    
    private String strReplace(String string){
    	string = string.replace("&#039;", "'");
    	string = string.replace("&#39;", "'");
	    string = string.replace("&amp;", "&");
	    string = string.replace("&qt;", ">");
	    string = string.replace("&lt;", "<");
	    string = string.replace("&quot;", "\"");
	    string = string.replace("&apos;", "'");
	    string = string.replace("\\/", "/");
	    string = string.replace("\\\"", "\"");
    	return string;
    }
 
    /**
     * Loops through response headers until Content-Type is found.
     * @param conn
     * @return ContentType object representing the value of
     * the Content-Type header
     */
    private static ContentType getContentTypeHeader(URLConnection conn) {
        int i = 0;
        boolean moreHeaders = true;
        do {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);
            if (headerName != null && headerName.equals("Content-Type"))
                return new ContentType(headerValue);
 
            i++;
            moreHeaders = headerName != null || headerValue != null;
        }
        while (moreHeaders);
 
        return null;
    }
 
    private static Charset getCharset(ContentType contentType) {
        if (contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName))
            return Charset.forName(contentType.charsetName);
        else
            return null;
    }
 
    /**
     * Class holds the content type and charset (if present)
     */
    private static final class ContentType {
        private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
 
        private String contentType;
        private String charsetName;
        private ContentType(String headerValue) {
            if (headerValue == null)
                throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
            int n = headerValue.indexOf(";");
            if (n != -1) {
                contentType = headerValue.substring(0, n);
                Matcher matcher = CHARSET_HEADER.matcher(headerValue);
                if (matcher.find())
                    charsetName = matcher.group(1);
            }
            else
                contentType = headerValue;
        }
    }
}