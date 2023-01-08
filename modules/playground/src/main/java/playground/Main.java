package playground;

import chariot.Client;

public class Main {

    public static void main(String[] args) {

        // Initialize a basic client
        Client client = Client.basic();

        // The id of the team to lookup,
        // last part in https://lichess.org/team/lichess-swiss
        String teamId = "lichess-swiss";

        // Fetch the single team using the client,
        // mapping the result to a info string,
        // or if no result a default string.
        // (For more examples of how to handle results, see @{link playground.examples.ResultHandling}
        String message = client.teams().byTeamId(teamId)
            .map(team -> "Team " + team.name() + " has " + team.nbMembers() + " members!")
            .orElse("Couldn't find team " + teamId);

        System.out.println(message);
    }
}
