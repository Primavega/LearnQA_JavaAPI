package tests;

import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest extends BaseTestCase {

    @Test
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
    public void testEditUserAsAnotherUser(){

        //create user1
        Map<String, String> userData =  DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData);
        String userId = responseCreateAuth.jsonPath().getString( "id");

        //create user2
        Map<String, String> userData2 =  DataGenerator.getRegistrationData();
        Response responseCreateAuth2 = apiCoreRequests.makePostRequest(
                baseUrl + "user/", userData2);
        String user2Id = responseCreateAuth2.jsonPath().getString( "id");

        //login as user1
        Map<String, String> authData = DataGenerator.getAuthData(userData);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                baseUrl + "user/login", authData);

        //try edit as user1
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
