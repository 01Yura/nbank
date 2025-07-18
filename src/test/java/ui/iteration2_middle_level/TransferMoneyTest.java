package ui.iteration2_middle_level;

import api.senior.models.CreateAccountResponseModel;
import api.senior.models.CreateUserRequestModel;
import api.senior.models.GetCustomerAccountsResponseModel;
import api.senior.models.TransactionType;
import api.senior.requests.steps.AdminSteps;
import api.senior.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.middle.pages.BankAlert;
import ui.middle.pages.DepositPage;
import ui.middle.pages.TransferPage;
import ui.middle.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {

    @Test
    void userCanTransferMoney() {
//        STEPS TO SET UP ENVIRONMENT:
//        create userSender, login to dashboard, create an account, deposit money
        CreateUserRequestModel userSender = AdminSteps.createUser();
        authAsUser(userSender);

        CreateAccountResponseModel senderAccount =
                new UserSteps(userSender.getUsername(), userSender.getPassword()).createNewAccount();

        Float deposit = 100.0F;
        new UserSteps(userSender.getUsername(), userSender.getPassword()).depositMoney(senderAccount, 100.0F);

//        create userRecipient and create a recipientAccount
        CreateUserRequestModel userRecipient = AdminSteps.createUser();

        CreateAccountResponseModel recipientAccount =
                new UserSteps(userRecipient.getUsername(), userRecipient.getPassword()).createNewAccount();

//        Start UI test
        Float amountToTransfer = 10.0F;
        new UserDashboard().open().makeTransfer()
                .getPage(TransferPage.class)
                .sendTransfer(senderAccount.getAccountNumber(), recipientAccount.getAccountNumber(), amountToTransfer)
                .checkAlertMessageAndAccept(
                        BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(),
                        amountToTransfer,
                        recipientAccount.getAccountNumber())
                        .getLogoutButton().click();

//        check balance userRecipient on UI
        authAsUser(userRecipient);
        new DepositPage().open().checkAccountBalance(recipientAccount.getAccountNumber(), amountToTransfer);

//        Check info about both accounts on API level:

//        take current info about the senderAccount after transfer
        GetCustomerAccountsResponseModel accountSenderAfterTransfer =
                new UserSteps(userSender.getUsername(), userSender.getPassword()).getAccountWIthSpecificNumber(senderAccount.getAccountNumber());

//        take current info about the recipientAccount after transfer
        GetCustomerAccountsResponseModel accountRecipientAfterTransfer =
                new UserSteps(userRecipient.getUsername(), userRecipient.getPassword()).getAccountWIthSpecificNumber(recipientAccount.getAccountNumber());

//      Проверяем, что сумма правильно списалась, и затем то, что списалось, то и пришло на другой счет и
//      суммировалось правильно (округляя до сотых, так как это FLOAT, будь он не ладен)
        assertThat(accountSenderAfterTransfer.getBalance()).isEqualTo(Math.round((deposit - amountToTransfer) * 100F) / 100F);
        assertThat(accountRecipientAfterTransfer.getBalance()).isEqualTo(Math.round((recipientAccount.getBalance() + amountToTransfer) * 100F) / 100F);

//      Проверяем что у отправителя статус транзакции TRANSFER_OUT, а у получателя TRANSFER_IN
        assertThat(accountSenderAfterTransfer.getTransactions().stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(recipientAccount.getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_OUT.toString());
        assertThat(accountRecipientAfterTransfer.getTransactions().stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId())).findFirst().get().getType())
                .isEqualTo(TransactionType.TRANSFER_IN.toString());

//        Проверяем, что время транзакции совпадает у отправителя и получателя
        assertThat(accountSenderAfterTransfer.getTransactions().stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(accountRecipientAfterTransfer.getId())).findFirst().get().getTimestamp())
                .isEqualTo(accountRecipientAfterTransfer.getTransactions().stream()
                        .filter(transaction -> transaction.getRelatedAccountId().equals(accountSenderAfterTransfer.getId())).findFirst().get().getTimestamp());
    }

    @Test
    void userCannotTransferMoney() {
//        STEPS TO SET UP ENVIRONMENT:
//        create userSender, login to dashboard, create an account, deposit money
        CreateUserRequestModel userSender = AdminSteps.createUser();
        authAsUser(userSender);

        CreateAccountResponseModel senderAccount =
                new UserSteps(userSender.getUsername(), userSender.getPassword()).createNewAccount();

        Float deposit = 100.0F;
        new UserSteps(userSender.getUsername(), userSender.getPassword()).depositMoney(senderAccount, 100.0F);

//        create userRecipient and create a recipientAccount
        CreateUserRequestModel userRecipient = AdminSteps.createUser();

        CreateAccountResponseModel recipientAccount =
                new UserSteps(userRecipient.getUsername(), userRecipient.getPassword()).createNewAccount();

//        Start UI test
        Float amountToTransfer = 99999.0F;
        new UserDashboard().open().makeTransfer()
                .getPage(TransferPage.class)
                .sendTransfer(senderAccount.getAccountNumber(), recipientAccount.getAccountNumber(), amountToTransfer)
                .checkAlertMessageAndAccept(
                        BankAlert.ERROR_EXCEED_10000.getMessage(),
                        BankAlert.ERROR_INSUFFICIENT_FUNDS.getMessage(),
                        BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT.getMessage())
                .getLogoutButton().click();

//        check balance userRecipient on UI
        authAsUser(userRecipient);
        new DepositPage().open().checkAccountBalance(recipientAccount.getAccountNumber(), recipientAccount.getBalance());

//        Check both accounts on API level:

//        take current info about the senderAccount after transfer
        GetCustomerAccountsResponseModel accountSenderAfterTransfer =
                new UserSteps(userSender.getUsername(), userSender.getPassword()).getAccountWIthSpecificNumber(senderAccount.getAccountNumber());

//        take current info about the recipientAccount after transfer
        GetCustomerAccountsResponseModel accountRecipientAfterTransfer =
                new UserSteps(userRecipient.getUsername(), userRecipient.getPassword()).getAccountWIthSpecificNumber(recipientAccount.getAccountNumber());


//        Check on API level that balance stayed the same on both accounts
        assertThat(deposit).isEqualTo(accountSenderAfterTransfer.getBalance());
        assertThat(recipientAccount.getBalance()).isEqualTo(accountRecipientAfterTransfer.getBalance());
    }
}
