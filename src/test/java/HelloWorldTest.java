import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.junit.jupiter.api.Test;

public class HelloWorldTest {

    @Test
    public void testGetText(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        //вывод ответа вместе с тэгами
        response.prettyPrint();

        //достаем текстовое содержимое
        String text = response.htmlPath().getString("html.body");
        System.out.println(text);
    }

    //HomeWork2
    //Ex5
    @Test
    public void testGetSecondMessage() {
        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String message = response.getString("messages[1].message");

        if (message == null){
            System.out.println("Сообщение не найдено");
        } else {
            System.out.println(message);
        }
    }

    //Ex6
    @Test
    public void testGetRedirectLink() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        String redirectLink = response.getHeader("Location");
        System.out.println(redirectLink);
    }

    //Ex7
    @Test
    public void testGetRedirectsNumber() {

        int statusCode = 0;
        int redirectsNumber = 0;
        String link = "https://playground.learnqa.ru/api/long_redirect";

        while(statusCode != 200) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(link)
                    .andReturn();

            statusCode = response.getStatusCode();
            System.out.println("\nStatus Code " + statusCode);

            if(statusCode != 200) {
                link = response.getHeader("Location");
                System.out.println("\nRedirect to " + link);
                redirectsNumber++;
            } else {
                System.out.println("\nRedirects Number " + redirectsNumber);
            }
        }
    }
}