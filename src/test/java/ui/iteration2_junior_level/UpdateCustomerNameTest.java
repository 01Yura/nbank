package ui.iteration2_junior_level;

import api.senior.models.CreateUserRequestModel;
import api.senior.models.GetCustomerProfileResponseModel;
import api.senior.models.LoginUserRequestModel;
import api.senior.requests.skeleton.requesters.CrudRequester;
import api.senior.requests.skeleton.requesters.Endpoint;
import api.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import api.senior.requests.steps.AdminSteps;
import api.senior.specs.RequestSpecs;
import api.senior.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCustomerNameTest extends SetupTest {
    @Test
    void userCanChangeTheirNameUsingValidName() throws InterruptedException {
        //        Setup env:
//        create user and login to dashboard
        CreateUserRequestModel user = AdminSteps.createUser();

        String userAuthToken = new CrudRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.responseReturns200Spec(),
                Endpoint.AUTH_LOGIN)
                .post(LoginUserRequestModel.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthToken);
        Selenide.open("/dashboard");

//        check that initial name is "noname" on UI
        String initialName = "noname";
        $(Selectors.byClassName("welcome-text")).shouldHave(Condition.text("Welcome, " + initialName));

//        check that the initial name is null on API
        GetCustomerProfileResponseModel initialCustomerProfile =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(user.getUsername(),
                        user.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        assertThat(initialCustomerProfile.getName()).isNull();

//        change the name
        $(Selectors.byClassName("user-info")).click();

        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        String newUserName = "New Name";
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible)
                .shouldBe(Condition.clickable).sendKeys(newUserName);
        Thread.sleep(1000);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldHave(Condition.attribute("value", newUserName));


        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();
        Alert alert = switchTo().alert();
        String actualAlertMessage = alert.getText();
        String expectedAlertMessage = "✅ Name updated successfully!";
        assertThat(actualAlertMessage).isEqualTo(expectedAlertMessage);
        alert.accept();

//        check that the name has been changed on UI
        refresh();
        $(Selectors.byClassName("user-name")).shouldBe(Condition.visible).shouldHave(Condition.partialText(newUserName));

//        check that the name has been changed on API
        GetCustomerProfileResponseModel finalCustomerProfile =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(user.getUsername(),
                        user.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        assertThat(finalCustomerProfile.getName()).isEqualTo(newUserName);
    }


    @Test
    void userCannotChangeTheirNameUsingInvalidName() throws InterruptedException {
        //        Setup env:
//        create user and login to dashboard
        CreateUserRequestModel user = AdminSteps.createUser();

        String userAuthToken = new CrudRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.responseReturns200Spec(),
                Endpoint.AUTH_LOGIN)
                .post(LoginUserRequestModel.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthToken);
        Selenide.open("/dashboard");

//        check that initial name is "noname" on UI
        String initialName = "noname";
        $(Selectors.byClassName("welcome-text")).shouldHave(Condition.text("Welcome, " + initialName));

//        check that the initial name is null on API
        GetCustomerProfileResponseModel initialCustomerProfile =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(user.getUsername(),
                        user.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        assertThat(initialCustomerProfile.getName()).isNull();

//        change the name
        $(Selectors.byClassName("user-info")).click();

        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        String newUserIncorrectName = "IncorrectName";
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible)
                .shouldBe(Condition.clickable)
                .sendKeys(newUserIncorrectName);

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String actualAlertMessage = alert.getText();
        String expectedAlertMessage1 = "Name must contain two words with letters only";
        String expectedAlertMessage2 = "❌ Please enter a valid name.";
        if (!(actualAlertMessage.equals(expectedAlertMessage1) || actualAlertMessage.equals(expectedAlertMessage2))) {
            throw new AssertionError(actualAlertMessage + " is not equal to expected messages");
        }
        alert.accept();

//        check that the name has not been changed on UI
        refresh();
        $(Selectors.byClassName("user-name")).shouldBe(Condition.visible).shouldHave(Condition.partialText(initialName));

//        check that the name has not been changed on API
        GetCustomerProfileResponseModel finalCustomerProfile =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(user.getUsername(),
                        user.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        assertThat(finalCustomerProfile.getName()).isNull();
    }
}
