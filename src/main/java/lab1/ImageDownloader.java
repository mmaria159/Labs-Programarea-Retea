package lab1;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloader {

    public static void main(String[] args) throws Exception {

        ImageDownloader imageDownloader = new ImageDownloader();
        imageDownloader.downloadImagesAsynchronous();
        //System.out.println(imageDownloader.getImagesUrl("me.utm.md",80));

    }

    public List<String> getImagesUrl(String url, int port) throws Exception {

        Socket s = new Socket(url, port);
        PrintWriter wtr = new PrintWriter(s.getOutputStream());
        wtr.println("GET / HTTP/1.1");
        wtr.println("Host: me.utm.md");
        wtr.println("Connection: keep-alive");
        wtr.println("Accept-Language: ro,en");
        wtr.println("DNT: 1");
        wtr.println("Save-Data: <sd-token>");
        wtr.println("");
        wtr.flush();
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String outStr;
        List<String> imagePaths = new ArrayList<>();
        while ((outStr = bufRead.readLine()) != null) {
            Pattern pattern = Pattern.compile("[^\"']*\\.(?:png|jpg|gif)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(outStr);
            boolean matchFound = matcher.find();
            if (matchFound) {
                imagePaths.add(matcher.group());
            }
        }

        List<String> listImagesUrl = new ArrayList<>();
        imagePaths.forEach((photo) -> {
            if (photo.startsWith("http://mib.utm.md")) {
                listImagesUrl.add(photo.replace("http://mib.utm.md", ""));
            } else {
                listImagesUrl.add("/" + photo);
            }
        });
        bufRead.close();
        wtr.close();
        return listImagesUrl;
    }

    //descarcam o imagine cu o solicitate HTTP folosind socket
    private void getOneImage(String path) {
        try {
            System.out.println("Thread: " + Thread.currentThread().getName() + " -> Starting to download the image: " + path);
            String domain = "me.utm.md";
            Socket socket = new Socket(domain, 80);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println("GET " + path + " HTTP/1.1\n" + "Host: " + domain + "\n");
            out.println();
            out.flush();
            String filePath = "C:/Users/Maria/Desktop/imges/images_ME_UTM_MD/" + getFileName(path);
            File file = new File(filePath);
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final InputStream inputStream = socket.getInputStream();
            boolean headerEnded = false;
            byte[] bytes = new byte[2048];
            int length;
            while ((length = inputStream.read(bytes)) != -1) {
                if (headerEnded)
                    fileOutputStream.write(bytes, 0, length);
                else {
                    for (int i = 0; i < 2045; i++) {
                        if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                            headerEnded = true;
                            fileOutputStream.write(bytes, i + 4, 2048 - i - 4);
                            break;
                        }
                    }
                }
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
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
        List<String> imagesUrls = getImagesUrl("me.utm.md", 80);
        for (String imagesUrl : imagesUrls) {
            executor.submit(() -> {
                try {
                    semaphore.acquire();
                    getOneImage(imagesUrl);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
