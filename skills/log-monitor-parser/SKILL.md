---
name: log-monitor-parser
description: "Parse local text log files line by line, extract supported fields, filter records by an inclusive time range, write them to MySQL, count ERROR records, and print a threshold alert. Use for local log monitoring workflows; do not use for web scraping, PDF processing, or GUI work."
---

# Log Monitor Parser

Use this skill when the input is a local text log and the requested result is a MySQL-backed ERROR count with a configurable alert threshold. Keep the workflow streaming and reproducible. Confirm the input path, time range, threshold, database name, table shape, and connection settings before changing code or running a live import. Never put database passwords in source, SQL files, examples, or output.

## Knowledge cards

### Card 1: Stream the file

- **Core knowledge:** Open the local file with a buffered reader and process one line at a time.
- **Brief explanation:** Do not load the complete file into memory; this is the requirement that makes large-log processing safe.
- **Example or self-test:** For a file larger than available memory, ask: "Does the implementation hold only the current line and a bounded insert batch?"

### Card 2: Parse the supported log grammar

- **Core knowledge:** Parse `yyyy-MM-dd HH:mm:ss LEVEL Interface - message`, where `LEVEL` is exactly `INFO`, `WARN`, or `ERROR`; extract the timestamp, level, interface token, and an optional `cost:<number>ms` from the message.
- **Brief explanation:** Use one anchored regular expression plus strict date parsing. A line with an invalid date, unsupported level, missing separator, or non-numeric cost is invalid and must not be stored.
- **Example or self-test:** `2026-09-28 10:20:15 INFO UserController - query user api, cost:23ms` yields time, `INFO`, `UserController`, and `23`; an `ERROR` message is the exception text.

### Card 3: Filter before persistence

- **Core knowledge:** Keep only parsed records whose timestamp is within the requested start and end timestamps, including both endpoints, before inserting them into MySQL.
- **Brief explanation:** Filtering before persistence prevents out-of-range records from entering the import result and keeps the stored dataset aligned with the requested window.
- **Example or self-test:** With `10:00:00` through `10:05:00`, keep records at exactly `10:00:00` and `10:05:00`, and discard `09:59:59` and `10:05:01`.

### Card 4: Write with JDBC safely

- **Core knowledge:** Insert filtered records with JDBC `PreparedStatement` parameters and bounded batches; commit successful batches and roll back the failing batch.
- **Brief explanation:** Parameter binding avoids SQL built from log text, while bounded batches reduce round trips without turning the whole file into one transaction.
- **Example or self-test:** Verify that `exception_message` and interface names containing quotes are inserted without changing the SQL statement.

### Card 5: Count and alert on ERROR

- **Core knowledge:** Count records with `level = 'ERROR'` inside the same inclusive time range and print an alert only when the count is strictly greater than the configured threshold.
- **Brief explanation:** Equality with the threshold is not an alert. Use a database count query rather than an in-memory counter that can miss already persisted batches.
- **Example or self-test:** A count of `3` with threshold `2` alerts; a count of `2` with threshold `2` does not.

### Card 6: Handle operational failures

- **Core knowledge:** Report missing/unreadable files, malformed input lines, and database connection or statement failures separately; close readers, statements, and connections.
- **Brief explanation:** A bad line should be skipped and counted, while a file or database failure should produce a clear non-success result. Do not silently continue after a database failure.
- **Example or self-test:** Ask: "Can the user distinguish a missing file from a missing table or invalid database credentials from the console output?"

## Scope boundaries

- Input is a local text `.log` file. Do not add web fetching, PDF parsing, or a graphical interface.
- Keep the existing Java 8/Maven conventions when adapting this repository, including the `com.logtool` package and MySQL JDBC dependency unless the user explicitly changes them.
- Do not invent extra log formats, fields, severity values, or alert destinations. Ask for a format change when the input does not match the supported grammar.
