package ui.middle.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccountButton = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement makeTransferButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount(){
        createNewAccountButton.click();
        return this;
    }

    public UserDashboard makeTransfer(){
        makeTransferButton.click();
        return this;
    }

    public UserDashboard depositMoney(){
        depositMoneyButton.click();
        return this;
    }
}
