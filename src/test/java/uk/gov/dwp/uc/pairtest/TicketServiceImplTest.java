package uk.gov.dwp.uc.pairtest;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;



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

    // Mockito
    @Test
    void shouldProcessPaymentAndReserveSeatsForValidPurchase() {

        // Arrange: Create mock implementations of both services.
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        // Inject the mocks into TicketServiceImpl.
        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        // Create a valid purchase.
        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                2
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                3
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                1
        );

        // Act: Attempt the purchase.
        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

        // Assert: Verify the correct payment amount.
        verify(paymentService).makePayment(123L, 95);

        // Assert: Verify the correct number of reserved seats.
        verify(seatReservationService).reserveSeat(123L, 5);
    }


    @Test
    @DisplayName("Should Not Call External Services For Invalid Purchase")
    void shouldNotCallExternalServicesForInvalidPurchase() {

        // Arrange: Create mocked dependencies.
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        // Invalid purchase: children without an adult.
        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                2
        );

        // Act and Assert: Purchase must be rejected.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        childRequest
                )
        );

        // Neither external service should have been called.
        verifyNoInteractions(paymentService, seatReservationService);
    }


    @Test
    @DisplayName("Should Process Payment Before Reserving Seats")
    void shouldProcessPaymentBeforeReservingSeats() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                2
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                3
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                1
        );

        // Act
        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

        // Assert: Verify the sequence of external calls.
        InOrder inOrder = inOrder(
                paymentService,
                seatReservationService
        );

        inOrder.verify(paymentService).makePayment(123L, 95);

        inOrder.verify(seatReservationService).reserveSeat(123L, 5);

        // Ensure there were no additional service calls.
        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Allow Purchase Of Exactly 25 Tickets")
    void shouldAllowPurchaseOfExactly25Tickets() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                20
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                4
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                1
        );

        // Total tickets = 20 + 4 + 1 = 25.
        // Total amount = (20 * 25) + (4 * 15) = 560.
        // Required seats = 20 + 4 = 24.

        // Act
        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

        // Assert
        verify(paymentService).makePayment(123L, 560);

        verify(seatReservationService).reserveSeat(123L, 24);

        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Aggregate Repeated Ticket Types")
    void shouldAggregateRepeatedTicketTypes() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        // Act: Submit repeated ADULT and CHILD entries.
        ticketService.purchaseTickets(
                123L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        );

        // Assert:
        // Adults = 3, Children = 4, Infants = 1.
        // Payment = (3 * 25) + (4 * 15) = 135.
        // Seats = 3 + 4 = 7.
        verify(paymentService).makePayment(123L, 135);

        verify(seatReservationService).reserveSeat(123L, 7);

        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Accept Adult Tickets Appearing Last")
    void shouldAcceptAdultTicketAppearingLast() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        // Act: The adult request appears after the children
        // and infant requests.
        ticketService.purchaseTickets(
                123L,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        );

        // Assert:
        // Payment = 25 + (2 * 15) = 55.
        // Seats = 1 adult + 2 children = 3.
        verify(paymentService).makePayment(123L, 55);

        verify(seatReservationService).reserveSeat(123L, 3);

        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Use Provided Account ID For Payment And Reservation")
    void shouldUseProvidedAccountIdForPaymentAndReservation() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        Long accountId = 987654321L;

        TicketTypeRequest adultRequest =
                new TicketTypeRequest(
                        TicketTypeRequest.Type.ADULT,
                        4
                );

        // Act
        ticketService.purchaseTickets(accountId, adultRequest);

        // Assert: Both services receive the same account ID.
        verify(paymentService).makePayment(accountId, 100);

        verify(seatReservationService).reserveSeat(accountId, 4);

        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Not Call External Services When Last Request Is Null")
    void shouldNotCallExternalServicesWhenLastRequestIsNull() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                2
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                1
        );

        // Act and Assert
        // The first two requests are valid.
        // The final null request makes the purchase invalid.
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        childRequest,
                        null
                )
        );

        // Ensure no payment or reservation occurred.
        verifyNoInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Not Call External Services When Total Exceeds 25")
    void shouldNotCallExternalServicesWhenTotalExceeds25() {

        // Arrange
        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                20
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                4
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                2
        );

        // Total = 20 + 4 + 2 = 26.

        // Act and Assert
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        childRequest,
                        infantRequest
                )
        );

        // No charge or seat reservation is permitted.
        verifyNoInteractions(
                paymentService,
                seatReservationService
        );
    }
















}
