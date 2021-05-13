package lab3;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;

import static org.jsoup.Jsoup.*;

public class Authentification {

    final static  String authUser = "uC8ADg1n";
    final static  String authPassword = "wWwVs3jr";

    public static Map<String, String> getCookies() {
        Authenticator.setDefault(
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyHost", "45.152.116.114");
        System.setProperty("http.proxyPort", "1623");
        try {
            return connect("https://www.andys.md/ro/login")
                    .method(Connection.Method.POST)
                    .data("email", "exempleuser007@gmail.com")
                    .data("password", "Anonim_007")
                    .execute().cookies();
        } catch (IOException e) {
            throw new RuntimeException("login error");
        }
    }
}

