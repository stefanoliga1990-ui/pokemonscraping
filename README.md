# Pokemon Price Monitor

Spring Boot application for monitoring Pokemon card prices through the CardTrader API.

## Local configuration

The CardTrader JWT must be provided through the `CARDTRADER_API_TOKEN` environment variable. Never store the token in source files.

PowerShell example, after copying the token to the clipboard:

```powershell
$env:CARDTRADER_API_TOKEN = (Get-Clipboard -Raw).Trim()
.\mvnw.cmd spring-boot:run
```

Other supported environment variables are documented in `.env.example`. The default monitoring schedule is every Monday at 06:00 in the `Europe/Rome` time zone.

## Build

```powershell
.\mvnw.cmd clean test
```

The project Maven settings use Maven Central directly and do not depend on the user-level Maven settings.
