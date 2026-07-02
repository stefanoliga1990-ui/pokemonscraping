# Pokemon Price Monitor

Spring Boot application for monitoring Pokemon card prices through the CardTrader API.

## Local configuration

The CardTrader JWT can be stored locally in `secrets/application-secrets.properties`:

```properties
CARDTRADER_API_TOKEN_FILE_VALUE=your-token
```

This file is loaded automatically when the application starts from the project root. It is ignored by Git and is not packaged in the application JAR. Never put the real token in `application.properties` or in the `.example` file.

Alternatively, the `CARDTRADER_API_TOKEN` environment variable can be used and takes precedence over the local secrets file.

PowerShell example, after copying the token to the clipboard:

```powershell
$env:CARDTRADER_API_TOKEN = (Get-Clipboard -Raw).Trim()
.\mvnw.cmd spring-boot:run
```

Other supported environment variables are documented in `.env.example`. The default monitoring schedule is every Monday at 06:00 in the `Europe/Rome` time zone.

## CardTrader client

The backend client is read-only and supports Pokemon expansions, categories, blueprints and marketplace offers. It applies the configured connection/read timeouts and throttles requests before they reach CardTrader's limits.

The default minimum interval is `50ms` for general API calls and a conservative `1s` for marketplace calls. They can be overridden through `CARDTRADER_MIN_REQUEST_INTERVAL` and `CARDTRADER_MARKETPLACE_MIN_REQUEST_INTERVAL`.

No token is required to run the test suite. A real `CARDTRADER_API_TOKEN` is required only when the application executes an actual CardTrader request.

## Build

```powershell
.\mvnw.cmd clean test
```

The project Maven settings use Maven Central directly and do not depend on the user-level Maven settings.
