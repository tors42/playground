# Playground

Use GitHub Codespaces to try out the [Chariot](https://github.com/tors42/chariot) Java library for accessing Lichess API.

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=586354374)

[Example](https://user-images.githubusercontent.com/4084220/213922943-5f6d0c22-68f6-44ad-ac5b-bbba8f8df8f3.webm)

## Build

The Codespace contains a pre-built Maven project which is configured to create a Java runtime image of the application.

The project can be re-built via Terminal with:

    mvn clean package

## Run Example

The Maven project is configured to create a "launcher" named `simple` and can be run via Terminal with:

    ./modules/runtime/target/maven-jlink/default/bin/simple

The launcher is just a "shortcut" for running the default `main`-class of the `playground` module, which can also be done explicitly via Terminal with:

    ./modules/runtime/target/maven-jlink/default/bin/java --module playground

To run another `main`-class, say `playground.example.ResultHandling`, specify it via Terminal with:

    ./modules/runtime/target/maven-jlink/default/bin/java --module playground/playground.example.ResultHandling

And for completeness, the last `main`-class,

    ./modules/runtime/target/maven-jlink/default/bin/java --module playground/playground.example.ResultHandlingDeconstruct

