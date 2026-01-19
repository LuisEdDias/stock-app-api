package lat.luisdias.stock_app_main_service.security.authentication.log;

import jakarta.persistence.Embeddable;
import lat.luisdias.stock_app_main_service.security.authentication.log.GeoLocationService.GeoLocationDTO;

@Embeddable
public class GeoLocationVO {
    private Double lat;
    private Double lon;
    private String country;
    private String region;
    private String city;

    public GeoLocationVO() {}

    public GeoLocationVO(GeoLocationDTO geoLocation) {
        this.country = geoLocation.country();
        this.region = geoLocation.region();
        this.city = geoLocation.city();
        this.lon = geoLocation.lon();
        this.lat = geoLocation.lat();
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }
}
