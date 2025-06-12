package iteration2_middle_level;

import models.GetCustomerProfileResponseModel;
import models.UpdateCustomerNameRequestModel;
import models.UpdateCustomerNameResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.GetCustomerProfileRequestSender;
import requests.UpdateCustomerNameRequestSender;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

public class UpdateCustomerName extends BaseTest {

    static Stream<Arguments> argsFor_userCannotChangeTheirNameUsingInvalidName() {
        return Stream.of(
//                Negative: Authorized user CANNOT change their name using invalid value (one word)
                Arguments.of("Newname", "Name must contain two words with letters only"),
//                Negative: Authorized user CANNOT change their name using invalid value (+ one digit)
                Arguments.of("New name1", "Name must contain two words with letters only"),
//                Negative: Authorized user CANNOT change their name using invalid value (+ one special symbol)
                Arguments.of("New name!", "Name must contain two words with letters only"),
//                Negative: Authorized user CANNOT change their name using invalid value (empty string)
                Arguments.of("", "Name must contain two words with letters only")
        );
    }


    @Test
    void userCanChangeTheirNameUsingValidName() {
//        Check that after @BeforeEach user creation their name is null
        GetCustomerProfileResponseModel responseModel =
                new GetCustomerProfileRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(GetCustomerProfileResponseModel.class);

        softly.assertThat(responseModel.getName()).isNull();

//        Change customer name
        UpdateCustomerNameRequestModel requestModel = UpdateCustomerNameRequestModel.builder()
                .name("New name")
                .build();

        UpdateCustomerNameResponseModel updateCustomerNameResponseModel =
                new UpdateCustomerNameRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()), ResponseSpecs.responseReturns200Spec())
                        .request(requestModel)
                        .extract().as(UpdateCustomerNameResponseModel.class);

        assertThat(requestModel.getName()).isEqualTo(updateCustomerNameResponseModel.getCustomer().getName());
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCannotChangeTheirNameUsingInvalidName")
    void userCannotChangeTheirNameUsingInvalidName(String newName, String errorValue) {
        //        Check that after @BeforeEach user creation their name is null
        GetCustomerProfileResponseModel responseModel =
                new GetCustomerProfileRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(GetCustomerProfileResponseModel.class);

        softly.assertThat(responseModel.getName()).isNull();

//        Change customer name
        UpdateCustomerNameRequestModel requestModel = UpdateCustomerNameRequestModel.builder()
                .name(newName)
                .build();


                new UpdateCustomerNameRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()), ResponseSpecs.responseReturns400WithoutKeyValueSpec())
                        .request(requestModel)
                        .assertThat()
                        .body(equalTo(errorValue));

//       Check again that after changing the name, the name is still null
        GetCustomerProfileResponseModel responseModel2 =
                new GetCustomerProfileRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()),
                        ResponseSpecs.responseReturns200Spec())
                        .request(null)
                        .extract().as(GetCustomerProfileResponseModel.class);

        softly.assertThat(responseModel2.getName()).isNull();
    }
}
