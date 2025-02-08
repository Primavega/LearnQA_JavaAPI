import io.restassured.RestAssured;
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
}