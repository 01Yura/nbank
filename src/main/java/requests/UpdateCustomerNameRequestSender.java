package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UpdateCustomerNameRequestModel;

import static io.restassured.RestAssured.given;

public class UpdateCustomerNameRequestSender extends BaseRequestSender<UpdateCustomerNameRequestModel> {
    public UpdateCustomerNameRequestSender(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse request(UpdateCustomerNameRequestModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .when()
                .put("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
