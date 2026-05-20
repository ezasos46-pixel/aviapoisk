package com.example.demo8.services;

import com.example.demo8.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SupabaseClient {

    private static SupabaseClient instance;

    public static SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    private static final String SUPABASE_URL = "https://ifymlutkxztuwgttaisq.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlmeW1sdXRreHp0dXdndHRhaXNxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYwNDc2NjMsImV4cCI6MjA4MTYyMzY2M30.TY2KOGAC8cv5WnHlNJyZNLpojtkVqwvM4INE-nxIIvQ";

    private final HttpClient http;
    private final Gson gson;
    private final AppCache cache;
    private final CityRepository cityRepo;
    private final FlightRepository flightRepo;
    private final BookingRepository bookingRepo;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private SupabaseClient() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.http = new HttpClient(SUPABASE_URL, SUPABASE_KEY);
        this.cache = new AppCache();
        this.cityRepo = new CityRepository(http, gson, cache);
        this.flightRepo = new FlightRepository(http, gson, cache, cityRepo);
        this.bookingRepo = new BookingRepository(http, gson, flightRepo);
    }

    // ===== Cities =====

    public List<City> getCities() throws IOException {
        return cityRepo.getCities();
    }

    // ===== Flights =====

    public List<Flight> getAllFlights() throws IOException {
        return flightRepo.getAllFlights();
    }

    public List<Flight> searchFlights(UUID fromCityId, UUID toCityId, LocalDate departureDate) throws IOException {
        return flightRepo.searchFlights(fromCityId, toCityId, departureDate);
    }

    public List<Flight> searchReturnFlights(UUID fromCityId, UUID toCityId, LocalDate returnDate) throws IOException {
        return flightRepo.searchReturnFlights(fromCityId, toCityId, returnDate);
    }

    public List<ConnectingFlight> searchConnectingFlights(UUID fromCityId, UUID stopCityId, UUID toCityId, LocalDate departureDate) throws IOException {
        return flightRepo.searchConnectingFlights(fromCityId, stopCityId, toCityId, departureDate);
    }

    // ===== Users =====

    public User registerUser(String email, String phone, String password) throws IOException {
        String passwordHash = String.valueOf(password.hashCode());
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("phone", phone);
        body.addProperty("password_hash", passwordHash);
        String response = http.post("users", body.toString());
        JsonArray arr = gson.fromJson(response, JsonArray.class);
        if (arr.size() > 0) return parseUser(arr.get(0).getAsJsonObject());
        return null;
    }

    public User loginUser(String emailOrPhone, String password) throws IOException {
        String passwordHash = String.valueOf(password.hashCode());
        String query = String.format("users?or=(email.eq.%s,phone.eq.%s)&password_hash=eq.%s&select=*",
                emailOrPhone, emailOrPhone, passwordHash);
        JsonArray arr = gson.fromJson(http.get(query), JsonArray.class);
        if (arr.size() > 0) return parseUser(arr.get(0).getAsJsonObject());
        return null;
    }

    public void updateUser(UUID userId, String email, String phone) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("phone", phone);
        http.patch("users?id=eq." + userId, body.toString());
    }

    public void deleteUser(UUID userId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("user_uuid", userId.toString());
        http.postRpc("delete_user_account", body.toString());
    }

    // ===== Bookings =====

    public Booking createBooking(Booking booking) throws IOException {
        return bookingRepo.createBooking(booking);
    }

    public List<Booking> getUserBookings(UUID userId) throws IOException {
        return bookingRepo.getUserBookings(userId);
    }

    public Booking findBookingByNumber(String bookingNumber) throws IOException {
        return bookingRepo.findBookingByNumber(bookingNumber);
    }

    // ===== Passengers =====

    public void createPassenger(Passenger passenger) throws IOException {
        bookingRepo.createPassenger(passenger);
    }

    // ===== Seed =====

    public void seedCities() throws IOException {
        String[][] cities = {
            {"Москва","MOW"},{"Санкт-Петербург","LED"},{"Новосибирск","OVB"},
            {"Екатеринбург","SVX"},{"Казань","KZN"},{"Нижний Новгород","GOJ"},
            {"Челябинск","CEK"},{"Самара","KUF"},{"Уфа","UFA"},
            {"Ростов-на-Дону","ROV"},{"Краснодар","KRR"},{"Пермь","PEE"},
            {"Воронеж","VOZ"},{"Волгоград","VOG"},{"Красноярск","KJA"},
            {"Саратов","RTW"},{"Тюмень","TJM"},{"Ижевск","IJK"},
            {"Барнаул","BAX"},{"Иркутск","IKT"},{"Хабаровск","KHV"},
            {"Владивосток","VVO"},{"Ярославль","IAR"},{"Омск","OMS"},
            {"Томск","TOF"},{"Кемерово","KEJ"},{"Новокузнецк","NOZ"},
            {"Рязань","RZN"},{"Астрахань","ASF"},{"Пенза","PEZ"},
            {"Липецк","LPK"},{"Тула","TYA"},{"Киров","KVX"},
            {"Чебоксары","CSY"},{"Калининград","KGD"},{"Брянск","BZK"},
            {"Курск","URS"},{"Иваново","IWA"},{"Магнитогорск","MQF"},
            {"Улан-Удэ","UUD"},{"Сочи","AER"},{"Мурманск","MMK"},
            {"Архангельск","ARH"},{"Якутск","YKS"},{"Южно-Сахалинск","UUS"},
            {"Петропавловск-Камчатский","PKC"},{"Магадан","GDX"},
            {"Нальчик","NAL"},{"Махачкала","MCX"},{"Грозный","GRV"},
            {"Владикавказ","OGZ"},{"Ставрополь","STW"},{"Симферополь","SIP"},
            {"Минеральные Воды","MRV"},{"Анапа","AAQ"},{"Геленджик","GDZ"},
            {"Белгород","EGO"},{"Орёл","OEL"},{"Тамбов","TBW"},
            {"Смоленск","LNX"},{"Псков","PKV"},{"Великий Новгород","NVR"},
            {"Вологда","VGD"},{"Петрозаводск","PES"},{"Сыктывкар","SCW"},
            {"Нарьян-Мар","NNM"},{"Салехард","SLY"},{"Ханты-Мансийск","HMA"},
            {"Сургут","SGC"},{"Нижневартовск","NJC"},{"Чита","HTA"},
            {"Благовещенск","BQS"},{"Комсомольск-на-Амуре","KXK"},
            {"Абакан","ABA"},{"Кызыл","KYZ"},{"Горно-Алтайск","RGK"},
            {"Элиста","ESL"},{"Саранск","SKX"},{"Йошкар-Ола","JOK"},
            {"Ульяновск","ULV"},{"Оренбург","REN"},{"Курган","KRO"},
            {"Норильск","NSK"},{"Воркута","VKT"},{"Анадырь","DYR"},
            {"Певек","PWE"},{"Мирный","MJZ"},{"Нерюнгри","NER"},
            {"Орск","OSW"},{"Стерлитамак","STL"},{"Набережные Челны","NBC"},
            {"Тобольск","TOX"},{"Нижнекамск","NKM"},{"Новороссийск","NOI"},
            {"Таганрог","TGK"},{"Армавир","ARV"},{"Волжский","VLZ"},
            {"Тверь","TVE"},{"Калуга","KLF"},{"Обнинск","OBN"},
            {"Кострома","KMW"},{"Владимир","VLD"},{"Рыбинск","RYB"},
            {"Дзержинск","DZR"},{"Арзамас","ARZ"},{"Муром","MUR"},
            {"Апатиты","APT"},{"Северодвинск","SVD"},{"Котлас","KTS"},
            {"Ухта","UCT"},{"Инта","INA"},{"Печора","PEX"},{"Усинск","USK"},
            {"Тольятти","TLT"},{"Сызрань","SZR"},{"Энгельс","ENS"},
            {"Балаково","BKW"},{"Камышин","KMY"},{"Шахты","SHA"},
            {"Батайск","BTK"},{"Новочеркасск","NCK"}
        };
        cityRepo.seedCities(cities);
    }

    public void seedFlights() throws IOException {
        List<City> cities = cityRepo.getAllCitiesUnfiltered();
        if (cities.isEmpty()) return;

        String airlinesJson = http.get("airlines?select=id,name&limit=10");
        JsonArray airlinesArr = gson.fromJson(airlinesJson, JsonArray.class);
        if (airlinesArr.size() == 0) {
            String[][] airlineData = {
                {"Аэрофлот","SU"},{"S7 Airlines","S7"},{"Победа","DP"},
                {"Уральские авиалинии","U6"},{"Россия","FV"}
            };
            JsonArray batch = new JsonArray();
            for (String[] a : airlineData) {
                JsonObject o = new JsonObject();
                o.addProperty("name", a[0]);
                o.addProperty("code", a[1]);
                batch.add(o);
            }
            String resp = http.post("airlines", batch.toString());
            airlinesArr = gson.fromJson(resp, JsonArray.class);
        }

        List<UUID> airlineIds = new ArrayList<>();
        for (int i = 0; i < airlinesArr.size(); i++)
            airlineIds.add(UUID.fromString(airlinesArr.get(i).getAsJsonObject().get("id").getAsString()));

        Map<String, City> cityByName = new HashMap<>();
        for (City c : cities) cityByName.put(c.getName(), c);

        String[] hubs = {"Москва","Санкт-Петербург","Новосибирск","Екатеринбург",
                "Краснодар","Красноярск","Хабаровск","Владивосток","Сочи"};

        List<UUID[]> pairs = new ArrayList<>();
        Set<String> pairKeys = new LinkedHashSet<>();
        for (City c : cities) {
            for (String hub : hubs) {
                City hubCity = cityByName.get(hub);
                if (hubCity == null || hubCity.getId().equals(c.getId())) continue;
                String k1 = c.getId() + "|" + hubCity.getId();
                String k2 = hubCity.getId() + "|" + c.getId();
                if (pairKeys.add(k1)) pairs.add(new UUID[]{c.getId(), hubCity.getId()});
                if (pairKeys.add(k2)) pairs.add(new UUID[]{hubCity.getId(), c.getId()});
            }
        }
        for (int i = 0; i < hubs.length; i++) {
            for (int j = i + 1; j < hubs.length; j++) {
                City a = cityByName.get(hubs[i]), b = cityByName.get(hubs[j]);
                if (a == null || b == null) continue;
                String k1 = a.getId() + "|" + b.getId(), k2 = b.getId() + "|" + a.getId();
                if (pairKeys.add(k1)) pairs.add(new UUID[]{a.getId(), b.getId()});
                if (pairKeys.add(k2)) pairs.add(new UUID[]{b.getId(), a.getId()});
            }
        }

        Map<String, Integer> existingCount = flightRepo.getExistingFlightPairs();
        String[] prefixes = {"SU","S7","DP","U6","FV"};
        Random rnd = new Random(42);
        int counter = 1000;
        JsonArray batch = new JsonArray();

        for (UUID[] pair : pairs) {
            String key = pair[0] + "|" + pair[1];
            int count = existingCount.getOrDefault(key, 0);
            if (count >= 2) continue;
            UUID airlineId = airlineIds.get(rnd.nextInt(airlineIds.size()));
            for (int f = 0; f < (2 - count); f++) {
                for (int day = 0; day < 365; day += 3) {
                    LocalDateTime dep = LocalDateTime.now().plusDays(day)
                            .withHour(6 + rnd.nextInt(14)).withMinute(rnd.nextInt(4) * 15).withSecond(0).withNano(0);
                    LocalDateTime arr = dep.plusHours(1 + rnd.nextInt(8)).plusMinutes(rnd.nextInt(60));
                    JsonObject flight = new JsonObject();
                    flight.addProperty("flight_number", prefixes[rnd.nextInt(prefixes.length)] + (counter++));
                    flight.addProperty("from_city_id", pair[0].toString());
                    flight.addProperty("to_city_id", pair[1].toString());
                    flight.addProperty("airline_id", airlineId.toString());
                    flight.addProperty("departure_time", dep.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    flight.addProperty("arrival_time", arr.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    flight.addProperty("base_price", 3000 + rnd.nextInt(27000));
                    flight.addProperty("available_seats", 50 + rnd.nextInt(151));
                    flight.addProperty("total_seats", 200);
                    batch.add(flight);
                    if (batch.size() >= 200) { insertFlightsBatch(batch); batch = new JsonArray(); }
                }
            }
        }
        if (batch.size() > 0) insertFlightsBatch(batch);
        cache.invalidate();
        System.out.println("seedFlights завершён");
    }

    private void insertFlightsBatch(JsonArray batch) throws IOException {
        http.postUpsert("flights", batch.toString());
    }

    private User parseUser(JsonObject obj) {
        User user = new User();
        user.setId(UUID.fromString(obj.get("id").getAsString()));
        user.setEmail(obj.get("email").getAsString());
        user.setPhone(obj.get("phone").getAsString());
        return user;
    }
}
