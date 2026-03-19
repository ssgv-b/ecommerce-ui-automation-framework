package models;

import java.time.Year;

public class CreditCardDetailsData {

    private final String cardName;
    private final String cardNumber;
    private final int cvc;
    private final int expiryMonth;
    private final int expiryYear;

    public CreditCardDetailsData(Builder builder) {
        this.cardName = builder.cardName;
        this.cardNumber = builder.cardNumber;
        this.cvc = builder.cvc;
        this.expiryMonth = builder.expiryMonth;
        this.expiryYear = builder.expiryYear;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getCvc() {
        return cvc;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String cardName;
        private String cardNumber;
        private int cvc;
        private int expiryMonth;
        private int expiryYear;

        public Builder cardName(String cardName) {
            this.cardName = cardName;
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder cvc(int cvc) {
            this.cvc = parseCvc(cvc);
            return this;
        }

        public Builder expiryMonth(int expiryMonth) {
            this.expiryMonth = parseExpiryMonth(expiryMonth);
            return this;
        }

        public Builder expiryYear(int expiryYear) {
            this.expiryYear = parseExpiryYear(expiryYear);
            return this;
        }
        private int parseCvc(int cvc) {
            int min = 100;
            int max = 999;
            if(cvc >=min && cvc <=max) {
                return cvc;
            }
            throw new IllegalArgumentException("Invalid CVC code, must be a 3 digit number.");
        }

        private int parseExpiryMonth(int expiryMonth) {
            int min = 1;
            int max = 12;
            if (expiryMonth >=min && expiryMonth <=max) {
                return expiryMonth;
            }
            throw new IllegalArgumentException("Invalid expiry month - value must be between 1 and 12.");
        }

        private int parseExpiryYear(int expiryYear) {
            int min = Year.now().getValue();
            int max = Year.now().plusYears(15).getValue();
            if (expiryYear >=min && expiryYear <=max) {
                return expiryYear;
            }
            throw new IllegalArgumentException("Invalid expiry year");
        }

        public CreditCardDetailsData build() {
            if (cardName==null || cardName.isBlank()) {
                throw new IllegalStateException("Card name is required.");
            }
            if (cardNumber==null || cardNumber.isBlank()) {
                throw new IllegalStateException("Card number is required.");
            }
            return new CreditCardDetailsData(this);
        }

    }

}

