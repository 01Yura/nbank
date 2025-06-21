package iteration2_senior_level;

import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import senior.models.*;
import senior.requests.skeleton.requesters.CrudRequester;
import senior.requests.skeleton.requesters.Endpoint;
import senior.requests.skeleton.requesters.ValidatedCrudRequester;
import senior.requests.steps.UserSteps;
import senior.specs.RequestSpecs;
import senior.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    static Stream<Arguments> argsFor_userCanDepositMoney() {
        return Stream.of(
                //                Positive: Authorized user can deposit a valid amount of money to their account (1)
                Arguments.of(1.0F),
                //                Positive: Authorized user can deposit a valid amount of money to their account (4999)
                Arguments.of(4999.0F),
                //                Positive: Authorized user can deposit a valid amount of money to their account (5000)
                Arguments.of(5000.0F)
        );
    }


    static Stream<Arguments> argsFor_userCannotDepositMoney() {
        return Stream.of(
                //                Negative: Authorized user CANNOT deposit money to their account if amount of money is negative number
                Arguments.of(-0.01F, "Invalid account or amount"),
                //                Negative: Authorized user CANNOT deposit money to their account if amount of money is 0
                Arguments.of(0.0F, "Invalid account or amount"),
                //                Negative: Authorized user CANNOT deposit money to their account if amount of money is more than 5000 (5001)
                Arguments.of(5000.01F, "Deposit amount exceeds the 5000 limit")
        );
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCanDepositMoney")
    void userCanDepositMoney(Float depositBalance) {
//        Create user + account
        CreateAccountResponseModel createAccountResponseModel = UserSteps.createAccount();

        //        Deposit money
        UserDepositMoneyRequestModel userDepositMoneyRequestModel = UserDepositMoneyRequestModel.builder()
                .id(createAccountResponseModel.getId())
                .balance(depositBalance)
                .build();

        UserDepositMoneyResponseModel userDepositMoneyResponseModel = new ValidatedCrudRequester<UserDepositMoneyResponseModel>(
                RequestSpecs.userSpec(
                        UserSteps.getCreateUserRequestModel().getUsername(),
                        UserSteps.getCreateUserRequestModel().getPassword()),
                ResponseSpecs.responseReturns200Spec(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .post(userDepositMoneyRequestModel);

        //        Check whether the balance is equal as should
        List<GetCustomerAccountsResponseModel> accounts = new CrudRequester(
                RequestSpecs.userSpec(
                        UserSteps.getCreateUserRequestModel().getUsername(),
                        UserSteps.getCreateUserRequestModel().getPassword()),
                ResponseSpecs.responseReturns200Spec(),
                Endpoint.CUSTOMER_ACCOUNTS)
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                });

        GetCustomerAccountsResponseModel account = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(userDepositMoneyResponseModel.getAccountNumber()))
                .findFirst().get();

        String transactionType = account.getTransactions().getFirst().getType();
        Long relatedAccountId = account.getTransactions().getFirst().getRelatedAccountId();


        softly.assertThat(userDepositMoneyResponseModel.getBalance()).isEqualTo(account.getBalance());
        softly.assertThat(transactionType).isEqualTo(TransactionType.DEPOSIT.toString());
        softly.assertThat(relatedAccountId).isEqualTo(userDepositMoneyRequestModel.getId());
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCannotDepositMoney")
    void userCannotDepositMoney(Float depositBalance, String expectedError) {
        //        Create user + account
        CreateAccountResponseModel createAccountResponseModel = UserSteps.createAccount();

        //        Deposit money
        UserDepositMoneyRequestModel userDepositMoneyRequestModel = UserDepositMoneyRequestModel.builder()
                .id(createAccountResponseModel.getId())
                .balance(depositBalance)
                .build();

        //        Check whether the balance is equal as should
        List<GetCustomerAccountsResponseModel> accounts = new CrudRequester(
                RequestSpecs.userSpec(
                        UserSteps.getCreateUserRequestModel().getUsername(),
                        UserSteps.getCreateUserRequestModel().getPassword()),
                ResponseSpecs.responseReturns200Spec(),
                Endpoint.CUSTOMER_ACCOUNTS)
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                });

        GetCustomerAccountsResponseModel account = accounts.stream()
                .filter(acc -> acc.getId().equals(createAccountResponseModel.getId()))
                .findFirst().get();

        softly.assertThat(account.getBalance()).isEqualTo(0.0F);
    }
}