package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class LoginUserTest extends SetupTest{

    @Test
    void adminCanLoginWithValidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
    }

    @Test
    void userCanLoginWithValidCredentials() {
//        Create user
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic VGVzdFVzZXIxOlRlc3RVc2VyMSE=")
                .when()
                .body("""
                        {
                          "username": "TestUser1",
                          "password": "TestUser1!",
                          "role": "USER"
                        }
                        """)
                .then()
                .that()
                .statusCode(HttpStatus.SC_CREATED);

//      User can log in using valid credentials and get auth token
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "TestUser1",
                          "password": "TestUser1!"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", Matchers.notNullValue());
    }
}
