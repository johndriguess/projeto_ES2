package test;

import model.Passenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserRepositoryTest {

    private Passenger passenger;

    @BeforeEach
    public void setUp() {
        passenger = new Passenger("Test User", "test@email.com", "12345", "pass");
    }

    @Test
    public void testAddSingleRating() {
        passenger.addRating(5);
        assertEquals(5.0, passenger.getAverageRating());
        assertEquals(1, passenger.getTotalRatings());
    }

    @Test
    public void testAddMultipleRatings() {
        passenger.addRating(5);
        passenger.addRating(3);
        assertEquals(4.0, passenger.getAverageRating());
        assertEquals(2, passenger.getTotalRatings());
    }

    @Test
    public void testRatingClampingUpper() {
        passenger.addRating(10);
        assertEquals(5.0, passenger.getAverageRating());
    }

    @Test
    public void testRatingClampingLower() {
        passenger.addRating(0);
        assertEquals(1.0, passenger.getAverageRating());
        
        passenger.addRating(-5);
        assertEquals(1.0, passenger.getAverageRating()); 
        assertEquals(2, passenger.getTotalRatings());
    }

    @Test
    public void testNoRatings() {
        assertEquals(0.0, passenger.getAverageRating());
        assertEquals(0, passenger.getTotalRatings());
    }
}