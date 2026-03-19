package models;

public class UserIdentityData {

    private final String userName;
    private final String userEmail;
    private final String userPassword;

    public UserIdentityData(Builder builder) {
        this.userName = builder.userName;
        this.userEmail = builder.userEmail;
        this.userPassword = builder.userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userName;
        private String userEmail;
        private String userPassword;

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder userPassword(String userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        public UserIdentityData build() {
            if(userName==null || userName.isBlank()) {
                throw new IllegalStateException("User name is required");
            }
            if(userEmail==null || userEmail.isBlank()) {
                throw new IllegalStateException("User email is required");
            }
            if(userPassword==null || userPassword.isBlank()) {
                throw new IllegalStateException("User password is required");
            }
            return new UserIdentityData(this);
        }
    }
}
