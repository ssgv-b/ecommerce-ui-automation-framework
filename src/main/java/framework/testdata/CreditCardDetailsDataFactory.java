package framework.testdata;

import models.CreditCardDetailsData;

public class CreditCardDetailsDataFactory {

    public static CreditCardDetailsData validCreditCardDetails() {
        CreditCardDetailsData.Builder builder = CreditCardDetailsData.builder();
        builder.cardName("Valid Customer");
        builder.cardNumber("4523453186599032");
        builder.cvc(542);
        builder.expiryMonth(10);
        builder.expiryYear(2029);
        return builder.build();
    }

    public static CreditCardDetailsData additionalValidCreditCardDetails() {
        CreditCardDetailsData.Builder builder = CreditCardDetailsData.builder();
        builder.cardName("Another Customer");
        builder.cardNumber("6678540967880285");
        builder.cvc(396);
        builder.expiryMonth(8);
        builder.expiryYear(2028);
        return builder.build();
    }

}
