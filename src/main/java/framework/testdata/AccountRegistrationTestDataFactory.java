package framework.testdata;

import models.AccountRegistrationData;

public class AccountRegistrationTestDataFactory {

    public static AccountRegistrationData validRegistrationUserMale() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        builder.title("Mr");
        builder.birthDay("20");
        builder.birthMonth("February");
        builder.birthYear("1996");
        builder.specialOfferSignUp(true);
        builder.newsLetterSignUp(true);
        builder.firstName("John");
        builder.lastName("Doe");
        builder.company("OpenAI");
        builder.address1("123 Main St");
        builder.address2("Apt 4B");
        builder.country("United States");
        builder.state("California");
        builder.city("San Francisco");
        builder.zipCode("94105");
        builder.mobileNumber("+14155552671");
        return builder.build();
    }

    public static AccountRegistrationData minimalRegistrationUser() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        builder.specialOfferSignUp(true);
        builder.newsLetterSignUp(true);
        builder.firstName("Jane");
        builder.lastName("Smith");
        builder.address1("456 Elm St");
        builder.country("India");
        builder.state("Maharashtra");
        builder.city("Mumbai");
        builder.zipCode("400001");
        builder.mobileNumber("+14155559876");
        // Only mandatory fields provided
        return builder.build();
    }

    public static AccountRegistrationData validRegistrationUserFemale() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        builder.title("Mrs");
        builder.birthDay("03");
        builder.birthMonth("November");
        builder.birthYear("1988");
        builder.newsLetterSignUp(true);
        builder.specialOfferSignUp(false);
        builder.firstName("Beth");
        builder.lastName("Love-Hewitt");
        builder.company("JP Chase");
        builder.address1("123 Main St");
        builder.address2("Apt 4B");
        builder.country("United States");
        builder.state("California");
        builder.city("Sacramento");
        builder.zipCode("90221");
        builder.mobileNumber("+14155427269");
        return builder.build();
    }

}
