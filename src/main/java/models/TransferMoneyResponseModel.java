package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyResponseModel extends BaseModel{
    private Long receiverAccountId;
    private Float amount;
    private String message;
    private Long senderAccountId;
}
