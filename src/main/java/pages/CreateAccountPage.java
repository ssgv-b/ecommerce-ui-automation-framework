package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import framework.utils.TextNormalizer;
import models.AccountRegistrationData;
import models.UserIdentityData;
import org.openqa.selenium.By;

public class CreateAccountPage extends BasePage {
    private final UserIdentityData userData;
    public CreateAccountPage(DriverContext driverContext, UserIdentityData userData) {
        super(driverContext);
        By createAccountPageSignal = By.xpath("//form[@action='/signup']");
        waitForVisibleElement(createAccountPageSignal);
        this.userData = userData;
    }

    private static final String titleNotProvided = "null";
    private static final By titleMrRadioBtn = By.id("id_gender1");
    private static final By titleMrsRadioBtn = By.id("id_gender2");
    private static final By passwordInput = By.id("password");
    private static final By daysDropdown = By.id("days");
    private static final By monthsDropdown = By.id("months");
    private static final By yearsDropdown = By.id("years");
    private static final By newsLetterCheckbox = By.xpath("//input[@id='newsletter']");
    private static final By specialOffersCheckbox = By.xpath("//input[@id='optin']");
    private static final By firstNameInput = By.id("first_name");
    private static final By lastNameInput = By.id("last_name");
    private static final By companyInput = By.id("company");
    private static final By address1Input = By.id("address1");
    private static final By address2Input = By.id("address2");
    private static final By countryDropdown = By.id("country");
    private static final By stateInput = By.id("state");
    private static final By cityInput = By.id("city");
    private static final By zipCodeInput = By.id("zipcode");
    private static final By mobileNumberInput = By.id("mobile_number");
    private static final By createAccountBtn = By.xpath("//button[@data-qa='create-account']");

    public AccountCreatedPage registerAccount(AccountRegistrationData data) {
        fillAccountRegistrationData(data);
        click(createAccountBtn);
        return new AccountCreatedPage(driverContext);
    }

    private void setIdentity(AccountRegistrationData data) {
        // DOB is optional
        setTitle(data);
        enterText(passwordInput, this.userData.getUserPassword());
        setDateOfBirth(data);
        enterText(firstNameInput, data.getFirstName());
        enterText(lastNameInput, data.getLastName());
    }

    private void setPreferences(AccountRegistrationData data) {
        // Independent flags; both may be selected
        if (data.getSpecialOfferSignUp()) {
            click(specialOffersCheckbox);
        }
        if (data.getNewsletterSignUp()) {
            click(newsLetterCheckbox);
        }
    }

    private void setAddress(AccountRegistrationData data) {
        //Optional fields
        String company = TextNormalizer.safeTrim(data.getCompany());
        String address2 = TextNormalizer.safeTrim(data.getAddress2());
        if (!company.isBlank()) {
            enterText(companyInput, company);
        }
        enterText(address1Input, data.getAddress1());
        if (!address2.isBlank()) {
            enterText(address2Input, address2);
        }
        selectByVisibleText(countryDropdown, data.getCountry());
        enterText(stateInput, data.getState());
        enterText(cityInput, data.getCity());
        enterText(zipCodeInput, data.getZipCode());
        enterText(mobileNumberInput, data.getMobileNumber());
    }

    private void fillAccountRegistrationData(AccountRegistrationData data) {
        setIdentity(data);
        setPreferences(data);
        setAddress(data);
    }


    private void setDateOfBirth(AccountRegistrationData data) {
        String day = TextNormalizer.safeTrim(data.getBirthDay());
        String month = TextNormalizer.safeTrim(data.getBirthMonth());
        String year = TextNormalizer.safeTrim(data.getBirthYear());

        if (day.isBlank() && month.isBlank() && year.isBlank()) {
            return;
        }
        if (day.isBlank() || month.isBlank() || year.isBlank()) {
            throw new RuntimeException("Date of birth must be fully provided or fully omitted");
        }
        selectDateOfBirth(day, month, year);
    }

    private void setTitle(AccountRegistrationData data) {
        String title = titleResolver(data);
        if (title.equalsIgnoreCase(titleNotProvided)) {
            return;
        }
        switch (title) {
            case "mr" -> click(titleMrRadioBtn);
            case "mrs" -> click(titleMrsRadioBtn);
        }

    }

    private String titleResolver(AccountRegistrationData data) {
        String title = TextNormalizer.normalizeText(data.getTitle());
        if (title.isBlank()) {
            return titleNotProvided;
        }
        switch (title) {
            case "mr", "mrs" -> {
                return title;
            }
            default -> throw new RuntimeException(
                    "Unsupported title for registration. Allowed values are Mr/Mrs."
            );
        }
    }

    private void selectDateOfBirth(String day, String month, String year) {
        selectByVisibleText(daysDropdown, day);
        selectByVisibleText(monthsDropdown, month);
        selectByVisibleText(yearsDropdown, year);
    }
}
