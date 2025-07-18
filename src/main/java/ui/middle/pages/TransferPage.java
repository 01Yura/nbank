package ui.middle.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage>{
    private SelenideElement chooseSenderAccountDropdownMenu = $(Selectors.byXpath("//option[text()='-- Choose an account --']/ancestor::select"));
    private SelenideElement recipientAccountNumberInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement enterAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement confirmTransferCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage sendTransfer(String senderAccountNumber, String recipientAccountNumber, Float amountMoneyToTransfer ){
        chooseSenderAccountDropdownMenu.selectOptionContainingText(senderAccountNumber);
        recipientAccountNumberInput.sendKeys(recipientAccountNumber);
        enterAmountInput.sendKeys(amountMoneyToTransfer.toString());
        confirmTransferCheckbox.click();
        sendTransferButton.click();
        return this;
    }
}
