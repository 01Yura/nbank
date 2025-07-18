package ui.middle.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    FAILD_TO_CREATE_USER("Failed to create user"),

    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),

    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),
    PLEASE_ENTER_A_VALID_NAME("❌ Please enter a valid name."),

    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited"),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),

    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred"),
    ERROR_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    ERROR_INSUFFICIENT_FUNDS("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    NO_USER_FOUND_WITH_THIS_ACCOUNT("❌ No user found with this account number.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
