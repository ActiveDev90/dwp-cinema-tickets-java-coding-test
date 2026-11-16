package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketTypeRequestTest {

    @Test
    @DisplayName("Should Store Ticket Type And Quantity")
    void shouldStoreTicketTypeAndQuantity() {

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                3
        );

        TicketTypeRequest.Type actualType =
                request.getTicketType();

        int actualQuantity = request.getNoOfTickets();

        assertEquals(
                TicketTypeRequest.Type.ADULT,
                actualType
        );

        assertEquals(3, actualQuantity);
    }
}
