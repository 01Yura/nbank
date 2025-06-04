package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetupTest {

    protected String userAuthToken;
    protected Integer userId;
    //    Create the list of accounts, because we will create two accounts
    protected List<Integer> accountIds = new ArrayList<>();

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.filters(new RequestLoggingFilter());
        RestAssured.filters(new ResponseLoggingFilter());
    }


    @BeforeEach
    void beforeEachTest() {
//        Create a new user and store their auth token and id
        System.err.println("----------------------Create a new user and store their auth token----------------------");
        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                        "username": "Test_User1",
                        "password": "Test_User1!",
                        "role": "USER"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .response();

        userAuthToken = response.getHeader("Authorization");
        userId = response.path("id");

//        Create two new accounts
        for (int i = 0; i < 2; i++) {
            System.err.println("-----------------------Create new account---------------------");
            Integer accountId = given()
                    .header("Authorization", userAuthToken)
                    .when()
                    .post("http://localhost:4111/api/v1/accounts")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("balance", Matchers.equalTo(0.0F))
                    .body("transactions", Matchers.empty())
                    .extract()
                    .path("id");

//            Add account's id into our list
            accountIds.add(accountId);
        }
    }

    @AfterEach
    void afterEachTest() {
        System.err.println("----------------------Delete user----------------------");
//        Delete user
        given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .when()
                .delete(String.format("http://localhost:4111/api/v1/admin/users/%d", userId))
                .then()
                .assertThat()
                .statusCode(200)
                .body(Matchers.equalTo(String.format("User with ID %d deleted successfully.", userId)));

//        Clear list of accounts
        accountIds.clear();
    }
}
