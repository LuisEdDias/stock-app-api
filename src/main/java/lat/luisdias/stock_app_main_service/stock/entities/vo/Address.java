package lat.luisdias.stock_app_main_service.stock.entities.vo;

import lat.luisdias.stock_app_main_service.stock.dto.address.StoreAddressDTO;

import java.util.Objects;

public class Address {
    private String number;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zip;
    private String complement;

    public Address(){}

    public Address(StoreAddressDTO addressDTO) {
        this.number = addressDTO.number();
        this.street = addressDTO.street();
        this.neighborhood = addressDTO.neighborhood();
        this.city = addressDTO.city();
        this.state = addressDTO.state();
        this.zip = addressDTO.zip();
        this.complement = addressDTO.complement();
    }

    public String getNumber() {
        return number;
    }

    public String getStreet() {
        return street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getComplement() {
        return complement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(number, address.number) && Objects.equals(street, address.street) && Objects.equals(neighborhood, address.neighborhood) && Objects.equals(city, address.city) && Objects.equals(state, address.state) && Objects.equals(zip, address.zip) && Objects.equals(complement, address.complement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, street, neighborhood, city, state, zip, complement);
    }
}
