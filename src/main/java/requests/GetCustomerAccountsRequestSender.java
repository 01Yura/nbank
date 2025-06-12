package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class GetCustomerAccountsRequestSender extends BaseRequestSender{
    public GetCustomerAccountsRequestSender(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse request(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .spec(responseSpecification);
    }
}
