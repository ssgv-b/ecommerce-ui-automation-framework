package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
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

    private static final String skipSignal = "none";
    private final By titleMrRadioBtn = By.id("id_gender1");
    private final By titleMrsRadioBtn = By.id("id_gender2");
    private final By passwordInput = By.id("password");
    private final By daysDropdown = By.id("days");
    private final By monthsDropdown = By.id("months");
    private final By yearsDropdown = By.id("years");
    private final By newsLetterCheckbox = By.xpath("//input[@id='newsletter']");
    private final By specialOffersCheckbox = By.xpath("//input[@id='optin']");
    private final By firstNameInput = By.id("first_name");
    private final By lastNameInput = By.id("last_name");
    private final By companyInput = By.id("company");
    private final By address1Input = By.id("address1");
    private final By address2Input = By.id("address2");
    private final By countryDropdown = By.id("country");
    private final By stateInput = By.id("state");
    private final By cityInput = By.id("city");
    private final By zipCodeInput = By.id("zipcode");
    private final By mobileNumberInput = By.id("mobile_number");
    private final By createAccountBtn = By.xpath("//button[@data-qa='create-account']");

    private AccountCreatedPage submitRegistration() {
        click(createAccountBtn);
        return new AccountCreatedPage(driverContext);
    }

    public AccountCreatedPage registerAccount(AccountRegistrationData data) {
        fillAccountRegistrationData(data);
        return submitRegistration();
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
        String company = normalizer.safeTrim(data.getCompany());
        String address2 = normalizer.safeTrim(data.getAddress2());
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
        String day = normalizer.safeTrim(data.getBirthDay());
        String month = normalizer.safeTrim(data.getBirthMonth());
        String year = normalizer.safeTrim(data.getBirthYear());

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
        if (title.equalsIgnoreCase(skipSignal)) {
            return;
        }
        switch (title) {
            case "mr" -> click(titleMrRadioBtn);
            case "mrs" -> click(titleMrsRadioBtn);
        }

    }

    private String titleResolver(AccountRegistrationData data) {
        String title = normalizer.normalizeText(data.getTitle());
        if (title.isBlank()) {
            return skipSignal;
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
