package framework.testdata;

import models.UserIdentityData;

import java.util.UUID;

public class UserIdentityDataFactory {

    public static UserIdentityData newUniqueUser() {
        UserIdentityData.Builder builder = UserIdentityData.builder();
        String userSuffix = generateUniqueSuffix();
        builder.userName("newtestuser_" +userSuffix);
        builder.userEmail("newtestuser_" +userSuffix+ "@factory.com");
        builder.userPassword("SecurePass123$");
        return builder.build();
    }

    public static UserIdentityData existingSeededUser() {
        UserIdentityData.Builder builder = UserIdentityData.builder();
        builder.userName("existinguser");
        builder.userEmail("existing@user.com");
        builder.userPassword("ExistingUser123$");
        return builder.build();
    }

    private static String generateUniqueSuffix() {
       UUID uniquePart = UUID.randomUUID();
       return uniquePart.toString().replace("-","").substring(0,5);
    }

    public static UserIdentityData invalidUser() {
        UserIdentityData.Builder builder = UserIdentityData.builder();
        builder.userName("invalidUser");
        builder.userEmail("222");
        builder.userPassword("invalidPass123$");
        return builder.build();
    }
}
