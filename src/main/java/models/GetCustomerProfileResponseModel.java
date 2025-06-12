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
public class GetCustomerProfileResponseModel extends BaseModel {
    private Long id;
    private String username;
    private String password;
    private Object name;
    private String role;
    private List<CreateAccountResponseModel> accounts;
}
