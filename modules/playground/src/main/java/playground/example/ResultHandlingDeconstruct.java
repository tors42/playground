package playground.example;

import java.util.List;
import java.util.stream.Stream;

import chariot.Client;

import chariot.model.*;

public class ResultHandlingDeconstruct {

    // Deconstruct Record Pattern Matching is a preview feature,
    // works fine to build with Maven and then run.
    // But running a full build with eclipse / redhat.java VS Code extension,
    // the generated class file differs from the one created by Maven,
    // and it can't run...
    // Haven't figured out how to make the VS Code extension create a
    // proper class file for this preview feature yet,
    // so putting this preview feature in this separate class,
    // so at least the other @{code ResultHandling} class works as expected
    // with both VS Code extension and Maven.

    // So run this with Maven instead of via Run-button in VS Code :(
    // 1. mvn clean package
    // 2. ./modules/runtime/target/maven-jlink/default/bin/java --module playground/playground.example.ResultHandlingDeconstruct

    public static void main(String[] args) {
        // Some chariot methods return results of type One<T>,
        // and some methods return results of type Many<T>

        exampleHandlingOne();

        exampleHandlingMany();
    }


    // One<Team>
    static void exampleHandlingOne() {
        Client client = Client.basic();
        String teamId = "lichess-swiss";

        // Looking up a team by its id - only one team (or none if not found or other error)
        One<Team> oneTeam = client.teams().byTeamId(teamId);

        // Different ways to extract the Team out of the One<Team> result
        String oneResult1 = instanceofDeconstruct(oneTeam);
        String oneResult2 =     switchDeconstruct(oneTeam);

        // All methods should extract the same message,
        // so only 1 distinct message should be shown by below println
        System.out.println("Result One<Team>:");
        List.of(oneResult1, oneResult2)
            .stream()
            .distinct() //removes duplicates
            .forEach(System.out::println);
    }


    static String instanceofDeconstruct(One<Team> result) {
        // instanceof Record deconstruction pattern match. Allows access to the contained value immediately.
        if (result instanceof Entry<Team>(Team team)) {
            String message = formatOne(team);
            return message;
        }
        return "No team!";
    }

    static String switchDeconstruct(One<Team> result) {
        // switch expression Record deconstruction pattern match
        return switch(result) {
            case Entry<Team>(Team team)                -> formatOne(team);
            case Fail<Team>(int statusCode, var error) -> "Code: " + statusCode + " - " + error.message();
            case None<Team>()                          -> "No team!";
            default -> "No team!"; // <-- This default-clause shouldn't be needed for exhaustiveness - Fixed in Java 20 build 27
        };
    }

    static String formatOne(Team team) {
        return "Team %s has %d members!".formatted(team.name(), team.nbMembers());
    }

    // Many<Team>
    static void exampleHandlingMany() {
        Client client = Client.basic();
        String searchTerm = "Lichess";

        // Searching for teams matching a search term - all teams matching only (or none if not found or error)
        Many<Team> manyTeams = client.teams().search(searchTerm);

        List<Team> list = manyTeams.stream().filter(team -> team.name().contains(searchTerm)).limit(4).toList();

        // Different ways to extract the Stream<Team> out of the Many<Team> result
        String manyResults1 = instanceofDeconstruct( simulateFreshResult(list) );
        String manyResults2 =     switchDeconstruct( simulateFreshResult(list) );

        // All methods should extract the same message,
        // so only 1 distinct message should be shown by below println
        System.out.println("Result Many<Team> (3):");
        List.of(manyResults1, manyResults2)
            .stream()
            .distinct() //removes duplicates
            .forEach(System.out::println);
    }

    static Many<Team> simulateFreshResult(List<Team> list) {
        return Many.entries(list.stream());
    }

    static String instanceofDeconstruct(Many<Team> result) {
        // instanceof Record deconstruction pattern match. Allows access to the contained stream immediately.
        if (result instanceof Entries<Team>(Stream<Team> stream)) {
            String message = formatThree(stream);
            return message;
        }
        return "No teams!";
    }

    static String switchDeconstruct(Many<Team> result) {
        // switch expression Record deconstruction pattern match
        return switch(result) {
            case Entries<Team>(Stream<Team> stream)    -> formatThree(stream);
            case Fail<Team>(int statusCode, var error) -> "Code: " + statusCode + " - " + error.message();
        };
    }

    static String formatThree(Stream<Team> teams) {
        return String.join("\n", teams.limit(3).map(ResultHandling::formatOne).toList());
    }

}
