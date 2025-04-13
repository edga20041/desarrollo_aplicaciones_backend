package com.example.desarrollo_aplicaciones.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoCodingService {
    private static final String API_KEY = "AIzaSyDFT0PTre5bG8loS6X2K9ujVJ-AkeRxZgM";
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json?address={address}&key={key}";

    public double[] geocode(String address) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("key", API_KEY);

        Map response = restTemplate.getForObject(GEOCODING_URL, Map.class, params);
        if (response != null && response.get("results") != null) {
            Map location = (Map) ((Map) ((Map) ((java.util.List) response.get("results")).get(0)).get("geometry")).get("location");
            double lat = (double) location.get("lat");
            double lng = (double) location.get("lng");
            return new double[]{lat, lng};
        }
        throw new RuntimeException("No se pudieron obtener las coordenadas para la dirección: " + address);
    }
}
