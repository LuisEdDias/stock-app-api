package lat.luisdias.stock_app_main_service.security.authentication.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class GeoLocationService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "http://ip-api.com/json/";
    private final Logger logger = LoggerFactory.getLogger(GeoLocationService.class);

    public Optional<GeoLocationVO> getLocationByIp(String ip) {
        try {
            String url = UriComponentsBuilder.fromUriString(API_URL + ip)
                    .queryParam("fields", "countryCode,region,city,lat,lon")
                    .build()
                    .toUriString();

            GeoLocationDTO location = restTemplate.getForObject(url, GeoLocationDTO.class);
            Optional.ofNullable(location).map(GeoLocationVO::new);
        } catch (Exception e) {
            logger.error("Unable to find localization from ip {}: {}", ip, e.getMessage());
        }
        return Optional.empty();
    }

    public record GeoLocationDTO(
            String country,
            String region,
            String city,
            Double lat,
            Double lon
    ) {
    }
}
