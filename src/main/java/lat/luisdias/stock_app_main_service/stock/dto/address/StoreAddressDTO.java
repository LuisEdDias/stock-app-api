package lat.luisdias.stock_app_main_service.stock.dto.address;

public record StoreAddressDTO(
        String number,
        String street,
        String neighborhood,
        String city,
        String state,
        String zip,
        String complement
) {
}
