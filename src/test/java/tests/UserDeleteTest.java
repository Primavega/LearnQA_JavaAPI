package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Delete user cases")
@Feature("User deletion")
public class UserDeleteTest extends BaseTestCase {

    @Test
    @Description("This test tries to delete user with ID = 2 that is protected form deletion")
    @DisplayName("Test delete protected user")
    public void testDeleteProtectedUser(){

        //login
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //try delete
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(
                baseUrl + "user/2",
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid")
        );

        Assertions.assertJsonByName(responseDeleteUser, "error", "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
    }

    @Test
    @Description("This test delete user that was just created by himself")
    @DisplayName("Test delete  just created user")
    public void testDeleteJustCreatedUser(){

        //create user
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //login
        Map<String, String> authData = DataGenerator.getAuthData(userData);
        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //delete
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(
                baseUrl + "user/" + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid")
        );
        Assertions.assertResponseCodeEquals(responseDeleteUser, 200);
        //get
        Response responseUserData = apiCoreRequests.makeGetRequest(
                baseUrl + "user/" + userId
        );

        System.out.println(responseUserData.asString());
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    @Test
    @Description("This test tries to delete user by another user")
    @DisplayName("Test delete user by another user")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-12345")
    public void testDeleteUserAsAnotherUser(){

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
        Map<String, String> authData = DataGenerator.getAuthData(userData);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //try delete user2
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(
                baseUrl + "user/" + user2Id,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid")
        );

        Assertions.assertJsonByName(responseDeleteUser, "error", "This user can only delete their own account.");
        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
    }
}
