package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

//лекция 4
public class UserRegisterTest extends BaseTestCase {

    @Test
    public void testCreateUserWithExistingEmail(){
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }

    @Test
    public void testCreateUserSuccessfully(){

        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        String[] expectedFields = {"username","firstName","lastName","email"};
        Assertions.assertJsonHasNotFields(responseCreateAuth, expectedFields);
    }

    @Test
    public void testCreateUserWithIncorrectEmail(){
        String email = DataGenerator.getRandomIncorrectEmail();

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }

    @ParameterizedTest
    @MethodSource("provideData")
    public void testCreateUserWithoutParameter(Map<String, String> userData){

         Response responseCreateAuth = apiCoreRequests.makePostRequest(
                 baseUrl + "user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }

    private static Stream<Map<String, String>> provideData() {
        Map<String, String> data1 = new HashMap<>();
        data1.put("email", null);
        data1 = DataGenerator.getRegistrationData(data1);

        Map<String, String> data2 = new HashMap<>();
        data2.put("password", null);
        data2 = DataGenerator.getRegistrationData(data2);

        Map<String, String> data3 = new HashMap<>();
        data3.put("username", null);
        data3 = DataGenerator.getRegistrationData(data3);

        Map<String, String> data4 = new HashMap<>();
        data4.put("firstName", null);
        data4 = DataGenerator.getRegistrationData(data4);

        Map<String, String> data5 = new HashMap<>();
        data5.put("lastName", null);
        data5 = DataGenerator.getRegistrationData(data5);

        return Stream.of(data1, data2, data3, data4, data5);
    }

    @Test
    public void testCreateUserWithShortName(){
        String name = DataGenerator.getRandomUsername(1);

        Map<String, String> userData = new HashMap<>();
        userData.put("username", name);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too short");
        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }

    @Test
    public void testCreateUserWithLongName(){
        String name = DataGenerator.getRandomUsername(251);

        Map<String, String> userData = new HashMap<>();
        userData.put("username", name);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        System.out.println(responseCreateAuth.asString());
        System.out.println(responseCreateAuth.statusCode());
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too long");
        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }
}
