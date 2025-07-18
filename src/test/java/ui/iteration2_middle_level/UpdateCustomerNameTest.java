package ui.iteration2_middle_level;

import api.senior.models.CreateUserRequestModel;
import api.senior.models.GetCustomerProfileResponseModel;
import api.senior.requests.steps.AdminSteps;
import api.senior.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import ui.middle.pages.BankAlert;
import ui.middle.pages.EditProfilePage;
import ui.middle.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCustomerNameTest extends BaseUiTest {
    @Test
    void userCanChangeTheirNameUsingValidName() throws InterruptedException {
//        STEPS TO SET UP ENVIRONMENT:
//        1. Admin login + create user + User login
        CreateUserRequestModel user = AdminSteps.createUser();
        authAsUser(user);


//        STEPS OF TEST:
//        check that the initial name is "noname" on UI
        String initialName = "noname";
        new UserDashboard().open().getUserNameInfoIcon().shouldHave(Condition.text(initialName));

//        check that the initial name is null on API
        GetCustomerProfileResponseModel initialCustomerProfile = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();
        assertThat(initialCustomerProfile.getName()).isNull();

//        change name
        String newUserName = "New Name";
        new EditProfilePage().open().changeName(newUserName).checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

//        check that the name has been changed on UI
        new EditProfilePage().open().getUserNameInfoIcon().shouldHave(Condition.text(newUserName));

//        check that the name has been changed on API
        GetCustomerProfileResponseModel finalCustomerProfile = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();
        assertThat(finalCustomerProfile.getName()).isEqualTo(newUserName);
    }


    @Test
    void userCannotChangeTheirNameUsingInvalidName() throws InterruptedException {
//        STEPS TO SET UP ENVIRONMENT:
//        1. Admin login + create user + User login
        CreateUserRequestModel user = AdminSteps.createUser();
        authAsUser(user);


//        STEPS OF TEST:
//        check that the initial name is "noname" on UI
        String initialName = "noname";
        new UserDashboard().open().getUserNameInfoIcon().shouldHave(Condition.text(initialName));

//        check that the initial name is null on API
        GetCustomerProfileResponseModel initialCustomerProfile = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();
        assertThat(initialCustomerProfile.getName()).isNull();

//        change the name
        String newUserIncorrectName = "IncorrectName";
        new EditProfilePage().open().changeName(newUserIncorrectName).checkAlertMessageAndAccept(
                BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage(),
                BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());

//        check that the name has not been changed on UI
        new EditProfilePage().open().getUserNameInfoIcon().shouldHave(Condition.text(initialName));

//        check that the name has not been changed on API
        GetCustomerProfileResponseModel finalCustomerProfile = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();
        assertThat(finalCustomerProfile.getName()).isNull();
    }
}
