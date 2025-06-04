package iteration1;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class CreateAccountTest extends SetupTest {
    @Test
    void userCanCreateAccount() {
//      Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "TestUser15",
                          "password": "TestUser15!",
                          "role": "USER"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(201);

//      User can log in using valid credentials and get auth token
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "TestUser15",
                          "password": "TestUser15!"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

//      Creation account
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(201);

    }
}
