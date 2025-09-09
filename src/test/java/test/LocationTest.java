package test;

import model.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Location - RF04
 */
public class LocationTest {
    
    @Test
    void testLocationConstructor_WithAddressOnly() {
        // Act
        Location location = new Location("Rua A, 123");
        
        // Assert
        assertEquals("Rua A, 123", location.getAddress());
        assertEquals("", location.getDescription());
    }
    
    @Test
    void testLocationConstructor_WithAddressAndDescription() {
        // Act
        Location location = new Location("Rua A, 123", "Próximo ao shopping");
        
        // Assert
        assertEquals("Rua A, 123", location.getAddress());
        assertEquals("Próximo ao shopping", location.getDescription());
    }
    
    @Test
    void testLocationConstructor_TrimsWhitespace() {
        // Act
        Location location = new Location("  Rua A, 123  ", "  Próximo ao shopping  ");
        
        // Assert
        assertEquals("Rua A, 123", location.getAddress());
        assertEquals("Próximo ao shopping", location.getDescription());
    }
    
    @Test
    void testLocationConstructor_NullDescription() {
        // Act
        Location location = new Location("Rua A, 123", null);
        
        // Assert
        assertEquals("Rua A, 123", location.getAddress());
        assertEquals("", location.getDescription());
    }
    
    @Test
    void testSetDescription() {
        // Arrange
        Location location = new Location("Rua A, 123");
        
        // Act
        location.setDescription("Nova descrição");
        
        // Assert
        assertEquals("Nova descrição", location.getDescription());
    }
    
    @Test
    void testSetDescription_TrimsWhitespace() {
        // Arrange
        Location location = new Location("Rua A, 123");
        
        // Act
        location.setDescription("  Nova descrição  ");
        
        // Assert
        assertEquals("Nova descrição", location.getDescription());
    }
    
    @Test
    void testSetDescription_Null() {
        // Arrange
        Location location = new Location("Rua A, 123", "Descrição original");
        
        // Act
        location.setDescription(null);
        
        // Assert
        assertEquals("", location.getDescription());
    }
    
    @Test
    void testToString_WithoutDescription() {
        // Arrange
        Location location = new Location("Rua A, 123");
        
        // Act
        String result = location.toString();
        
        // Assert
        assertEquals("Rua A, 123", result);
    }
    
    @Test
    void testToString_WithDescription() {
        // Arrange
        Location location = new Location("Rua A, 123", "Próximo ao shopping");
        
        // Act
        String result = location.toString();
        
        // Assert
        assertEquals("Rua A, 123 - Próximo ao shopping", result);
    }
    
    @Test
    void testEquals_SameAddress() {
        // Arrange
        Location location1 = new Location("Rua A, 123");
        Location location2 = new Location("Rua A, 123");
        
        // Act & Assert
        assertTrue(location1.equals(location2));
    }
    
    @Test
    void testEquals_DifferentAddress() {
        // Arrange
        Location location1 = new Location("Rua A, 123");
        Location location2 = new Location("Rua B, 456");
        
        // Act & Assert
        assertFalse(location1.equals(location2));
    }
    
    @Test
    void testEquals_SameObject() {
        // Arrange
        Location location = new Location("Rua A, 123");
        
        // Act & Assert
        assertTrue(location.equals(location));
    }
    
    @Test
    void testEquals_Null() {
        // Arrange
        Location location = new Location("Rua A, 123");
        
        // Act & Assert
        assertFalse(location.equals(null));
    }
    
    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Location location = new Location("Rua A, 123");
        String other = "Rua A, 123";
        
        // Act & Assert
        assertFalse(location.equals(other));
    }
    
    @Test
    void testHashCode_SameAddress() {
        // Arrange
        Location location1 = new Location("Rua A, 123");
        Location location2 = new Location("Rua A, 123");
        
        // Act & Assert
        assertEquals(location1.hashCode(), location2.hashCode());
    }
    
    @Test
    void testHashCode_DifferentAddress() {
        // Arrange
        Location location1 = new Location("Rua A, 123");
        Location location2 = new Location("Rua B, 456");
        
        // Act & Assert
        assertNotEquals(location1.hashCode(), location2.hashCode());
    }
}
