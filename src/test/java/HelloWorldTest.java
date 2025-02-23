import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

    //Ex13
    @ParameterizedTest
    @MethodSource("provideData")
    public void testUserAgent(Map<String, String> data){

        RequestSpecification spec = RestAssured.given();
        spec.baseUri("https://playground.learnqa.ru/ajax/api/user_agent_check");
        spec.header("User-Agent", data.get("user_agent"));
        JsonPath response = spec.get().jsonPath();

        Map<String, String> responseData = new HashMap<>();
        responseData.put("user_agent", response.get("user_agent"));
        responseData.put("platform", response.get("platform"));
        responseData.put("browser", response.get("browser"));
        responseData.put("device", response.get("device"));

        assertEquals(data, responseData, "Error detected");
    }

    private static Stream<Map<String, String>> provideData() {
        Map<String, String> data1 = new HashMap<>();
        data1.put("user_agent", "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        data1.put("platform", "Mobile");
        data1.put("browser", "No");
        data1.put("device", "Android");

        Map<String, String> data2 = new HashMap<>();
        data2.put("user_agent", "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1");
        data2.put("platform", "Mobile");
        data2.put("browser", "Chrome");
        data2.put("device", "iOS");

        Map<String, String> data3 = new HashMap<>();
        data3.put("user_agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        data3.put("platform", "Googlebot");
        data3.put("browser", "Unknown");
        data3.put("device", "Unknown");

        Map<String, String> data4 = new HashMap<>();
        data4.put("user_agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0");
        data4.put("platform", "Web");
        data4.put("browser", "Chrome");
        data4.put("device", "No");

        Map<String, String> data5 = new HashMap<>();
        data5.put("user_agent", "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
        data5.put("platform", "Mobile");
        data5.put("browser", "No");
        data5.put("device", "iPhone");

        return Stream.of(data1, data2, data3, data4, data5);
    }
}