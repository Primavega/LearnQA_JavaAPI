package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import lib.Assertions;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import lib.ApiCoreRequests;

@Epic("Authorization cases")
@Feature("Authorization")
public class UserAuthTest extends BaseTestCase {

    String cookie;
    String header;
    int userIdOnAuth;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email" ,"vinkotov@example.com");
        authData.put("password" ,"1234");
        Response responseGetAuth = apiCoreRequests
                .makePostRequest(baseUrl + "user/login", authData);

        this.cookie = this.getCookie(responseGetAuth,"auth_sid");
        this.header = this.getHeader(responseGetAuth, "x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    @Description("This test successfully authorize user by email and password")
    @DisplayName("Test positive auth user")
    @Severity(SeverityLevel.CRITICAL)
    @Epic("Authorization cases")
    @Feature("Authorization")
    public void testAuthUser(){

        Response pesponseCheckAuth = apiCoreRequests
                .makeGetRequest(baseUrl + "user/auth", this.header, this.cookie);

        Assertions.assertJsonByName(pesponseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @Description("This test checks authorization status w/o sending auth cookie or token")
    @DisplayName("Test negative auth user")
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    @Severity(SeverityLevel.BLOCKER)
    public void testNegativeAuthUser(String condition) {
        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(
                    baseUrl + "user/auth",
                    this.cookie
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(
                    baseUrl + "user/auth",
                    this.header
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condithion value is unknown: " + condition);
        }
    }
}
