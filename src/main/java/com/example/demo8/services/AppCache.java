package com.example.demo8.services;

import com.example.demo8.models.City;
import com.example.demo8.models.Flight;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppCache {

    private static final long TTL_MS = 5 * 60 * 1000; // 5 минут

    // Города с рейсами
    private List<City> cities;
    private long citiesTime = 0;

    // Все города без фильтрации
    private List<City> allCities;
    private long allCitiesTime = 0;

    // Все рейсы (для Дейкстры)
    private List<Flight> allFlights;
    private long allFlightsTime = 0;

    // Карта cityId -> name (производная от allCities)
    private Map<UUID, String> cityNameMap;

    public boolean isCitiesValid()    { return cities != null     && System.currentTimeMillis() - citiesTime    < TTL_MS; }
    public boolean isAllCitiesValid() { return allCities != null  && System.currentTimeMillis() - allCitiesTime < TTL_MS; }
    public boolean isAllFlightsValid(){ return allFlights != null && System.currentTimeMillis() - allFlightsTime < TTL_MS; }

    public List<City> getCities()    { return cities; }
    public List<City> getAllCities() { return allCities; }
    public List<Flight> getAllFlights() { return allFlights; }
    public Map<UUID, String> getCityNameMap() { return cityNameMap; }

    public void setCities(List<City> cities) {
        this.cities = cities;
        this.citiesTime = System.currentTimeMillis();
    }

    public void setAllCities(List<City> allCities) {
        this.allCities = allCities;
        this.allCitiesTime = System.currentTimeMillis();
    }

    public void setAllFlights(List<Flight> flights, Map<UUID, String> nameMap) {
        this.allFlights = flights;
        this.cityNameMap = nameMap;
        this.allFlightsTime = System.currentTimeMillis();
    }

    public void invalidate() {
        citiesTime = 0;
        allCitiesTime = 0;
        allFlightsTime = 0;
    }
}
