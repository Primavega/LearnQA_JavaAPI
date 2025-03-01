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

@Epic("Edit user cases")
@Feature("User editing")
public class UserEditTest extends BaseTestCase {

    @Test
    @Description("This test edit just created user by himself")
    @DisplayName("Test edit created user")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditJustCreatedTest(){

        //create user
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //login
        Map<String, String> authData = DataGenerator.getAuthData(userData);
        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //edit
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(
                baseUrl + "user/" + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData
        );

        Assertions.assertResponseCodeEquals(responseEditUser, 200);

        //get
        Response responseUserData = apiCoreRequests.makeGetRequest(
                baseUrl + "user/" + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid")
        );

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Description("This test tries to edit user without authorization")
    @DisplayName("Test edit user without auth")
    @Severity(SeverityLevel.BLOCKER)
    public void testEditUserNotAuth(){

        //create user
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //try edit
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(
                baseUrl + "user/" + userId,
                editData
        );

        Assertions.assertJsonByName(responseEditUser, "error", "Auth token not supplied");
        Assertions.assertResponseCodeEquals(responseEditUser, 400);
    }

    @Test
    @Description("This test tries to edit user by another user")
    @DisplayName("Test edit user by another user")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-12346")
    public void testEditUserAsAnotherUser(){

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

        //try edit user2
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(
                baseUrl + "user/" + user2Id,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData
        );

        Assertions.assertJsonByName(responseEditUser, "error", "This user can only edit their own data.");
        Assertions.assertResponseCodeEquals(responseEditUser, 400);
    }

    @Test
    @Description("This test tries to edit user using email without @")
    @DisplayName("Test edit user using incorrect email")
    public void testEditUsingIncorrectEmail(){

        //create user
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //login
        Map<String, String> authData = DataGenerator.getAuthData(userData);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //edit
        String newEmail = DataGenerator.getRandomIncorrectEmail();
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        Response responseEditUser = apiCoreRequests.makePutRequest(
                baseUrl + "user/" + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData
        );

        Assertions.assertJsonByName(responseEditUser, "error", "Invalid email format");
        Assertions.assertResponseCodeEquals(responseEditUser, 400);
    }

    @Test
    @Description("This test tries to edit user using too short username")
    @DisplayName("Test edit user using short name")
    public void testEditUsingShortUsername(){

        //create user
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //login
        Map<String, String> authData = DataGenerator.getAuthData(userData);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //edit
        String newUsername = DataGenerator.getRandomUsername(1);
        Map<String, String> editData = new HashMap<>();
        editData.put("username", newUsername);

        Response responseEditUser = apiCoreRequests.makePutRequest(
                baseUrl + "user/" + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData
        );

        Assertions.assertJsonByName(responseEditUser, "error", "The value for field `username` is too short");
        Assertions.assertResponseCodeEquals(responseEditUser, 400);
    }
}
