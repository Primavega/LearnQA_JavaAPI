import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    //Ex8
    @Test
    public void testGetToken() throws InterruptedException {
        String status;
        String result;

        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String token = response.getString("token");
        int seconds = response.getInt("seconds");

        status = executeRequest(token).getString("status");

        if(status.equals("Job is NOT ready")){
            System.out.println(status);
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        }

        response = executeRequest(token);

        status = response.getString("status");
        result = response.getString("result");
        if(status.equals("Job is ready") &&  result != null){
            System.out.println("\nresult " + result);
        } else {
            System.out.println("\nSomething wrong");
        }
    }

    private JsonPath executeRequest(String token){
        return RestAssured
                .given()
                .param("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();
    }

    //Ex9
    @Test
    public void testGetPassword() {

        Set<String> passwords = getPasswords();

        for (String pass : passwords) {

            Map<String, String> data = new HashMap<>();
            data.put("login", "super_admin");
            data.put("password", pass);

            Response responseForGet = RestAssured
                    .given()
                    .body(data)
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String responseCookie = responseForGet.getCookie("auth_cookie");
            Map<String, String> cookies = new HashMap<>();
            if (responseCookie != null) {
                cookies.put("auth_cookie", responseCookie);
            }
            Response responseForCheck = RestAssured
                    .given()
                    .body(data)
                    .cookies(cookies)
                    .when()
                    .post("https://playground.learnqa.ru/api/check_auth_cookie")
                    .andReturn();

            if(!responseForCheck.htmlPath().getString("html.body").equals("You are NOT authorized")){
                responseForCheck.print();
                System.out.println("\nRight password is: " + pass);
                return;
            }
        }
    }

    private Set<String> getPasswords(){
        Set<String> passwords = new LinkedHashSet<>();
        String filePath = "src/test/java/input.txt"; // Путь к файлу с входными данными

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    passwords.add(word.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return passwords;
    }

    // HomeWork3
    // Ex10
    @Test
    public void testStringLength(){
        String string = "Поздравляю с 23 февраля!";
        assertTrue(string.length() > 15, "String length is less then 15");
    }

    //Ex11
    @Test
    public void testGetCookie(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        String cookie = response.getCookie("HomeWork");
        assertEquals(cookie, "hw_value", "Cookie value " + cookie + " is not correct");
    }

    //Ex12
    @Test
    public void testGetHeader(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        String header = response.getHeader("x-secret-homework-header");
        assertEquals(header, "Some secret value", "Header value " + header + " is not correct");
    }
}