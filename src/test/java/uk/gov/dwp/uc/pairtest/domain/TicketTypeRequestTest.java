package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketTypeRequestTest {

    @Test
    @DisplayName("Should Store Ticket Type And Quantity")
    void shouldStoreTicketTypeAndQuantity() {

        // Arrange: create a request for three adult tickets.
        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                3
        );

        // Act: retrieve the values stored in the object.
        TicketTypeRequest.Type actualType =
                request.getTicketType();

        int actualQuantity = request.getNoOfTickets();

        // Assert: verify both values match expectations.
        assertEquals(
                TicketTypeRequest.Type.ADULT,
                actualType
        );

        assertEquals(3, actualQuantity);
    }
}
