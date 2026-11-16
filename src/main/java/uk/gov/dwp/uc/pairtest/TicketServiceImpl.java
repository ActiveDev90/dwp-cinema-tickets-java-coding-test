package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;

import java.util.Objects;


public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS = 25;


    private final TicketPaymentService paymentService;

    private final SeatReservationService seatReservationService;


    public TicketServiceImpl() {
        this(
                new TicketPaymentServiceImpl(),
                new SeatReservationServiceImpl()
        );
    }

    public TicketServiceImpl(
            TicketPaymentService paymentService,
            SeatReservationService seatReservationService
    ) {
        this.paymentService = Objects.requireNonNull(paymentService);
        this.seatReservationService =
                Objects.requireNonNull(seatReservationService);
    }



    @Override
    public void purchaseTickets(Long accountId,
                                TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        TicketBreakdown breakdown =
                validatePurchase(accountId, ticketTypeRequests);

        int totalAmount = breakdown.totalAmount();
        int seatsRequired = breakdown.seatsRequired();

        paymentService.makePayment(accountId, totalAmount);

        seatReservationService.reserveSeat(accountId, seatsRequired);
    }


    /**
     * Validates a purchase and returns its ticket breakdown.
     * InvalidPurchaseException is thrown if the purchase is invalid
     */
    private TicketBreakdown validatePurchase(
            Long accountId,
            TicketTypeRequest... ticketTypeRequests
    ) {

        // The account ID must be greater than zero.
        // Validate the account before processing a purchase.
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID must be a positive number.");
        }

        // A purchase must contain at least one
        // ticket request. A ticket must not be null or empty
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket request is required.");
        }


        int totalTickets = 0;

        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            if (request == null) {
                throw new InvalidPurchaseException("Ticket request cannot be null.");
            }

            if (request.getTicketType() == null) {
                throw new InvalidPurchaseException("Ticket type is required.");
            }

            int quantity = request.getNoOfTickets();

            if (quantity <= 0) {
                throw new InvalidPurchaseException("Ticket quantity must be greater than zero.");
            }

            // Maximum 25 tickets per purchase.
            // Check before addition to prevent overflow.
            if (quantity > MAX_TICKETS - totalTickets) {
                throw new InvalidPurchaseException("A purchase cannot exceed 25 tickets.");
            }

            totalTickets += quantity;

            switch (request.getTicketType()) {
                case ADULT -> adultTickets += quantity;
                case CHILD -> childTickets += quantity;
                case INFANT -> infantTickets += quantity;
            }
        }

            if (adultTickets == 0
                    && (childTickets > 0 || infantTickets > 0)) {
                throw new InvalidPurchaseException("An adult ticket is required for child or infant tickets.");
            }

            if (infantTickets > adultTickets) {
                throw new InvalidPurchaseException(
                        "Infant ticket quantity cannot exceed adult ticket quantity.");
            }

            return new TicketBreakdown(
                    adultTickets,
                    childTickets,
                    infantTickets
            );


    }

}
