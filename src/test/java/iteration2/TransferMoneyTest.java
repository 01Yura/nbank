package iteration2;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransferMoneyTest extends SetupTest {

    Stream<Arguments> argsFor_userCanTransferMoney() {
        return Stream.of(
                Arguments.of(1, 100, 500, 1.0F),
                Arguments.of(10000, 5000, 15000, 10000.0F),
                Arguments.of(9999, 5000, 10000, 9999.0F)
        );
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCanTransferMoney")
    void userCanTransferMoney(Integer transferAmount, Integer depositPerCycle,  Integer depositThreshold, Float expectedAmount) {
        System.err.println("--------------------userCanTransferMoney------------------------");

        Integer senderAccountId = accountIds.getFirst();
        Integer receiverAccountId = accountIds.getLast();

        //        Deposit money until the balance is enough for a successful transfer
        Float currentBalance = 0.0F;
        while (currentBalance < depositThreshold) {
            currentBalance = given()
                    .header("Authorization", userAuthToken)
                    .contentType(ContentType.JSON)
                    .body(String.format("""
                            {
                              "id": %s,
                              "balance": %d
                            }
                            """, senderAccountId, depositPerCycle))
                    .when()
                    .post("http://localhost:4111/api/v1/accounts/deposit")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .path("balance");
        }

//        Transfer money
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %d
                        }
                        """, senderAccountId, receiverAccountId, transferAmount))
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(200);

//        Check whether balance is equal as should on both accounts
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.balance", senderAccountId), equalTo(currentBalance - transferAmount))
                .body(String.format("find { it.id == %d }.balance", receiverAccountId), equalTo(expectedAmount));

    }

    Stream<Arguments> argsFor_userCannotTransferMoney() {
        return Stream.of(
//                Negative: Authorized user CANNOT transfer amount money from one account to another if this amount of money doesn't exist on their account
                Arguments.of(1000, 100, 200, 0.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount money from one account to another (-1)
                Arguments.of(-1, 1, 2, 0.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount money from one account to another (0)
                Arguments.of(0, 1, 2, 0.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount money from one account to another (10001)
                Arguments.of(10001, 5000, 11000, 0.0F, "Transfer amount cannot exceed 10000")

        );
    }

    @ParameterizedTest
    @MethodSource("argsFor_userCannotTransferMoney")
    void userCannotTransferMoney(Integer transferAmount, Integer depositPerCycle,  Integer depositThreshold, Float expectedAmount, String error) {
        System.err.println("--------------------userCannotTransferMoney------------------------");

        Integer senderAccountId = accountIds.getFirst();
        Integer receiverAccountId = accountIds.getLast();

        //        Deposit money until the balance is sufficient for a successful transfer
        Float currentBalance = 0.0F;
        while (currentBalance < depositThreshold) {
            currentBalance = given()
                    .header("Authorization", userAuthToken)
                    .contentType(ContentType.JSON)
                    .body(String.format("""
                            {
                              "id": %s,
                              "balance": %d
                            }
                            """, senderAccountId, depositPerCycle))
                    .when()
                    .post("http://localhost:4111/api/v1/accounts/deposit")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .path("balance");
        }

//        Transfer money
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %d
                        }
                        """, senderAccountId, receiverAccountId, transferAmount))
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(400)
                .body(Matchers.containsString(error));


//        Check whether balance is equal as should on both accounts
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.balance", senderAccountId), equalTo(currentBalance))
                .body(String.format("find { it.id == %d }.balance", receiverAccountId), equalTo(expectedAmount));


    }


}
