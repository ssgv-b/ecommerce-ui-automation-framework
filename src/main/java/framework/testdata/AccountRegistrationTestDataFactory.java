package framework.testdata;

import constants.Country;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import models.AccountRegistrationData;
import net.datafaker.Faker;

public class AccountRegistrationTestDataFactory {

    private static final Faker FAKER = new Faker(Locale.US);

    public static AccountRegistrationData validRegistrationUserMale() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        LocalDate birthDate = FAKER.timeAndDate().birthday(18, 80);
        return builder.title("Mr")
                .birthDay(String.valueOf(birthDate.getDayOfMonth()))
                .birthMonth(birthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .birthYear(String.valueOf(birthDate.getYear()))
                .specialOfferSignUp(true)
                .newsLetterSignUp(true)
                .firstName(FAKER.name().maleFirstName())
                .lastName(FAKER.name().lastName())
                .company(FAKER.company().name())
                .address1(FAKER.address().streetAddress())
                .address2(FAKER.address().secondaryAddress())
                .country(FAKER.options().option(Country.class).getCountryName())
                .state(FAKER.address().state())
                .city(FAKER.address().cityName())
                .zipCode(FAKER.address().zipCode())
                .mobileNumber(FAKER.phoneNumber().phoneNumber())
                .build();
    }

    public static AccountRegistrationData minimalRegistrationUser() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        return builder.specialOfferSignUp(true)
                .newsLetterSignUp(true)
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .address1(FAKER.address().streetAddress())
                .country(FAKER.options().option(Country.class).getCountryName())
                .state(FAKER.address().state())
                .city(FAKER.address().cityName())
                .zipCode(FAKER.address().zipCode())
                .mobileNumber(FAKER.phoneNumber().phoneNumberInternational())
                .build();
    }

    public static AccountRegistrationData validRegistrationUserFemale() {
        AccountRegistrationData.Builder builder = AccountRegistrationData.builder();
        LocalDate birthDate = FAKER.timeAndDate().birthday(18, 80);
        return builder.title("Mrs")
                .birthDay(String.valueOf(birthDate.getDayOfMonth()))
                .birthMonth(birthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .birthYear(String.valueOf(birthDate.getYear()))
                .newsLetterSignUp(true)
                .specialOfferSignUp(false)
                .firstName(FAKER.name().femaleFirstName())
                .lastName(FAKER.name().lastName())
                .company(FAKER.company().name())
                .address1(FAKER.address().streetAddress())
                .address2(FAKER.address().secondaryAddress())
                .country(FAKER.options().option(Country.class).getCountryName())
                .state(FAKER.address().state())
                .city(FAKER.address().cityName())
                .zipCode(FAKER.address().zipCode())
                .mobileNumber(FAKER.phoneNumber().phoneNumber())
                .build();
    }
}
