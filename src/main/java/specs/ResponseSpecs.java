package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs() {
    }


    public static ResponseSpecification responseReturns200Spec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }


    private static ResponseSpecBuilder defaultResponseSpecBuilder() {
        return new ResponseSpecBuilder();
    }


    public static ResponseSpecification responseReturns201Spec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(201)
                .build();
    }


    public static ResponseSpecification responseReturns400Spec(String errorKey, String errorValue) {
        return defaultResponseSpecBuilder()
                .expectStatusCode(400)
                .expectBody(errorKey, Matchers.hasItem(errorValue))
                .build();
    }


    public static ResponseSpecification responseReturns400WithoutKeyValueSpec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }
}

