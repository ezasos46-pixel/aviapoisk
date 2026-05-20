package com.example.demo8.services;

import com.example.demo8.models.Flight;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ShortestPathService {

    private final SupabaseClient supabaseClient;

    public ShortestPathService(SupabaseClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public record PathResult(List<Flight> legs, BigDecimal totalPrice) {}

    /**
     * Дейкстра по графу рейсов. Рёбра — рейсы на ближайшие 30 дней от сегодня.
     * Вес ребра — base_price рейса.
     */
    /**
     * Вычисляет кратчайшие пути от fromCityId до всех остальных городов.
     * Возвращает Map: toCityId -> PathResult
     */
    public Map<UUID, PathResult> findAllShortestPathsFrom(UUID fromCityId, List<Flight> allFlights) {
        Map<UUID, List<Flight>> graph = new HashMap<>();
        Set<UUID> allCityIds = new HashSet<>();
        for (Flight f : allFlights) {
            if (f.getFromCityId() != null && f.getToCityId() != null) {
                graph.computeIfAbsent(f.getFromCityId(), k -> new ArrayList<>()).add(f);
                allCityIds.add(f.getFromCityId());
                allCityIds.add(f.getToCityId());
            }
        }

        Map<UUID, BigDecimal> dist = new HashMap<>();
        Map<UUID, Flight> prevFlight = new HashMap<>();
        PriorityQueue<UUID> pq = new PriorityQueue<>(Comparator.comparing(id -> dist.getOrDefault(id, BigDecimal.valueOf(Long.MAX_VALUE))));

        for (UUID id : allCityIds) dist.put(id, BigDecimal.valueOf(Long.MAX_VALUE));
        dist.put(fromCityId, BigDecimal.ZERO);
        pq.add(fromCityId);

        while (!pq.isEmpty()) {
            UUID current = pq.poll();
            for (Flight flight : graph.getOrDefault(current, Collections.emptyList())) {
                UUID next = flight.getToCityId();
                BigDecimal newDist = dist.get(current).add(flight.getBasePrice());
                if (newDist.compareTo(dist.getOrDefault(next, BigDecimal.valueOf(Long.MAX_VALUE))) < 0) {
                    dist.put(next, newDist);
                    prevFlight.put(next, flight);
                    pq.remove(next);
                    pq.add(next);
                }
            }
        }

        Map<UUID, PathResult> results = new HashMap<>();
        for (UUID toId : allCityIds) {
            if (toId.equals(fromCityId)) continue;
            BigDecimal d = dist.getOrDefault(toId, BigDecimal.valueOf(Long.MAX_VALUE));
            if (d.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) == 0) continue;
            List<Flight> path = new ArrayList<>();
            UUID cur = toId;
            while (prevFlight.containsKey(cur)) {
                Flight f = prevFlight.get(cur);
                path.add(0, f);
                cur = f.getFromCityId();
            }
            results.put(toId, new PathResult(path, d));
        }
        return results;
    }

    public PathResult findShortestPath(UUID fromCityId, UUID toCityId) throws IOException {
        // Загружаем все рейсы одним запросом
        List<Flight> allFlights = supabaseClient.getAllFlights();

        // Строим граф: cityId -> список рейсов из этого города
        Map<UUID, List<Flight>> graph = new HashMap<>();
        for (Flight f : allFlights) {
            if (f.getFromCityId() != null && f.getToCityId() != null) {
                graph.computeIfAbsent(f.getFromCityId(), k -> new ArrayList<>()).add(f);
            }
        }

        // Собираем все уникальные города из рейсов
        Set<UUID> allCityIds = new HashSet<>();
        for (Flight f : allFlights) {
            if (f.getFromCityId() != null) allCityIds.add(f.getFromCityId());
            if (f.getToCityId() != null) allCityIds.add(f.getToCityId());
        }

        // Дейкстра
        Map<UUID, BigDecimal> dist = new HashMap<>();
        Map<UUID, Flight> prevFlight = new HashMap<>();
        PriorityQueue<UUID> pq = new PriorityQueue<>(Comparator.comparing(id -> dist.getOrDefault(id, BigDecimal.valueOf(Long.MAX_VALUE))));

        for (UUID id : allCityIds) {
            dist.put(id, BigDecimal.valueOf(Long.MAX_VALUE));
        }
        dist.put(fromCityId, BigDecimal.ZERO);
        pq.add(fromCityId);

        while (!pq.isEmpty()) {
            UUID current = pq.poll();
            if (current.equals(toCityId)) break;

            List<Flight> edges = graph.getOrDefault(current, Collections.emptyList());
            for (Flight flight : edges) {
                UUID next = flight.getToCityId();
                BigDecimal newDist = dist.get(current).add(flight.getBasePrice());
                if (newDist.compareTo(dist.getOrDefault(next, BigDecimal.valueOf(Long.MAX_VALUE))) < 0) {
                    dist.put(next, newDist);
                    prevFlight.put(next, flight);
                    pq.remove(next);
                    pq.add(next);
                }
            }
        }

        BigDecimal toDist = dist.getOrDefault(toCityId, BigDecimal.valueOf(Long.MAX_VALUE));
        if (toDist.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) == 0) {
            return null; // путь не найден
        }

        // Восстанавливаем путь
        List<Flight> path = new ArrayList<>();
        UUID cur = toCityId;
        while (prevFlight.containsKey(cur)) {
            Flight f = prevFlight.get(cur);
            path.add(0, f);
            cur = f.getFromCityId();
        }

        return new PathResult(path, toDist);
    }
}
