package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketBreakdownTest {

    @Test
    @DisplayName("Should Calculate Total Payment For Mixed Tickets")
    void shouldCalculateTotalPaymentForMixedTickets() {

        // Arrange: 2 adults, 3 children, 1 infant.
        TicketBreakdown breakdown =
                new TicketBreakdown(2, 3, 1);

        // Expected: (2 * 25) + (3 * 15) = 95.
        int expectedAmount = 95;

        // Act
        int actualAmount = breakdown.totalAmount();

        // Assert
        assertEquals(expectedAmount, actualAmount);
    }

    @Test
    @DisplayName("Should Exclude Infants From Seat Reservation")
    void shouldExcludeInfantsFromSeatReservation() {

        // Arrange
        TicketBreakdown breakdown =
                new TicketBreakdown(2, 3, 1);

        // Act
        int actualSeats = breakdown.seatsRequired();

        // Assert: Only adults and children need seats.
        assertEquals(5, actualSeats);
    }

    @Test
    @DisplayName("Should Calculate Adult Only Purchase")
    void shouldCalculateAdultOnlyPurchase() {

        // Arrange: 4 adults.
        TicketBreakdown breakdown =
                new TicketBreakdown(4, 0, 0);

        // Act and Assert
        assertEquals(100, breakdown.totalAmount());
        assertEquals(4, breakdown.seatsRequired());
    }

    @Test
    void shouldNotChargeOrReserveSeatsForInfants() {

        // Arrange: 2 adults and 2 infants.
        TicketBreakdown breakdown =
                new TicketBreakdown(2, 0, 2);

        // Act and Assert
        assertEquals(50, breakdown.totalAmount());
        assertEquals(2, breakdown.seatsRequired());
    }
}
