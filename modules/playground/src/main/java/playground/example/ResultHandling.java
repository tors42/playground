package playground.example;

import java.util.List;
import java.util.stream.Stream;

import chariot.Client;

import chariot.model.*;

public class ResultHandling {

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
        String oneResult1 =             unsafeGet(oneTeam);
        String oneResult2 =                 maybe(oneTeam);
        String oneResult3 =                   map(oneTeam);
        String oneResult4 =        instanceofType(oneTeam);
        String oneResult5 = instanceofTypeNegated(oneTeam);
        String oneResult6 =            switchType(oneTeam);
        String oneResult7 = instanceofDeconstruct(oneTeam);
        String oneResult8 =     switchDeconstruct(oneTeam);


        // All methods should extract the same message,
        // so only 1 distinct message should be shown by below println
        System.out.println("Result One<Team>:");
        List.of(oneResult1, oneResult2, oneResult3, oneResult4, oneResult5, oneResult6, oneResult7, oneResult8)
            .stream()
            .distinct() //removes duplicates
            .forEach(System.out::println);
    }

    static String unsafeGet(One<Team> result) {
        // Not recommended, since no check if the result contains a Team or not
        Team team = result.get(); // Unsafe extraction

        String message = formatOne(team);
        return message;
    }

    static String maybe(One<Team> result) {
        // Team wrapped in standard @{link java.util.Optional}
        java.util.Optional<Team> optionalTeam = result.maybe();

        if (optionalTeam.isPresent()) {
            Team team = optionalTeam.get(); // Mostly safe extraction, since inside `isPresent()`-block.
            String message = formatOne(team);
            return message;
        } else {
            return "No team!";
        }
    }

    static String map(One<Team> result) {
        // Apply function Team -> String to result, if any, or else default
        String message = result
            .map(team -> formatOne(team))
            .orElse("No team!");
        return message;
    }

    static String instanceofType(One<Team> result) {
        // instanceof Type pattern match
        if (result instanceof Entry<Team> one) {
            Team team = one.entry(); // Safe extraction
            String message = formatOne(team);
            return message;
        } else {
            return "No team!";
        }
    }

    static String instanceofTypeNegated(One<Team> result) {
        // instanceof Type pattern match, negated. Allows for shallower indentation.
        if (! (result instanceof Entry<Team> one)) {
            return "No team!";
        }

        Team team = one.entry();
        String message = formatOne(team);
        return message;
    }

    static String switchType(One<Team> result) {
        // switch expression pattern match
        return switch(result) {
            case Entry<Team> one -> formatOne(one.entry());
            case Fail<Team> fail -> fail.message();
            case None<Team> none -> "No team!";
        };
    }


    static String instanceofDeconstruct(One<Team> result) {
        // instanceof Record deconstruction pattern match. Allows access to the contained value immediately.
        if (result instanceof Entry(Team team)) {
            String message = formatOne(team);
            return message;
        }
        return "No team!";
    }

    static String switchDeconstruct(One<Team> result) {
        // switch expression Record deconstruction pattern match
        return switch(result) {
            case Entry(Team team)                -> formatOne(team);
            case Fail(int statusCode, var error) -> "Code: " + statusCode + " - " + error.message();
            case None _                          -> "No team!";
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

        // A successful Many result will contain a Stream with the results.
        // Unlike a One<Team> result, the Many<Team> result is only processable once.
        // So this example code which is trying to show different ways of handling a result,
        // will make use of a simulated response in order to not need to re-send the request
        // to Lichess multiple times.

        // Collecting the first 4 found teams into a list, which will be used to simulate fresh results.
        List<Team> list = manyTeams.stream().filter(team -> team.name().contains(searchTerm)).limit(4).toList();

        // Different ways to extract the Stream<Team> out of the Many<Team> result
        String manyResults1 =   unconditionalStream( simulateFreshResult(list) );
        String manyResults2 =        instanceofType( simulateFreshResult(list) );
        String manyResults3 = instanceofTypeNegated( simulateFreshResult(list) );
        String manyResults4 =            switchType( simulateFreshResult(list) );
        String manyResults5 = instanceofDeconstruct( simulateFreshResult(list) );
        String manyResults6 =     switchDeconstruct( simulateFreshResult(list) );


        // All methods should extract the same message,
        // so only 1 distinct message should be shown by below println
        System.out.println("Result Many<Team> (3):");
        List.of(manyResults1, manyResults2, manyResults3, manyResults4, manyResults5, manyResults6)
            .stream()
            .distinct() //removes duplicates
            .forEach(System.out::println);
    }

    static Many<Team> simulateFreshResult(List<Team> list) {
        return Many.entries(list.stream());
    }

    static String unconditionalStream(Many<Team> result) {
        // Regardless if the result is successful or failed,
        // we will return a stream (which is empty if result is failed)
        return formatThree(result.stream());
    }

    static String instanceofType(Many<Team> result) {
        // instanceof Type pattern match
        if (result instanceof Entries<Team> many) {
            Stream<Team> teams = many.stream(); // Safe extraction
            String message = formatThree(teams);
            return message;
        } else {
            return "No teams!";
        }
    }

    static String instanceofTypeNegated(Many<Team> result) {
        // instanceof Type pattern match, negated. Allows for shallower indentation.
        if (! (result instanceof Entries<Team> many)) {
            return "No teams!";
        }

        Stream<Team> teams = many.stream();
        String message = formatThree(teams);
        return message;
    }

    static String switchType(Many<Team> result) {
        // switch expression pattern match
        return switch(result) {
            case Entries<Team> many -> formatThree(many.stream());
            case Fail<Team> fail    -> fail.message();
        };
    }

    static String instanceofDeconstruct(Many<Team> result) {
        // instanceof Record deconstruction pattern match. Allows access to the contained stream immediately.
        if (result instanceof Entries(Stream<Team> stream)) {
            String message = formatThree(stream);
            return message;
        }
        return "No teams!";
    }

    static String switchDeconstruct(Many<Team> result) {
        // switch expression Record deconstruction pattern match
        return switch(result) {
            case Entries(Stream<Team> stream)    -> formatThree(stream);
            case Fail(int statusCode, var error) -> "Code: " + statusCode + " - " + error.message();
        };
    }

    static String formatThree(Stream<Team> teams) {
        return String.join("\n", teams.limit(3).map(ResultHandling::formatOne).toList());
    }

}
