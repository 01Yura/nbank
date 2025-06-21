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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TransferMoneyTest extends BaseTest {

    static Stream<Arguments> argsFor_userCanTransferMoney() {
        return Stream.of(
                Arguments.of(10.0F, 50.0F, 50.0F, 10.0F),
                Arguments.of(10000.0F, 5000.0F, 15000.0F, 10000.0F),
                Arguments.of(9999.0F, 5000.0F, 10000.0F, 9999.0F)
        );
    }


    static Stream<Arguments> argsFor_userCannotTransferMoney() {
        return Stream.of(
//                Negative: Authorized user CANNOT transfer amount money from one account to another if this amount of money doesn't exist on their account
                Arguments.of(1000.0F, 100.0F, 200.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount of money from one account to another (-1)
                Arguments.of(-1.0F, 1.0F, 2.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount of money from one account to another (0)
                Arguments.of(0.0F, 1.0F, 2.0F, "Invalid transfer: insufficient funds or invalid accounts"),
//                Negative: Authorized user CANNOT transfer invalid amount of money from one account to another (10001)
                Arguments.of(10001.0F, 5000.0F, 11000.0F, "Transfer amount cannot exceed 10000")

        );
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCanTransferMoney")
    void userCanTransferMoney(Float transferAmount, Float depositPerCycle, Float depositThreshold, Float expectedAmount) {
        List<CreateUserRequestModel> users = new ArrayList<>();
        List<CreateAccountResponseModel> accounts = new ArrayList<>();
        List<UserDepositMoneyResponseModel> deposits = new ArrayList<>();

//        Create a first user + account + deposit money until the balance is enough for a successful transfer
        UserSteps.depositMoney(depositPerCycle, depositThreshold);

        users.add(UserSteps.getCreateUserRequestModel());
        accounts.add(UserSteps.getCreateAccountResponseModel());
        deposits.add(UserSteps.getUserDepositMoneyResponseModel());


//        Create a second user + account
        UserSteps.createAccount();

        users.add(UserSteps.getCreateUserRequestModel());
        accounts.add(UserSteps.getCreateAccountResponseModel());

//        Transfer money
        TransferMoneyRequestModel model = TransferMoneyRequestModel.builder()
                .senderAccountId(accounts.get(0).getId())
                .receiverAccountId(accounts.get(1).getId())
                .amount(transferAmount)
                .build();

        TransferMoneyResponseModel transferMoneyResponseModel =
                new ValidatedCrudRequester<TransferMoneyResponseModel>(RequestSpecs.userSpec(users.get(0).getUsername(),
                        users.get(0).getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.ACCOUNTS_TRANSFER)
                        .post(model);

        softly.assertThat(transferMoneyResponseModel.getSenderAccountId()).isEqualTo(accounts.get(0).getId());
        softly.assertThat(transferMoneyResponseModel.getReceiverAccountId()).isEqualTo(accounts.get(1).getId());
        softly.assertThat(transferMoneyResponseModel.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferMoneyResponseModel.getAmount()).isEqualTo(expectedAmount);

//        Take current info about the account of the first user (who is sent money)
        List<GetCustomerAccountsResponseModel> listFirstUserAccounts =
                new CrudRequester(RequestSpecs.userSpec(users.get(0).getUsername(), users.get(0).getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.CUSTOMER_ACCOUNTS)
                        .get()
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountSender = listFirstUserAccounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accounts.get(0).getAccountNumber()))
                .findFirst().get();

        //        Take current info about the account of the second user (who is taken money)
        List<GetCustomerAccountsResponseModel> listOfSecondUserAccounts =
                new CrudRequester(RequestSpecs.userSpec(users.get(1).getUsername(), users.get(1).getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.CUSTOMER_ACCOUNTS)
                        .get()
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountReceiver = listOfSecondUserAccounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accounts.get(1).getAccountNumber()))
                .findFirst().get();

//      Проверяем, что сумма правильно списалась, и затем то, что списалось, то и пришло на другой счет и
//      суммировалось правильно (округляя до сотых, так как это FLOAT, будь он не ладен)
        softly.assertThat(accountSender.getBalance()).isEqualTo(Math.round((deposits.get(0).getBalance() - transferAmount) * 100F) / 100F);
        softly.assertThat(accountReceiver.getBalance()).isEqualTo(Math.round((accounts.get(1).getBalance() + transferAmount) * 100F) / 100F);

//      Проверяем что у отправителя статус транзакции TRANSFER_OUT, а у получателя TRANSFER_IN
        softly.assertThat(accountSender.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(accounts.get(1).getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_OUT.toString());
        softly.assertThat(accountReceiver.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(accounts.get(0).getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_IN.toString());

//        Проверяем, что время транзакции совпадает у отправителя и получателя
        softly.assertThat(accountSender.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(accounts.get(1).getId())).findFirst().get().getTimestamp())
                .isEqualTo(accountReceiver.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(accounts.get(0).getId())).findFirst().get().getTimestamp());
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCannotTransferMoney")
    void userCannotTransferMoney(Float transferAmount, Float depositPerCycle, Float depositThreshold, String expectedError) {
        List<CreateUserRequestModel> users = new ArrayList<>();
        List<CreateAccountResponseModel> accounts = new ArrayList<>();
        List<UserDepositMoneyResponseModel> deposits = new ArrayList<>();

//        Create a first user + account + deposit money until the balance is enough for a successful transfer
        UserSteps.depositMoney(depositPerCycle, depositThreshold);
        users.add(UserSteps.getCreateUserRequestModel());
        accounts.add(UserSteps.getCreateAccountResponseModel());
        deposits.add(UserSteps.getUserDepositMoneyResponseModel());


//        Create a second user + account
        UserSteps.createAccount();
        users.add(UserSteps.getCreateUserRequestModel());
        accounts.add(UserSteps.getCreateAccountResponseModel());

//        Transfer money
        TransferMoneyRequestModel model = TransferMoneyRequestModel.builder()
                .senderAccountId(accounts.get(0).getId())
                .receiverAccountId(accounts.get(1).getId())
                .amount(transferAmount)
                .build();

        new CrudRequester(RequestSpecs.userSpec(users.get(0).getUsername(),
                users.get(0).getPassword()),
                ResponseSpecs.responseReturns400WithoutKeyValueSpec(expectedError),
                Endpoint.ACCOUNTS_TRANSFER)
                .post(model);


        //        Take current info about the account of the first user (who is sent money)
        List<GetCustomerAccountsResponseModel> listFirstUserAccounts =
                new CrudRequester(RequestSpecs.userSpec(users.get(0).getUsername(), users.get(0).getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.CUSTOMER_ACCOUNTS)
                        .get()
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountSender = listFirstUserAccounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accounts.get(0).getAccountNumber()))
                .findFirst().get();

        //        Take current info about the account of the second user (who is taken money)
        List<GetCustomerAccountsResponseModel> listOfSecondUserAccounts =
                new CrudRequester(RequestSpecs.userSpec(users.get(1).getUsername(), users.get(1).getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.CUSTOMER_ACCOUNTS)
                        .get()
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountReceiver = listOfSecondUserAccounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accounts.get(1).getAccountNumber()))
                .findFirst().get();

//        Check that the amount of money was not changed on both accounts
        softly.assertThat(accountSender.getBalance()).isEqualTo(deposits.get(0).getBalance());
        softly.assertThat(accountReceiver.getBalance()).isEqualTo(accounts.get(1).getBalance());
    }
}