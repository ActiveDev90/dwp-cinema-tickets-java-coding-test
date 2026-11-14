package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS = 25;

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {


        // Rule 1: The account ID must be greater than zero.
        // Validate the account before processing a purchase.
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }

        // Rule 2: A purchase must contain at least one
        // ticket request. A ticket must not be null or empty
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
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
                throw new InvalidPurchaseException();
            }

            // Rule 4: A ticket category must be specified.
            if (request.getTicketType() == null) {
                throw new InvalidPurchaseException();
            }

            // Rule 5: Quantity must be positive.
            int quantity = request.getNoOfTickets();

            if (quantity <= 0) {
                throw new InvalidPurchaseException();
            }

            // Rule 6: Maximum 25 tickets per purchase.
            // Check before addition to prevent overflow.
            if (quantity > MAX_TICKETS - totalTickets) {
                throw new InvalidPurchaseException();
            }

            totalTickets += quantity;

            // Count tickets by category.
            switch (request.getTicketType()) {
                case ADULT -> adultTickets += quantity;
                case CHILD -> childTickets += quantity;
                case INFANT -> infantTickets += quantity;
            }

            // Rule 7: Child and infant tickets require an adult.
            if (adultTickets == 0
                    && (childTickets > 0 || infantTickets > 0)) {
                throw new InvalidPurchaseException();
            }

            // Rule 8: Each infant needs a separate adult lap.
            // This is our documented business assumption.
            if (infantTickets > adultTickets) {
                throw new InvalidPurchaseException();
            }
        }

    }

}
