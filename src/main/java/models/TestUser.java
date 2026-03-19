package models;

public class TestUser {
    private final AccountRegistrationData profile;
    private final UserIdentityData identity;

    public TestUser(AccountRegistrationData profile,
                    UserIdentityData identity) {
        this.profile = profile;
        this.identity = identity;
    }

    public UserIdentityData getIdentity() {
        return identity;
    }

    public AccountRegistrationData getProfile() {
        return profile;
    }
}
