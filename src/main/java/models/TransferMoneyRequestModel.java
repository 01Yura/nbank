package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyRequestModel extends  BaseModel{
    private Long senderAccountId;
    private Long receiverAccountId;
    private Float amount;
}


