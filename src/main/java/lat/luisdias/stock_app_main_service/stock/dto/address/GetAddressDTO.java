package lat.luisdias.stock_app_main_service.stock.dto.address;

import lat.luisdias.stock_app_main_service.stock.entities.vo.Address;

public record GetAddressDTO(
        String number,
        String street,
        String neighborhood,
        String city,
        String state,
        String zip,
        String complement
) {
    public GetAddressDTO (Address address) {
        this(
                address.getNumber(),
                address.getStreet(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZip(),
                address.getComplement()
        );
    }
}
