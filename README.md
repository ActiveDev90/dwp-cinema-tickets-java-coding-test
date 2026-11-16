# Cinema Tickets — Java 21

A Java implementation of the Department of Work and Pensions (DWP) coding exercise.
The service validates a ticket purchase, calculates its total price and required seats,
then calls the supplied payment and seat-reservation services,
based on the supplied https://github.com/dwp/cinema-tickets.

## Requirements

- Java 21
- Apache Maven 3.x

##   Build and test

From the project root (the directory containing pom.xml):
  `mvn clean verify`
A successful verify run compiles the code,
runs the JUnit 5 tests and packages target/cinema-tickets-1.0.0.jar

## Implementation Overview

1. TicketServiceImpl implements the supplied TicketService contract. It performs validation before invoking external dependencies,
   then calls TicketPaymentService.makePayment(...) followed by SeatReservationService.reserveSeat(...).
2. TicketBreakdown is a small record that encapsulates validated adult, child and infant counts and calculates total cost and required seats.
3. TicketTypeRequest is immutable and represents a ticket category and quantity.
4. InvalidPurchaseException represents invalid purchase requests and supports descriptive error messages.
   The service supports constructor injection of the payment and seat-reservation interfaces so tests can use Mockito mocks.
   A no-argument constructor supplies the provided third-party implementations for normal use.

## Business rules

Some introductory text.

| Rule                             | Handling                                     |
|----------------------------------|----------------------------------------------|
| Adult £25, child £15, infant £0  | Total calculated from ticket counts          |
| Infants do not occupy seats      | Only adult and child tickets require seats   |
| Maximum 25 tickets per purchase  | Includes infants; overflow-safe validation   |
| Child or infant without an adult | Purchase rejected                            |
| Infants sit on adults' laps      | **Assumption:** at most one infant per adult |


## Testing

The JUnit 5 and Mockito tests cover:
- Invalid account IDs, missing requests, invalid types and quantities.
- The 25-ticket boundary, purchases across multiple entries and very large quantities.
- Adult-accompaniment and infant-ratio validation.
- Ticket pricing and seat totals, including free infants.
- Repeated ticket types and input order independence.
- Payment-before-reservation ordering and account ID propagation.
- No external calls for invalid or partially invalid baskets.
- Descriptive validation exception messages.
  Tests use injected mocks to verify interactions with the third-party service interfaces without making real payment or reservation calls.