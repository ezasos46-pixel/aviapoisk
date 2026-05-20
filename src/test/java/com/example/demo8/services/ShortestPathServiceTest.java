package com.example.demo8.services;

import com.example.demo8.models.Flight;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShortestPathServiceTest {

    @Test
    void findAllShortestPathsFrom_prefersCheaperMultiLegPath() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        Flight directExpensive = flight(a, c, "A-C", 300);
        Flight aToB = flight(a, b, "A-B", 100);
        Flight bToC = flight(b, c, "B-C", 80);

        ShortestPathService service = new ShortestPathService(null);
        Map<UUID, ShortestPathService.PathResult> result =
                service.findAllShortestPathsFrom(a, List.of(directExpensive, aToB, bToC));

        ShortestPathService.PathResult toC = result.get(c);
        assertNotNull(toC);
        assertEquals(new BigDecimal("180"), toC.totalPrice());
        assertEquals(2, toC.legs().size());
        assertEquals("A-B", toC.legs().get(0).getFlightNumber());
        assertEquals("B-C", toC.legs().get(1).getFlightNumber());
    }

    @Test
    void findAllShortestPathsFrom_returnsEmptyWhenNoUsableFlights() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        Flight invalid = new Flight();
        invalid.setFromCityId(from);
        invalid.setToCityId(null);
        invalid.setBasePrice(new BigDecimal("50"));

        ShortestPathService service = new ShortestPathService(null);
        Map<UUID, ShortestPathService.PathResult> result =
                service.findAllShortestPathsFrom(from, List.of(invalid));

        assertTrue(result.isEmpty());
        assertFalse(result.containsKey(to));
    }

    @Test
    void findAllShortestPathsFrom_ignoresUnreachableCities() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID x = UUID.randomUUID();
        UUID y = UUID.randomUUID();

        Flight aToB = flight(a, b, "A-B", 90);
        Flight xToY = flight(x, y, "X-Y", 40);

        ShortestPathService service = new ShortestPathService(null);
        Map<UUID, ShortestPathService.PathResult> result =
                service.findAllShortestPathsFrom(a, List.of(aToB, xToY));

        assertTrue(result.containsKey(b));
        assertFalse(result.containsKey(x));
        assertFalse(result.containsKey(y));
    }

    @Test
    void findAllShortestPathsFrom_choosesBestDirectFlightWhenSeveralDirectOptions() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        Flight expensive = flight(from, to, "D-1", 500);
        Flight cheap = flight(from, to, "D-2", 120);

        ShortestPathService service = new ShortestPathService(null);
        Map<UUID, ShortestPathService.PathResult> result =
                service.findAllShortestPathsFrom(from, List.of(expensive, cheap));

        ShortestPathService.PathResult toResult = result.get(to);
        assertNotNull(toResult);
        assertEquals(new BigDecimal("120"), toResult.totalPrice());
        assertEquals(1, toResult.legs().size());
        assertEquals("D-2", toResult.legs().get(0).getFlightNumber());
    }

    @Test
    void findAllShortestPathsFrom_buildsPathWithThreeLegs() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();

        Flight aToB = flight(a, b, "A-B", 50);
        Flight bToC = flight(b, c, "B-C", 60);
        Flight cToD = flight(c, d, "C-D", 70);

        ShortestPathService service = new ShortestPathService(null);
        Map<UUID, ShortestPathService.PathResult> result =
                service.findAllShortestPathsFrom(a, List.of(aToB, bToC, cToD));

        ShortestPathService.PathResult toD = result.get(d);
        assertNotNull(toD);
        assertEquals(new BigDecimal("180"), toD.totalPrice());
        assertEquals(3, toD.legs().size());
        assertEquals("A-B", toD.legs().get(0).getFlightNumber());
        assertEquals("B-C", toD.legs().get(1).getFlightNumber());
        assertEquals("C-D", toD.legs().get(2).getFlightNumber());
    }

    private static Flight flight(UUID from, UUID to, String number, int price) {
        Flight flight = new Flight();
        flight.setFromCityId(from);
        flight.setToCityId(to);
        flight.setFlightNumber(number);
        flight.setBasePrice(BigDecimal.valueOf(price));
        return flight;
    }
}
