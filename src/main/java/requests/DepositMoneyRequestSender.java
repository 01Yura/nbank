package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UserDepositMoneyRequestModel;

import static io.restassured.RestAssured.given;

public class DepositMoneyRequestSender extends BaseRequestSender<UserDepositMoneyRequestModel> {
    public DepositMoneyRequestSender(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse request(UserDepositMoneyRequestModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .spec(responseSpecification);
    }
}
