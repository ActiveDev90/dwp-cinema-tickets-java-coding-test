package uk.gov.dwp.uc.pairtest;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class TicketServiceImplTest {

    @Test
    @DisplayName("Should Reject Null Account ID")
    void shouldRejectNullAccountId() {

        // Arrange: create our service.
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

        // Act and Assert: a null account ID must be rejected.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(null, request)
        );
    }

    @Test
    @DisplayName("Should Reject Zero Account ID")
    void shouldRejectZeroAccountId() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

        // Act and Assert
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, request)
        );
    }

    @Test
    @DisplayName("Should Reject Negative Account ID")
    void shouldRejectNegativeAccountId() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

        // Act and Assert
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-1L, request)
        );
    }


    @Test
    @DisplayName("Should Reject Null Ticket Requests")
    void shouldRejectNullTicketRequests() {

        // Arrange: create the ticket service.
        TicketService ticketService = new TicketServiceImpl();

        // Act and Assert: a null ticket request array
        // must be rejected even when the account is valid.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        (TicketTypeRequest[]) null
                )
        );
    }

    @Test
    @DisplayName("Should Reject Empty Ticket Requests")
    void shouldRejectEmptyTicketRequests() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        // Act and Assert: purchasing no tickets is invalid.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L)
        );
    }


    @Test
    @DisplayName("Should Reject Null Ticket Request Entry")
    void shouldRejectNullTicketRequestEntry() {

        // Arrange: create the service.
        TicketService ticketService = new TicketServiceImpl();

        // Create a valid adult ticket request.
        TicketTypeRequest adultRequest =
                new TicketTypeRequest(
                        TicketTypeRequest.Type.ADULT,
                        2
                );

        // The second element is deliberately null.
        TicketTypeRequest[] requests = {
                adultRequest,
                null
        };

        // Act and Assert: reject the entire purchase
        // because one of its entries is null.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        requests
                )
        );
    }


    @Test
    @DisplayName("Should Reject Null Ticket Type")
    void shouldRejectNullTicketType() {

        // Arrange: create a request with no ticket category.
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                null,
                2
        );

        // Act and Assert: ticket type must be specified.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );
    }

    @Test
    @DisplayName("Should Reject Zero Ticket Quantity")
    void shouldRejectZeroTicketQuantity() {

        // Arrange: a customer requests zero adult tickets.
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                0
        );

        // Act and Assert: zero quantities must be rejected.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );
    }

    @Test
    @DisplayName("Should Reject Negative Ticket Quantity")
    void shouldRejectNegativeTicketQuantity() {

        // Arrange: a negative child ticket quantity.
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                -3
        );

        // Act and Assert: negative quantities must be rejected.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );
    }


    @Test
    @DisplayName("Should Reject More Than 25 Tickets In Single Request")
    void shouldRejectMoreThan25TicketsInSingleRequest() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                26
        );

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );
    }

    @Test
    @DisplayName("Should Reject More Than 25 Tickets Across Multiple Requests")
    void shouldRejectMoreThan25TicketsAcrossMultipleRequests() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                12
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                9
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                5
        );

        // Total = 12 + 9 + 5 = 26 tickets.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        childRequest,
                        infantRequest
                )
        );
    }

    @Test
    @DisplayName("Should Reject Extremely Large Ticket Quantities")
    void shouldRejectExtremelyLargeTicketQuantities() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest firstRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                Integer.MAX_VALUE
        );

        TicketTypeRequest secondRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                Integer.MAX_VALUE
        );

        TicketTypeRequest thirdRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                3
        );

        // A naive int sum would overflow and wrap to 1.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        firstRequest,
                        secondRequest,
                        thirdRequest
                )
        );
    }


    @Test
    @DisplayName("Should Reject Child Tickets Without Adult")
    void shouldRejectChildTicketsWithoutAdult() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                2
        );

        // Act and Assert
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        childRequest
                )
        );
    }

    @Test
    @DisplayName("Should Reject Infant Tickets Without Adult")
    void shouldRejectInfantTicketsWithoutAdult() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                1
        );

        // Act and Assert
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        infantRequest
                )
        );
    }

    @Test
    @DisplayName("Should Reject More Infants Than Adults")
    void shouldRejectMoreInfantsThanAdults() {

        // Arrange
        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                2
        );

        // Act and Assert
        // Assumption: each infant requires a separate adult lap.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        infantRequest
                )
        );
    }







}
