package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferMoneyRequestModel;

import static io.restassured.RestAssured.given;

public class TransferMoneyRequestSender extends BaseRequestSender<TransferMoneyRequestModel> {
    public TransferMoneyRequestSender(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse request(TransferMoneyRequestModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpecification);
    }
}