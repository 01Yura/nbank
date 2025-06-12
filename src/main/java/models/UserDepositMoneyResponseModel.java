package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDepositMoneyResponseModel extends BaseModel {
    private Long id;
    private String accountNumber;
    private Float balance;
    private List<Transaction> transactions;
}
