package service;

import model.Ride;
import util.DistanceCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple routing helper that "calculates" an optimized route and an ETA for a ride.
 * The implementation is intentionally naive: it creates one or two segments (driver->pickup,
 * pickup->destination) and uses DistanceCalculator to estimate distances and times.
 *
 * When the driver's current location is unknown, only the origin->destination segment
 * is generated. This allows the system to populate an initial ETA as soon as the ride
 * request is created; the full route is recalculated after a driver accepts the ride.
 */
public class RouteService {
    private static final double DEFAULT_SPEED_KMH = 30.0; // used by DistanceCalculator when using addresses

    public void generateRoute(Ride ride) {
        if (ride == null) {
            return;
        }

        List<String> steps = new ArrayList<>();
        int totalEta = 0;

        String origin = ride.getOrigin() != null ? ride.getOrigin().getAddress() : null;
        String destination = ride.getDestination() != null ? ride.getDestination().getAddress() : null;
        String driverLoc = ride.getDriverCurrentLocation() != null
                ? ride.getDriverCurrentLocation().getAddress()
                : null;

        // segment from driver to pickup (only if we know driver location)
        if (driverLoc != null && origin != null) {
            double dist = DistanceCalculator.calculateDistance(driverLoc, origin);
            steps.add(String.format("Motorista -> Passageiro (%.1f km)", dist));
            totalEta += DistanceCalculator.calculateEstimatedTime(driverLoc, origin);
        }

        // segment from pickup to destination
        if (origin != null && destination != null) {
            double dist = DistanceCalculator.calculateDistance(origin, destination);
            steps.add(String.format("Passageiro -> Destino (%.1f km)", dist));
            totalEta += DistanceCalculator.calculateEstimatedTime(origin, destination);
        }

        ride.setOptimizedRoute(steps);
        ride.setEstimatedTimeMinutes(totalEta);
    }
}
