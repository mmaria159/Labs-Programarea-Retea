package lab3;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

import static org.jsoup.Jsoup.*;

public class Authentification {

    /*  public static Map<String, String> getAccountPage () {
           Response response = null;
           try {

               response = connect("https://www.andys.md/ro/login")
                       .method(Connection.Method.POST)
                       .data("email", "exempleuser007@gmail.com")
                       .data("password", "Anonim_007")
                       .execute();
               Document homePage = connect("https://www.andys.md/ro/account")
                       .cookies(response.cookies())
                       .get();
               System.out.println(response.body());
               System.out.println(response.cookies());
               System.out.println(homePage);
           } catch (IOException e) {
               e.printStackTrace();
           }
           return response.cookies();
       }*/

    public static Map<String, String> getCookies() {
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

