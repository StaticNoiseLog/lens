package lens;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class LensTest {
    @Test
    void simpleLens() {
        User user = new User("johndoe", "jdoe@example.com", new Booking(new Show(new Movie("foo bar"), LocalDateTime.now()), 2));

        Lens<User, String> userNameLens = Lens.of(s -> s.username, User::withUsername);

        String username = userNameLens.get(user);
        Assertions.assertEquals("johndoe", username);

        user = userNameLens.set(user, "janedoe");
        Assertions.assertEquals("janedoe", user.username);
    }

    /**
     * We can compose multiple Lenses together which results in a new Lens. The new Lens will be able to look into the
     * object hierarchy deeper than which could be done from individual Lenses. With the composition of Lenses, the
     * depth of the object graph can be reached in a simpler way and then mutate/fetched easily. Let’s try to understand
     * it through an example:
     *
     * In order to create a composite Lens, we create a function f that accepts two Lenses and combines them to create a
     * new Lens. So, assume there’s a Lens 1 that talks to type A and B (where B is part of A following the composition
     * principle in OOP), and another Lens 2 that talks to type B and C (where C is part of B). The function f will
     * return a new Lens that talks not only to types A & B or B & C individually but also to type A & C going deep into
     * the inner levels.
     */
    @Test
    void lensComposition() {
        Lens<Movie, String> movieTitleLens = Lens.of(s -> s.title, Movie::withTitle);

        Lens<Show, Movie> showMovieLens = Lens.of(s -> s.movie, Show::withMovie);
        Lens<Show, LocalDateTime> showDateTimeLens = Lens.of(s -> s.dateTime, Show::withDateTime);

        Lens<Booking, Show> bookingShowLens = Lens.of(s -> s.show, Booking::withShow);
        Lens<Booking, Integer> bookingSeatsLens = Lens.of(s -> s.numSeats, Booking::withNumSeats);

        Lens<User, String> userNameLens = Lens.of(s -> s.username, User::withUsername);
        Lens<User, String> userEmailLens = Lens.of(s -> s.emailId, User::withEmailId);
        Lens<User, Booking> userBookingLens = Lens.of(s -> s.booking, User::withBooking);

        // Lens composition
        Lens<User, String> changeMovieName = userBookingLens.andThen(bookingShowLens).andThen(showMovieLens).andThen(movieTitleLens);
        Lens<User, LocalDateTime> changeShowDateTime = userBookingLens.andThen(bookingShowLens).andThen(showDateTimeLens);
        Lens<User, Integer> changeBookingSeats = userBookingLens.andThen(bookingSeatsLens);

        // Immutable Structure
        User user = new User("johndoe", "jdoe@example.com", new Booking(new Show(new Movie("shawshank redemption"), LocalDateTime.now()), 2));

        // Mutations through lenses
        user = changeMovieName.mod(user, s -> "street race");
        Assertions.assertEquals("street race", user.booking.show.movie.title);

        user = changeShowDateTime.mod(user, s -> LocalDateTime.of(2021, 10, 14, 5, 30));
        Assertions.assertEquals("2021-10-14T05:30", user.booking.show.dateTime.toString());

        user = changeBookingSeats.mod(user, s -> 3);
        Assertions.assertEquals(3, user.booking.numSeats);
    }
}
