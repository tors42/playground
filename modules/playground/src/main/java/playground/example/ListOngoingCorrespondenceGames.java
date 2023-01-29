package playground.example;

import java.time.*;
import java.util.function.Function;
import java.util.Comparator;

import chariot.model.Enums.Color;
import chariot.model.Enums.PerfType;
import chariot.model.Game;
import chariot.Client;

class ListOngoingCorrespondenceGames {
    public static void main(String[] args) {
        String userId = args.length == 0 ? System.console().readLine("Lichess user id: ") : args[0];

        Function<Game, Color> ourColorInGame =
            game -> game.players().white().name().equalsIgnoreCase(userId) ? Color.white : Color.black;

        Function<Game, Color> colorToPlayInGame =
            game -> game.lastFen().contains(" w ") ? Color.white : Color.black;

        Function<Game, Boolean> isItOurMove =
            game -> ourColorInGame.apply(game) == colorToPlayInGame.apply(game);

        Function<Game, Duration> timeLeftToMove =
            game -> Duration.between(ZonedDateTime.now(), game.lastMoveAt().plusDays(game.daysPerTurn()));

        Comparator<Game> gamesWithOurMoveFirst    = Comparator.comparing(isItOurMove).reversed();
        Comparator<Game> gamesWithClosestDeadline = Comparator.comparing(timeLeftToMove);

        var client = Client.basic();

        var games = client.games().byUserId(userId, params -> params
                .perfType(PerfType.correspondence)
                .ongoing()
                .finished(false)
                .lastFen()
                .clocks()
                ).stream()
            .sorted(gamesWithOurMoveFirst.thenComparing(gamesWithClosestDeadline))
            .toList();

        int wPadding = games.stream().mapToInt(game -> game.players().white().name().length()).max().orElse(15);
        int bPadding = games.stream().mapToInt(game -> game.players().black().name().length()).max().orElse(15);

        games.forEach(game -> System.out.format(
                    "%3d hours left - https://lichess.org/%s %"+wPadding+"s - %-"+bPadding+"s  %2d days/move%n",
                    timeLeftToMove.apply(game).toHours(),
                    game.id(),
                    game.players().white().name() + (colorToPlayInGame.apply(game) == Color.white ? "*" : ""),
                    game.players().black().name() + (colorToPlayInGame.apply(game) == Color.black ? "*" : ""),
                    game.daysPerTurn()
                    ));
    }
}
