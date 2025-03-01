package tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Get user data cases")
@Feature("User reading")
public class UserGetTest extends BaseTestCase {

    @Test
    @Description("This test tries to get user data without authorization")
    @DisplayName("Test get user without auth")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserDataNotAuth(){
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl + "user/2");

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }

    @Test
    @Description("This test gets user data by himself")
    @DisplayName("Test get user data successfully")
    public void testGetUserDetailsAuthAsSameUser(){

        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(
                baseUrl + "user/2", header, cookie);

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasField(responseUserData, "firstName");
        Assertions.assertJsonHasField(responseUserData, "lastName");
        Assertions.assertJsonHasField(responseUserData, "email");
    }

    @Test
    @Description("This test tries to get user data without permission")
    @DisplayName("Test get user data by another user")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserDetailsAuthAsAnotherUser(){

        //create user1
        Map<String, String> userData =  DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);

        //create user2
        Map<String, String> userData2 =  DataGenerator.getRegistrationData();
        Response responseCreateAuth2 = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData2);
        String user2Id = responseCreateAuth2.jsonPath().getString( "id");

        //login as user1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //get user2
        Response responseUserData = apiCoreRequests.makeGetRequest(
                baseUrl + "user/" + user2Id,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid")
        );

        Assertions.assertJsonHasField(responseUserData, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }

}
