package iteration2_middle_level;

import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.DepositMoneyRequestSender;
import requests.GetCustomerAccountsRequestSender;
import requests.TransferMoneyRequestSender;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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

        //        Deposit money until the balance is enough for a successful transfer
        Float currentBalance = firstUserAccount.getBalance();

        UserDepositMoneyRequestModel userDepositMoneyRequestModel = UserDepositMoneyRequestModel.builder()
                .id(firstUserAccount.getId())
                .balance(depositPerCycle)
                .build();

        while (currentBalance < depositThreshold) {
            UserDepositMoneyResponseModel userDepositMoneyResponseModel = new DepositMoneyRequestSender(
                    RequestSpecs.userSpec(
                            firstUser.getUsername(),
                            firstUser.getPassword()),
                    ResponseSpecs.responseReturns200Spec())
                    .request(userDepositMoneyRequestModel)
                    .extract().as(UserDepositMoneyResponseModel.class);

            currentBalance = userDepositMoneyResponseModel.getBalance();
        }

//        Transfer money
        TransferMoneyRequestModel model = TransferMoneyRequestModel.builder()
                .senderAccountId(firstUserAccount.getId())
                .receiverAccountId(secondUserAccount.getId())
                .amount(transferAmount)
                .build();

        TransferMoneyResponseModel responseBody =
                new TransferMoneyRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()), ResponseSpecs.responseReturns200Spec())
                        .request(model)
                        .extract().as(TransferMoneyResponseModel.class);

        softly.assertThat(responseBody.getSenderAccountId()).isEqualTo(firstUserAccount.getId());
        softly.assertThat(responseBody.getReceiverAccountId()).isEqualTo(secondUserAccount.getId());
        softly.assertThat(responseBody.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(responseBody.getAmount()).isEqualTo(expectedAmount);

//        Take current info about the account of the first user (who is sent money)
        List<GetCustomerAccountsResponseModel> accounts1 =
                new GetCustomerAccountsRequestSender(RequestSpecs.userSpec(firstUser.getUsername(), firstUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountSender = accounts1.stream()
                .filter(acc -> acc.getAccountNumber().equals(firstUserAccount.getAccountNumber()))
                .findFirst().get();

        //        Take current info about the account of the second user (who is taken money)
        List<GetCustomerAccountsResponseModel> accounts2 =
                new GetCustomerAccountsRequestSender(RequestSpecs.userSpec(secondUser.getUsername(), secondUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountReceiver = accounts2.stream()
                .filter(acc -> acc.getAccountNumber().equals(secondUserAccount.getAccountNumber()))
                .findFirst().get();

//      Проверяем, что сумма правильно списалась, и затем то, что списалось, то и пришло на другой счет и
//      суммировалось правильно (округляя до сотых, так как это FLOAT, будь он не ладен)
        softly.assertThat(accountSender.getBalance()).isEqualTo(Math.round((currentBalance - transferAmount) * 100F) / 100F);
        softly.assertThat(accountReceiver.getBalance()).isEqualTo(Math.round((secondUserAccount.getBalance() + transferAmount) * 100F) / 100F);

//      Проверяем что у отправителя статус транзакции TRANSFER_OUT, а у получателя TRANSFER_IN
        softly.assertThat(accountSender.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(secondUserAccount.getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_OUT.toString());
        softly.assertThat(accountReceiver.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(firstUserAccount.getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_IN.toString());

//        Проверяем, что время транзакции совпадает у отправителя и получателя
        softly.assertThat(accountSender.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(secondUserAccount.getId())).findFirst().get().getTimestamp())
                .isEqualTo(accountReceiver.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(firstUserAccount.getId())).findFirst().get().getTimestamp());
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCannotTransferMoney")
    void userCannotTransferMoney(Float transferAmount, Float depositPerCycle, Float depositThreshold, String expectedError) {
        //        Deposit money until the balance is enough for a test transfer
        Float currentBalance = firstUserAccount.getBalance();

        UserDepositMoneyRequestModel userDepositMoneyRequestModel = UserDepositMoneyRequestModel.builder()
                .id(firstUserAccount.getId())
                .balance(depositPerCycle)
                .build();

        while (currentBalance < depositThreshold) {
            UserDepositMoneyResponseModel userDepositMoneyResponseModel = new DepositMoneyRequestSender(
                    RequestSpecs.userSpec(
                            firstUser.getUsername(),
                            firstUser.getPassword()),
                    ResponseSpecs.responseReturns200Spec())
                    .request(userDepositMoneyRequestModel)
                    .extract().as(UserDepositMoneyResponseModel.class);

            currentBalance = userDepositMoneyResponseModel.getBalance();
        }

//        Transfer money
        TransferMoneyRequestModel model = TransferMoneyRequestModel.builder()
                .senderAccountId(firstUserAccount.getId())
                .receiverAccountId(secondUserAccount.getId())
                .amount(transferAmount)
                .build();

        String actualError =
                new TransferMoneyRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()), ResponseSpecs.responseReturns400WithoutKeyValueSpec())
                        .request(model)
                        .extract().asString();

        softly.assertThat(actualError).isEqualTo(expectedError);

        //        Take current info about the account of the first user (who is sent money)
        List<GetCustomerAccountsResponseModel> accounts1 =
                new GetCustomerAccountsRequestSender(RequestSpecs.userSpec(firstUser.getUsername(), firstUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountSender = accounts1.stream()
                .filter(acc -> acc.getAccountNumber().equals(firstUserAccount.getAccountNumber()))
                .findFirst().get();

        //        Take current info about the account of the second user (who is taken money)
        List<GetCustomerAccountsResponseModel> accounts =
                new GetCustomerAccountsRequestSender(RequestSpecs.userSpec(secondUser.getUsername(), secondUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(new TypeRef<List<GetCustomerAccountsResponseModel>>() {
                        });

        GetCustomerAccountsResponseModel accountReceiver = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(secondUserAccount.getAccountNumber()))
                .findFirst().get();

//        Check that the amount of money was not changed on both accounts
        softly.assertThat(accountSender.getBalance()).isEqualTo(currentBalance);
        softly.assertThat(accountReceiver.getBalance()).isEqualTo(secondUserAccount.getBalance());

    }
}