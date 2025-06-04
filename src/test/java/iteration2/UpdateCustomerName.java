package iteration2;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class UpdateCustomerName extends SetupTest {

    Stream<Arguments> argsFor_userCannotChangeTheirNameUsingInvalidName() {
        return Stream.of(
//                Negative: Authorized user CANNOT change their name using invalid value (one word)
                Arguments.of("Newname"),
//                Negative: Authorized user CANNOT change their name using invalid value (+ one digit)
                Arguments.of("New name1"),
//                Negative: Authorized user CANNOT change their name using invalid value (+ one special symbol)
                Arguments.of("New name!"),
//                Negative: Authorized user CANNOT change their name using invalid value (empty string)
                Arguments.of("")
        );
    }

    @Test
    void userCanChangeTheirNameUsingValidName() {
        System.err.println("--------------------userCanChangeTheirNameUsingValidName-------------------------");

//        Check that after user creation their name is null
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(userId))
                .body("name", Matchers.nullValue());

//        Change customer name
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "New name"
                        }""")
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("customer.id", Matchers.equalTo(userId))
                .body("customer.name", Matchers.equalTo("New name"));
    }

    @ParameterizedTest
    @MethodSource("argsFor_userCannotChangeTheirNameUsingInvalidName")
    void userCannotChangeTheirNameUsingInvalidName(String newName) {
        System.err.println("--------------------userCannotChangeTheirNameUsingInvalidName-------------------------");

//        Check that after user creation their name is null
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(userId))
                .body("name", Matchers.nullValue());

//        Change customer name
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                          "name": %s
                        }""", newName))
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(400);


        //        Check that after changing name their name is still null
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(userId))
                .body("name", Matchers.nullValue());

    }
}
