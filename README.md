## Thought Process

I don't really like working on stuff alone without any requirements, I get affected by scope creep
and tend to over-refactor or over-engineer stuff. I like having another person to ask if we should
pause the subscription if the customer doesn't have sufficient funds for example.

I used Quartz scheduler as the scheduler implementation. I wanted to go with something robust and
ready not to have to reinvent the wheel. Kinda regret it now though, due to some gymnastics that I
had to do to inject dependencies to jobs (i.e. custom Job Factory). I could have used the
JobDataMap, but I did not like the idea. I'd rather have my dependencies in constructors.

I also opted of using `sealed classes` as State instead of exceptions, due to Kotlin not having
checked exceptions. Compiler doesn't enforce catching them which might prove troublesome. When using
exhaustive `when` the code won't compile until all the branches will be covered.

I didn't want BillingService handling all the stuff related to payment like updating statues or
retries. I feel like "Billing" doesn't need to know about this stuff, only payment. I couldn't
decide if BillingService should be a dependency to InvoiceService or the other way around... I came
to conclusion that it would be better to include BillingService as a dependency to InvoiceService,
as the Billing one is only responsible for payment and the Invoice one has much more uses.

All the errors are reported to mocked `Telemetry`. The idea is that the doesn't have to do anything
notification related (e.g. emails). That would be handled by some kind of alert rules or another
service that would read the job results.

I also added some comments around the code to explain some stuff I did in more detail.

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the
Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with
him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if
thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his
strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every
month. Our database contains a few invoices for the different markets in which we operate. Your task
is to build the logic that will schedule payment of those invoices on the first of the month. While
this may seem simple, there is space for some decisions to be taken and you will be expected to
justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
