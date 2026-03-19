package models;

import java.util.Objects;

public class Address {
    private final String name;
    private final String street;
    private final String cityStateZip;
    private final String country;
    private final String phoneNumber;

    public Address(String name, String street, String cityStateZip, String country, String phoneNumber) {
        this.name = name;
        this.street = street;
        this.cityStateZip = cityStateZip;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getCityStateZip() {
        return cityStateZip;
    }

    public String getCountry() {
        return country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return Objects.equals(name, address.name) &&
                Objects.equals(street, address.street) &&
                Objects.equals(cityStateZip, address.cityStateZip) &&
                Objects.equals(country, address.country) &&
                Objects.equals(phoneNumber, address.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, street, cityStateZip, country, phoneNumber);
    }

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", street='" + street + '\'' +
                ", cityStateZip='" + cityStateZip + '\'' +
                ", country='" + country + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
