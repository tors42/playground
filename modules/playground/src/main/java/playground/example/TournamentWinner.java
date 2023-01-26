package playground.example;

import chariot.Client;
import chariot.model.Arena;

public class TournamentWinner {

    public static void main(String[] args) {

        String arenaId = args.length == 0 ? "qzRBGPLN" : args[0];

        winnerOfArena1(arenaId);

        winnerOfArena2(arenaId);
    }

    static void winnerOfArena1(String arenaId) {
        var client = Client.basic();

        var resultPage1 = client.tournaments().arenaById(arenaId);
        if (resultPage1.isPresent()) {
            Arena arena = resultPage1.get();
            printArena(arena);
        }

        var resultPage2 = client.tournaments().arenaById(arenaId, 2);
        if (resultPage2.isPresent()) {
            Arena arena = resultPage2.get();
            printArena(arena);
        }
    }

    static void printArena(Arena arena) {
        System.out.println("Name: " + arena.fullName());
        var standing = arena.standing();
        var players = standing.players();
        System.out.println("Number of players page %d: %d".formatted(standing.page(), players.size()));
        players.stream().limit(3).forEach(System.out::println);
    }


    static void winnerOfArena2(String arenaId) {
        var client = Client.basic();

        client.tournaments().resultsByArenaId(arenaId).stream()
            .limit(3)
            .forEach(System.out::println);

    }

}
