package framework.testdata;

import java.util.UUID;
import models.UserIdentityData;

public class UserIdentityDataFactory {

    public static UserIdentityData newUniqueUser() {
        UserIdentityData.Builder builder = UserIdentityData.builder();
        String uniqueUserSuffix = generateUniqueSuffix();
        builder.userName("newtestuser_" + uniqueUserSuffix);
        builder.userEmail("newtestuser_" + uniqueUserSuffix + "@factory.com");
        builder.userPassword("SecurePass123$");
        return builder.build();
    }

    private static String generateUniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static UserIdentityData invalidUser() {
        UserIdentityData.Builder builder = UserIdentityData.builder();
        builder.userName("invalidUser");
        builder.userEmail("emailUser@ggg.com");
        builder.userPassword("invalidPass123$");
        return builder.build();
    }
}
