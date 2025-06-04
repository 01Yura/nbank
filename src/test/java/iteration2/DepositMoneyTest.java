package iteration2;

import io.restassured.http.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class DepositMoneyTest extends SetupTest {

    Stream<Arguments> argsFor_userCanDepositMoney() {
        return Stream.of(
//                Positive: Authorized user can deposit valid amount of money to their account (1)
                Arguments.of(1, 1.0F),
//                Positive: Authorized user can deposit valid amount of money to their account (4999)
                Arguments.of(4999, 4999.0F),
//                Positive: Authorized user can deposit valid amount of money to their account (5000)
                Arguments.of(5000, 5000.0F)
        );
    }

    Stream<Arguments> argsFor_userCannotDepositMoney() {
        return Stream.of(
//                Negative: Authorized user CANNOT deposit money to their account if amount of money is negative number
                Arguments.of(-1, "Invalid account or amount"),
//                Negative: Authorized user CANNOT deposit money to their account if amount of money is 0
                Arguments.of(0, "Invalid account or amount"),
//                Negative: Authorized user CANNOT deposit money to their account if amount of money is more than 5000 (5001)
                Arguments.of(5001, "Deposit amount exceeds the 5000 limit")
        );
    }

    @ParameterizedTest
    @MethodSource("argsFor_userCanDepositMoney")
    void userCanDepositMoney(Integer depositBalance, Float expectedBalance) {
        System.err.println("--------------------userCanDepositMoney------------------------");
        Integer accountId = accountIds.getFirst();

//        Deposit money
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                          "id": %s,
                          "balance": %d
                        }
                        """, accountId, depositBalance))
                .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(200);

//        Check whether balance is equal as should
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.balance", accountId), equalTo(expectedBalance));

    }

    @ParameterizedTest
    @MethodSource("argsFor_userCannotDepositMoney")
    void userCannotDepositMoney(Integer depositBalance, String expextedError) {
        System.err.println("--------------------userCannotDepositMoney------------------------");
//        Deposit money
        Integer accountId = accountIds.getFirst();

        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                          "id": %s,
                          "balance": %d
                        }
                        """, accountId, depositBalance))
                .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(400)
                .body(containsString(expextedError)).toString();

//        Check whether the balance is equal as should
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.balance", accountId), equalTo(0.0F));
    }
}
