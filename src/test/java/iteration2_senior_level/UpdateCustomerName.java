package iteration2_senior_level;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import senior.models.CreateUserRequestModel;
import senior.models.GetCustomerProfileResponseModel;
import senior.models.UpdateCustomerNameRequestModel;
import senior.models.UpdateCustomerNameResponseModel;
import senior.requests.skeleton.requesters.CrudRequester;
import senior.requests.skeleton.requesters.Endpoint;
import senior.requests.skeleton.requesters.ValidatedCrudRequester;
import senior.requests.steps.AdminSteps;
import senior.specs.RequestSpecs;
import senior.specs.ResponseSpecs;

import java.util.stream.Stream;

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


    static Stream<Arguments> argsFor_userCanChangeTheirNameUsingValidName() {
        return Stream.of(
//                Positive: Authorized user can change their name using a valid value
                Arguments.of("New Name"),
                Arguments.of("NEW NAME"),
                Arguments.of("N N"),
                Arguments.of("n n"),
                Arguments.of("N name"),
                Arguments.of("New n")
        );
    }

    @MethodSource("argsFor_userCanChangeTheirNameUsingValidName")
    @ParameterizedTest
    void userCanChangeTheirNameUsingValidName(String username) {
//        Create a new user
        CreateUserRequestModel createUserRequestModel = AdminSteps.createUser();

//        Check that name firstUser is null
        GetCustomerProfileResponseModel getCustomerProfileResponseModel =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(createUserRequestModel.getUsername(),
                        createUserRequestModel.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        softly.assertThat(getCustomerProfileResponseModel.getName()).isNull();

//        Change customer name
        UpdateCustomerNameRequestModel updateCustomerNameRequestModel = UpdateCustomerNameRequestModel.builder()
                .name(username)
                .build();

        UpdateCustomerNameResponseModel updateCustomerNameResponseModel =
                new ValidatedCrudRequester<UpdateCustomerNameResponseModel>(RequestSpecs.userSpec(createUserRequestModel.getUsername(),
                        createUserRequestModel.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.UPDATE_CUSTOMER_PROFILE)
                        .put(updateCustomerNameRequestModel);

        softly.assertThat(updateCustomerNameRequestModel.getName()).isEqualTo(updateCustomerNameResponseModel.getCustomer().getName());
        softly.assertThat(updateCustomerNameResponseModel.getMessage()).isEqualTo("Profile updated successfully");
    }


    @ParameterizedTest
    @MethodSource("argsFor_userCannotChangeTheirNameUsingInvalidName")
    void userCannotChangeTheirNameUsingInvalidName(String newName, String errorValue) {
        //        Create a new user
        CreateUserRequestModel createUserRequestModel = AdminSteps.createUser();

        //        Check that name firstUser is null
        GetCustomerProfileResponseModel getCustomerProfileResponseModel =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(createUserRequestModel.getUsername(),
                        createUserRequestModel.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        softly.assertThat(getCustomerProfileResponseModel.getName()).isNull();

//        Change customer name
        UpdateCustomerNameRequestModel updateCustomerNameRequestModel = UpdateCustomerNameRequestModel.builder()
                .name(newName)
                .build();

        new CrudRequester(RequestSpecs.userSpec(createUserRequestModel.getUsername(),
                createUserRequestModel.getPassword()),
                ResponseSpecs.responseReturns400WithoutKeyValueSpec(errorValue),
                Endpoint.UPDATE_CUSTOMER_PROFILE)
                .put(updateCustomerNameRequestModel);

//       Check again that after changing the name, the name is still null
        GetCustomerProfileResponseModel getCustomerProfileResponseModel2 =
                new ValidatedCrudRequester<GetCustomerProfileResponseModel>(RequestSpecs.userSpec(createUserRequestModel.getUsername(),
                        createUserRequestModel.getPassword()),
                        ResponseSpecs.responseReturns200Spec(),
                        Endpoint.GET_CUSTOMER_PROFILE)
                        .get();

        softly.assertThat(getCustomerProfileResponseModel2.getName()).isNull();
    }
}
