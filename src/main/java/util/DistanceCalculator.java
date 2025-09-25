package util;

public class DistanceCalculator {
    
    private static final int WAITING_TIME_MINUTES = 7;
    
    public static double calculateDistance(String origin, String destination) {
        if (origin == null || destination == null) {
            return 0.0;
        }
        
        int originHash = Math.abs(origin.toLowerCase().hashCode());
        int destHash = Math.abs(destination.toLowerCase().hashCode());
        
        double baseDistance = 2.0 + (Math.abs(originHash - destHash) % 23);
        
        double lengthFactor = (origin.length() + destination.length()) / 100.0;
        baseDistance += lengthFactor;
        
        return Math.round(baseDistance * 10.0) / 10.0;
    }
    
    public static int calculateEstimatedTime(double distanceKm, double averageSpeedKmH) {
        if (distanceKm <= 0 || averageSpeedKmH <= 0) {
            return 0;
        }
        
        double timeHours = distanceKm / averageSpeedKmH;
        int timeMinutes = (int) Math.round(timeHours * 60) + WAITING_TIME_MINUTES;
        
        return Math.max(10, Math.min(120, timeMinutes));
    }
    
    public static int calculateEstimatedTime(String origin, String destination) {
        double distance = calculateDistance(origin, destination);
        double averageSpeedKmH = 30.0;
        return calculateEstimatedTime(distance, averageSpeedKmH);
    }

    public static int getWaitingTime() {
        return WAITING_TIME_MINUTES;
    }
}