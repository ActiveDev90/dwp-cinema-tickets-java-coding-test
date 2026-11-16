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

        // Validate the complete purchase.
        TicketBreakdown breakdown =
                validatePurchase(accountId, ticketTypeRequests);

        // Calculate the payment amount.
        int totalAmount = breakdown.totalAmount();

        // Calculate the number of seats required.
        int seatsRequired = breakdown.seatsRequired();

        // Process the payment.
        paymentService.makePayment(accountId, totalAmount);

        // Reserve the seats after payment.
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

        // Rule 1: The account ID must be greater than zero.
        // Validate the account before processing a purchase.
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID must be a positive number.");
        }

        // Rule 2: A purchase must contain at least one
        // ticket request. A ticket must not be null or empty
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket request is required.");
        }

        // Accumulate tickets across all request entries.
        int totalTickets = 0;

        // Track quantities for each ticket type.
        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;

        // Validate individual ticket request
        for (TicketTypeRequest request : ticketTypeRequests) {

            // Rule 3: Each ticket request must exist.
            if (request == null) {
                throw new InvalidPurchaseException("Ticket request cannot be null.");
            }

            // Rule 4: A ticket category must be specified.
            if (request.getTicketType() == null) {
                throw new InvalidPurchaseException("Ticket type is required.");
            }

            // Rule 5: Quantity must be positive.
            int quantity = request.getNoOfTickets();

            if (quantity <= 0) {
                throw new InvalidPurchaseException("Ticket quantity must be greater than zero.");
            }

            // Rule 6: Maximum 25 tickets per purchase.
            // Check before addition to prevent overflow.
            if (quantity > MAX_TICKETS - totalTickets) {
                throw new InvalidPurchaseException("A purchase cannot exceed 25 tickets.");
            }

            totalTickets += quantity;

            // Count tickets by category.
            switch (request.getTicketType()) {
                case ADULT -> adultTickets += quantity;
                case CHILD -> childTickets += quantity;
                case INFANT -> infantTickets += quantity;
            }
        }

            // Rule 7: Child and infant tickets require an adult.
            if (adultTickets == 0
                    && (childTickets > 0 || infantTickets > 0)) {
                throw new InvalidPurchaseException("An adult ticket is required for child or infant tickets.");
            }

            // Assumption: Each infant needs a separate adult lap.
            if (infantTickets > adultTickets) {
                throw new InvalidPurchaseException(
                        "Infant ticket quantity cannot exceed adult ticket quantity.");
            }


            // Validation succeeded: return the quantities.
            return new TicketBreakdown(
                    adultTickets,
                    childTickets,
                    infantTickets
            );


    }

}
