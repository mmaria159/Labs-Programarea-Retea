import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jsoup.Connection.Method.*;
import static org.jsoup.Jsoup.connect;

public class RequestsToAndys {
    private static Map<String, String> cookies = Authentification.getCookies();

    public static String headRequest() throws IOException {
        Response resp = connect("https://www.andys.md/ro/account").method(HEAD).cookies(cookies).execute();
        return resp.contentType();
    }

    public static Map<String, List<String>> optionsRequest() throws IOException {
        Response resp = connect("https://www.andys.md/ro/account").method(OPTIONS).cookies(cookies).execute();
        return resp.multiHeaders();
    }

    public static String searchMyAccountName(String link) throws Exception {
        Response resp = connect(link).method(GET).cookies(cookies).execute();
        String text = resp.body();
        Pattern pattern = Pattern.compile("Macovei Maria", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return "The name of account was found in the page, this is: " + matcher.group();
        }
        throw new Exception("The name of account wasn't found in this page");
    }

    public static void serchEmails() throws Exception {
        Response resp = connect("https://www.andys.md/ro/account").method(GET).cookies(cookies).execute();
        String text = resp.body();
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(text);
        Set<String> emails = new HashSet<String>();
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        System.out.println(emails);
    }

    public static void getLinks() throws Exception {
        Document doc;
        doc = Jsoup.connect("https://www.andys.md").get();
        Elements elements = doc.select("a[href]");
        Set<String> links = new HashSet<String>();
        for (Element e : elements) {
            links.add(e.attr("abs:href"));
        }
        System.out.println("\n" + links);
    }

    public static void getAllImages(String link) throws IOException, InterruptedException {
        Document page = Jsoup.connect(link).cookies(cookies).get();
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(5);
        Elements imageElements = page.getElementsByTag("img");
        for (Element element : imageElements) {
            exec.submit(() -> {
                DownoaldImages.downloadImage(element.absUrl("src"));
                latch.countDown();
                System.out.println(Thread.currentThread().getName());

            });
        }
        latch.await();
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\nContent-Type: " + headRequest());
        System.out.println("\nOptions response: " + optionsRequest());
        System.out.println("\n" + searchMyAccountName("https://www.andys.md/ro/account"));
        System.out.println("\n" + "The list of all links :");
        getLinks();
        System.out.println("\n" + "The list of all emails :");
        serchEmails();
        System.out.println("\n" + "The list of all images :");
        getAllImages("https://www.andys.md/ro/catalog");

    }
}
