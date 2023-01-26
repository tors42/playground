package playground.example;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.*;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Player.User;

public class DownloadGames {

    static Path win  = Path.of("games-win.pgn");
    static Path draw = Path.of("games-draw.pgn");
    static Path loss = Path.of("games-loss.pgn");

    public static void main(String[] args) throws Exception {

        for (var file : List.of(win, draw, loss))
            Files.deleteIfExists(file);

        String userId;
        if (args.length == 0) {
            System.out.print("Lichess user id: ");
            userId = System.console().readLine();
        } else {
            userId = args[0];
        }

        var client = initializeClient(); //initializeClient("API-TOKEN");

        //launchHttpServer();

        var counter = new LongAdder();
        Consumer<Game> progress = __ -> {
            counter.increment();
            if (counter.intValue() % 100 == 0) System.out.println("Downloaded %d games".formatted(counter.intValue()));
        };

        record PgnFile(String pgn, Path file) {}

        var until = ZonedDateTime.now().withDayOfYear(1);
        var since = until.minusYears(1);

        System.out.println("Downloading games from %s to %s".formatted(DateTimeFormatter.ISO_LOCAL_DATE.format(since), DateTimeFormatter.ISO_LOCAL_DATE.format(until)));

        System.out.println("Downloaded %d games".formatted(counter.intValue()));
        client.games().byUserId(userId, parameters -> parameters
                .pgn()
                .clocks()
                .since(since)
                .until(until)
                )
            .stream()
            .peek(progress)
            .map(game -> new PgnFile(game.pgn(), destinationFile(game, userId)))
            .forEach(pgnFile -> writeToFile(pgnFile.file(), pgnFile.pgn()));
        System.out.println("Downloaded %d games".formatted(counter.intValue()));
    }

    static Path destinationFile(Game game, String myId) {
        if (game.winner() == null) return draw;
        return switch(game.winner()) {
            //case null  -> draw; // codespace vscode run doesn't like null in switch, so above workaround
            case white -> game.players().white() instanceof User u && u.id().equals(myId) ? win : loss;
            case black -> game.players().black() instanceof User u && u.id().equals(myId) ? win : loss;
        };
    }

    static void writeToFile(Path file, String data) throws UncheckedIOException {
        try { Files.writeString(file, data, CREATE, APPEND); }
        catch (IOException ioe) { throw new UncheckedIOException(ioe); }
    }

    static Client initializeClient() {
        return Client.basic();
    }

    static ClientAuth initializeClient(String token) {
        return Client.auth(token);
    }

    static void launchHttpServer() throws Exception {
        var bindAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8000);
        var httpServer = com.sun.net.httpserver.HttpServer.create(bindAddress, 0);

        var env = System.getenv();
        if (env.containsKey("CODESPACE_NAME")) {
            System.out.println("ctrl+click to open -> https://%s-8000.%s/".formatted(
                        env.get("CODESPACE_NAME"),
                        env.get("GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN")));
        } else {
            System.out.println("Open in browser: http://" + httpServer.getAddress());
        }

        httpServer.createContext("/", exchange -> {
            switch(exchange.getRequestURI().getPath()) {

                case "/win" -> respondFile(exchange, win);
                case "/draw" -> respondFile(exchange, draw);
                case "/loss" -> respondFile(exchange, loss);

                default -> respond(exchange, 200, """
                <html>
                    <body>
                        <a href="/win">Win</a>
                        <a href="/draw">Draw</a>
                        <a href="/loss">Loss</a>
                    </body>
                </html>
                """);
            };
        });
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
    }

    static void respond(com.sun.net.httpserver.HttpExchange exchange, int code, String body) {
        var bytes = body.getBytes();
        try {
            exchange.getResponseHeaders().put("content-type", List.of("text/html"));
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        } finally{
            exchange.close();
        }
    }

    static void respondFile(com.sun.net.httpserver.HttpExchange exchange, Path file) {
        try {
            if (Files.exists(file)) {
                byte[] bytes = Files.readAllBytes(file);
                exchange.getResponseHeaders().put("content-type", List.of("text/plain"));
                exchange.getResponseHeaders().put("content-disposition", List.of("attachment;filename=\""+file.getFileName().toString()+"\""));
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } else {
                exchange.sendResponseHeaders(404, 0);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        } finally{
            exchange.close();
        }
    }

}
