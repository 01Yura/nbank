package iteration1;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateUserTest extends SetupTest {

    static Stream<Arguments> argsFor_AdminCannotCreateUserWithInvalidCredentials() {
        return Stream.of(
//            Username field validation:
//            Negative: Authorized admin cannot create user that already exist
                Arguments.of("TestUser1", "TestUser1!", "USER", "Error: Username 'TestUser1' already exists."),
//            Negative: Authorized admin cannot create new user with blank username
                Arguments.of("", "TestUser2!", "USER", "Username cannot be blank"),
//             Negative: Authorized admin cannot create new user with username consists of 2 characters
                Arguments.of("Te", "TestUser3!", "USER", "Username must be between 3 and 15 characters"),
//             Negative: Authorized admin cannot create new user with username consists of 16 characters
                Arguments.of("TestUserUserUser", "TestUser4!", "USER", "Username must be between 3 and 15 characters"),
//             Negative: Authorized admin cannot create new user with username has invalid characters (@#$%^&*)
                Arguments.of("TestUser5#", "TestUser5!", "USER", "Username must contain only letters, digits, dashes, underscores, and dots"),

//             Role field validation:
//             Negative: Authorized admin cannot create new user with role is not from list
        Arguments.of("TestUser6", "TestUser16", "SUPERADMIN", "Role must be either 'ADMIN' or 'USER'"),

//             Password field validation:
//             Negative: Authorized admin cannot create new user with valid username and invalid password (less then 8 symbols)
                Arguments.of("TestUser7", "Seven7!", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with valid username and invalid password (without special characters)
                Arguments.of("TestUser8", "NoSpecial1", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with valid username and invalid password (without an uppercase letter)
                Arguments.of("TestUser9", "nouppercase1!", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with valid username and invalid password (without a lowercase letter)
                Arguments.of("TestUser10", "NOLOWERCASE1!", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with valid username and invalid password (without digits)
                Arguments.of("TestUser11", "NoNumber!", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with valid username and invalid password (with spaces)
                Arguments.of("TestUser12", "With spaces1!", "USER", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
//             Negative: Authorized admin cannot create new user with username with blank password
                Arguments.of("TestUser13", "", "USER", "Password cannot be blank")
        );
    }

    @Order(1)
    @Test
    void adminCanCreateUserWithValidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "TestUser1",
                          "password": "TestUser1!",
                          "role": "USER"
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(201)
                .body("username", equalTo("TestUser1"))
                .body("password", notNullValue())
                .body("role", equalTo("USER"));
    }

    @ParameterizedTest
    @MethodSource("argsFor_AdminCannotCreateUserWithInvalidCredentials")
    void adminCannotCreateUserWithInvalidCredentials(String username, String password, String role, String expectedError) {
        String requestBody = String.format("""
                {
                  "username": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """, username, password, role);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(400)
                .body(Matchers.containsString(expectedError));



    }
}
