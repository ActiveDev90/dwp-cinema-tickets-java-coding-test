package uk.gov.dwp.uc.pairtest;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TicketServiceImplTest {

    @Test
    @DisplayName("Should Reject Null Account ID")
    void shouldRejectNullAccountId() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

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

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, request)
        );

        assertEquals(
                "Account ID must be a positive number.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should Reject Negative Account ID")
    void shouldRejectNegativeAccountId() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

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
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        (TicketTypeRequest[]) null
                )
        );

        assertEquals(
                "At least one ticket request is required.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should Reject Empty Ticket Requests")
    void shouldRejectEmptyTicketRequests() {

        TicketService ticketService = new TicketServiceImpl();

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L)
        );
    }


    @Test
    @DisplayName("Should Reject Null Ticket Request Entry")
    void shouldRejectNullTicketRequestEntry() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest adultRequest =
                new TicketTypeRequest(
                        TicketTypeRequest.Type.ADULT,
                        2
                );

        // The second element is null.
        TicketTypeRequest[] requests = {
                adultRequest,
                null
        };

        // Act and Assert: reject the entire purchase
        // because one of its entries is null.
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        requests
                )
        );

        assertEquals(
                "Ticket request cannot be null.",
                exception.getMessage()
        );
    }


    @Test
    @DisplayName("Should Reject Null Ticket Type")
    void shouldRejectNullTicketType() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                null,
                2
        );

        InvalidPurchaseException exception= assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );

        assertEquals(
                "Ticket type is required.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should Reject Zero Ticket Quantity")
    void shouldRejectZeroTicketQuantity() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                0
        );

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );

        assertEquals(
                "Ticket quantity must be greater than zero.",
                exception.getMessage()
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

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(123L, request)
        );

        assertEquals(
                "A purchase cannot exceed 25 tickets.",
                exception.getMessage()
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

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                2
        );

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        childRequest
                )
        );

        assertEquals(
                "An adult ticket is required for child or infant tickets.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should Reject Infant Tickets Without Adult")
    void shouldRejectInfantTicketsWithoutAdult() {

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                1
        );

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

        TicketService ticketService = new TicketServiceImpl();

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT,
                1
        );

        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT,
                2
        );

        // Assumption: each infant requires a separate adult lap.
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        infantRequest
                )
        );

        assertEquals(
                "Infant ticket quantity cannot exceed adult ticket quantity.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should Process Payment And Reserve Seats For Valid Purchase")
    void shouldProcessPaymentAndReserveSeatsForValidPurchase() {

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

        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

        verify(paymentService).makePayment(123L, 95);

        verify(seatReservationService).reserveSeat(123L, 5);
    }


    @Test
    @DisplayName("Should Not Call External Services For Invalid Purchase")
    void shouldNotCallExternalServicesForInvalidPurchase() {

        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD,
                2
        );

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        childRequest
                )
        );

        // Neither external service should be called.
        verifyNoInteractions(paymentService, seatReservationService);
    }


    @Test
    @DisplayName("Should Process Payment Before Reserving Seats")
    void shouldProcessPaymentBeforeReservingSeats() {

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

        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

        InOrder inOrder = inOrder(
                paymentService,
                seatReservationService
        );

        inOrder.verify(paymentService).makePayment(123L, 95);

        inOrder.verify(seatReservationService).reserveSeat(123L, 5);

        verifyNoMoreInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Allow Purchase Of Exactly 25 Tickets")
    void shouldAllowPurchaseOfExactly25Tickets() {

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

        ticketService.purchaseTickets(
                123L,
                adultRequest,
                childRequest,
                infantRequest
        );

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

        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

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

        TicketPaymentService paymentService =
                mock(TicketPaymentService.class);

        SeatReservationService seatReservationService =
                mock(SeatReservationService.class);

        TicketService ticketService = new TicketServiceImpl(
                paymentService,
                seatReservationService
        );

        ticketService.purchaseTickets(
                123L,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        );

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

        ticketService.purchaseTickets(accountId, adultRequest);

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

        verifyNoInteractions(
                paymentService,
                seatReservationService
        );
    }


    @Test
    @DisplayName("Should Not Call External Services When Total Exceeds 25")
    void shouldNotCallExternalServicesWhenTotalExceeds25() {

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

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        123L,
                        adultRequest,
                        childRequest,
                        infantRequest
                )
        );

        verifyNoInteractions(
                paymentService,
                seatReservationService
        );
    }


















}
