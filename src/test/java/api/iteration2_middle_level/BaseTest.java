package api.iteration2_middle_level;

import api.middle.generators.RandomData;
import api.middle.models.CreateAccountResponseModel;
import api.middle.models.CreateUserRequestModel;
import api.middle.models.UserRole;
import api.middle.requests.CreateAccountRequestSender;
import api.middle.requests.CreateUserRequestSender;
import api.middle.specs.RequestSpecs;
import api.middle.specs.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    protected SoftAssertions softly;
    protected CreateUserRequestModel firstUser;
    protected CreateUserRequestModel secondUser;
    protected CreateAccountResponseModel firstUserAccount;
    protected CreateAccountResponseModel secondUserAccount;


    @BeforeEach
    public void initSoftAssertions() {
        this.softly = new SoftAssertions();
    }


    @AfterEach
    public void verifySoftAssertions() {
        this.softly.assertAll();
    }


    @BeforeEach
    public void create2Users() {
        firstUser = CreateUserRequestModel.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateUserRequestSender(RequestSpecs.adminSpec(), ResponseSpecs.responseReturns201Spec())
                .request(firstUser);

        secondUser = CreateUserRequestModel.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateUserRequestSender(RequestSpecs.adminSpec(), ResponseSpecs.responseReturns201Spec())
                .request(secondUser);
    }


    @BeforeEach
    public void createAccountForEachUser() {
        firstUserAccount =
                new CreateAccountRequestSender(RequestSpecs.userSpec(firstUser.getUsername(),
                        firstUser.getPassword()), ResponseSpecs.responseReturns201Spec())
                        .request(null)
                        .extract().as(CreateAccountResponseModel.class);

        secondUserAccount =
                new CreateAccountRequestSender(RequestSpecs.userSpec(secondUser.getUsername(),
                        secondUser.getPassword()), ResponseSpecs.responseReturns201Spec())
                        .request(null)
                        .extract().as(CreateAccountResponseModel.class);
    }
}
