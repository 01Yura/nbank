package ui.middle.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
public class DepositPage extends BasePage<DepositPage>{
    private SelenideElement welcomeText = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement chooseAnAccountDropdown = $(Selectors.byXpath("//option[text()='-- Choose an account --']/ancestor::select"));
    private SelenideElement enterAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage depositMoney(String accountNumber, Float moneyAmount){
        chooseAnAccountDropdown.selectOptionContainingText(accountNumber);
        enterAmountInput.sendKeys(moneyAmount.toString());
        depositButton.click();
        return this;
    }
    public DepositPage checkAccountBalance(String accountNumber, Float balance){
        chooseAnAccountDropdown.click();
        String expectedText = String.format("%s (Balance: $%s0)", accountNumber,
                balance.toString());
        $$(Selectors.byXpath("//select/option")).findBy(Condition.text(expectedText)).shouldBe(Condition.visible);
        return this;
    }
}
