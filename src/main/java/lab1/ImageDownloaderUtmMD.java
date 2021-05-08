package lab1;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloaderUtmMD {
    public static void main(String[] args) throws Exception {
        ImageDownloaderUtmMD imageDownloaderUtmMD = new ImageDownloaderUtmMD();
        //System.out.println(imageDownloaderUtmMD.getImagesUrl("utm.md", 443));
        imageDownloaderUtmMD.downloadImagesAsynchronous();
        //imageDownloaderUtmMD.getOneImage("/wp-content/uploads/2019/05/study-germany-erasmusplus.jpg");
    }

    public Set<String> getImagesUrl(String host, int port) throws Exception {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory
                .createSocket(host, port);
        PrintWriter wtr = new PrintWriter(sslsocket.getOutputStream());
        wtr.println("GET / HTTP/1.1");
        wtr.println("Host: utm.md");
        wtr.println("Connection: keep-alive");
        wtr.println("Accept-Language: ro,en");
        wtr.println("DNT: 1");
        wtr.println("Save-Data: <sd-token>");

        wtr.println("");
        wtr.flush();
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
        String outStr;

        List<String> allPaths = new ArrayList<>();
        while ((outStr = bufRead.readLine()) != null) {
            String urlRegex = "((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
            Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
            Matcher urlMatcher = pattern.matcher(outStr);

            while (urlMatcher.find()) {
                allPaths.add(outStr.substring(urlMatcher.start(0),
                        urlMatcher.end(0)));
            }
        }
        List<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < allPaths.size(); i++) {
            Pattern pattern2 = Pattern.compile("[^\"']*\\.(?:png|jpg|gif)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern2.matcher(allPaths.get(i));
            boolean matchFound = matcher.find();
            if (matchFound) {
                imagePaths.add(matcher.group());
            }
        }
        Set<String> listImagesUrl = new HashSet<>();
        imagePaths.forEach((photo) -> {
            photo = photo.replace("https://utm.md", "");
            listImagesUrl.add(photo);
        });
        bufRead.close();
        wtr.close();
        return listImagesUrl;
    }

    private void getOneImage(String path) {
        try {
            System.out.println("Thread: " + Thread.currentThread().getName() + " -> Starting to download the image: " + path);
            String domain = "utm.md";
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(domain, 443);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream())));
            //out.println("GET " + path + " HTTP/1.1\n" + "Host: " + domain + "\n");
            out.println("GET " + path + " HTTP/1.1\r\nHost: " + domain + " \r\n\r\n");
            out.flush();
            String filePath = "C:/Users/Maria/Desktop/imges/images_UTM_MD/" + getFileName(path);
            File file = new File(filePath);
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final InputStream inputStream = sslsocket.getInputStream();
            int count, offset;
            byte[] buffer = new byte[2048];
            boolean eohFound = false;
            while ((count = inputStream.read(buffer)) != -1) {
                offset = 0;
                if (!eohFound) {
                    String string = new String(buffer, 0, count);
                    int indexOfEOH = string.indexOf("\r\n\r\n");
                    if (indexOfEOH != -1) {
                        count = count - indexOfEOH - 4;
                        offset = indexOfEOH + 4;
                        eohFound = true;
                    } else {
                        count = 0;
                    }
                }
                fileOutputStream.write(buffer, offset, count);
                fileOutputStream.flush();
            }
            inputStream.close();
            fileOutputStream.close();
            sslsocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Thread: " + Thread.currentThread().getName() + " -> Downloading of the image: " + path + " finished successfully!");
    }

    private String getFileName(String filePath) {
        Pattern pattern = Pattern.compile("[^\\\\/:*?\"<>|\\r\\n]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filePath);
        boolean matchFound = matcher.find();
        if (matchFound) {
            return matcher.group();
        } else {
            return "fakefilename";
        }
    }

    private void downloadImagesAsynchronous() throws Exception {
        Semaphore semaphore = new Semaphore(2);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        Set<String> imagesUrls = getImagesUrl("utm.md", 443);
        for (String imagesUrl : imagesUrls) {
            executor.submit(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getOneImage(imagesUrl);
                semaphore.release();
            });
        }
    }
}


