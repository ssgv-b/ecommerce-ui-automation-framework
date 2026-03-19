package models;

import java.util.Arrays;

public class AccountRegistrationData {
    private final String title;
    private final String birthDay;
    private final String birthMonth;
    private final String birthYear;
    private final boolean newsletterSignUp;
    private final boolean specialOfferSignUp;
    private final String firstName;
    private final String lastName;
    private final String company;
    private final String address1;
    private final String address2;
    private final String country;
    private final String state;
    private final String city;
    private final String zipCode;
    private final String mobileNumber;

    private AccountRegistrationData(Builder builder) {
        this.title = builder.title;
        this.birthDay = builder.birthDay;
        this.birthMonth = builder.birthMonth;
        this.birthYear = builder.birthYear;
        this.newsletterSignUp = builder.newsletterSignUp;
        this.specialOfferSignUp = builder.specialOfferSignUp;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.company = builder.company;
        this.address1 = builder.address1;
        this.address2 = builder.address2;
        this.country = builder.country;
        this.state = builder.state;
        this.city = builder.city;
        this.zipCode = builder.zipCode;
        this.mobileNumber = builder.mobileNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public boolean getNewsletterSignUp() { return newsletterSignUp; }

    public boolean getSpecialOfferSignUp() { return specialOfferSignUp; }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompany() { return company; }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String birthDay;
        private String birthMonth;
        private String birthYear;
        private boolean newsletterSignUp;
        private boolean specialOfferSignUp;
        private String firstName;
        private String lastName;
        private String company;
        private String address1;
        private String address2;
        private String country;
        private String state;
        private String city;
        private String zipCode;
        private String mobileNumber;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder birthDay(String birthDay) {
            this.birthDay = birthDay;
            return this;
        }

        public Builder birthMonth(String birthMonth) {
            this.birthMonth = birthMonth;
            return this;
        }

        public Builder birthYear(String birthYear) {
            this.birthYear = birthYear;
            return this;
        }

        public Builder newsLetterSignUp(boolean newsletterSignUp) {
            this.newsletterSignUp = newsletterSignUp;
            return this;
        }

        public Builder specialOfferSignUp(boolean specialOfferSignUp) {
            this.specialOfferSignUp = specialOfferSignUp;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder address1(String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder address2(String address2) {
            this.address2 = address2;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public AccountRegistrationData build() {
            if (firstName == null || firstName.isBlank()) {
                throw new IllegalStateException("First name is required.");
            }
            if (lastName == null || lastName.isBlank()) {
                throw new IllegalStateException("Last name is required.");
            }
            if (address1 == null || address1.isBlank()) {
                throw new IllegalStateException("Address1 is required.");
            }
            if (country == null || country.isBlank()) {
                throw new IllegalStateException("Country is required.");
            }
            if (state == null || state.isBlank()) {
                throw new IllegalStateException("State is required.");
            }
            if (city == null || city.isBlank()) {
                throw new IllegalStateException("City is required.");
            }
            if (zipCode == null || zipCode.isBlank()) {
                throw new IllegalStateException("Zip code is required.");
            }
            if (mobileNumber == null || mobileNumber.isBlank()) {
                throw new IllegalStateException("Mobile number is required.");
            }

            return new AccountRegistrationData(this);
        }

    }

    public Address getAddressRegistrationData() {
        String name = joinNonBlank(getFirstName(), getLastName());
        String street = joinNonBlank(getAddress1(), getAddress2());
        String cityStateZip = joinNonBlank(getCity(), getState(), getZipCode());
        String country = getCountry();
        String phone = getMobileNumber();
        return new Address(name, street, cityStateZip, country, phone);
    }

    private static String joinNonBlank(String... parts) {
        return Arrays.stream(parts)
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }
}
