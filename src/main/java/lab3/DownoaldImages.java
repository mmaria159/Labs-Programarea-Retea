package lab3;

import java.io.*;
import java.net.URL;

public class DownoaldImages extends Thread {
    public static void downloadImage(String strImageURL) {

        String strImageName =
                strImageURL.substring(strImageURL.lastIndexOf("/") + 1);

        System.out.println("Download image : " + strImageName + ", from: " + strImageURL);

        try {

            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n = -1;

            OutputStream os =
                    new FileOutputStream("src\\\\main\\\\resources\\\\images" + "\\\\" + strImageName);

            while ((n = in.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }

            os.close();

            System.out.println("Image "+ strImageName + " saved");

        } catch (IOException e) {
            System.err.println("Image not found: " + strImageURL);
        }

    }

}
