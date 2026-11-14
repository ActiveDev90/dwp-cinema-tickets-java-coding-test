package uk.gov.dwp.uc.pairtest;

/**
 * Represents the quantities of each ticket type
 * and provides ticket pricing and seat calculations.
 */
record TicketBreakdown(
        int adultTickets,
        int childTickets,
        int infantTickets
) {

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;

    /**
     * Calculates the total payment in pounds.
     * Infant tickets are free.
     */
    int totalAmount() {
        return (adultTickets * ADULT_TICKET_PRICE)
                + (childTickets * CHILD_TICKET_PRICE);
    }

    /**
     * Calculates the number of seats required.
     * Infants sit on adults' laps and require no seats.
     */
    int seatsRequired() {
        return adultTickets + childTickets;
    }
}
