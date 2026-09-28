# log-parser-tool

`log-parser-tool` is a Java 8 console tool for importing local application logs
into MySQL and detecting error spikes. It reads large files line by line,
filters records by an inclusive time range, persists matching records, counts
`ERROR` rows, and prints an alert when the count is greater than a user-defined
threshold.

The repository also contains the reusable Codex Skill
`skills/log-monitor-parser`. The project does not support web scraping, PDF
processing, or a graphical interface.

## Main features

- Stream UTF-8 `.log` files with a buffered reader instead of loading the whole file.
- Parse `yyyy-MM-dd HH:mm:ss LEVEL Interface - message` records for `INFO`, `WARN`, and `ERROR`.
- Extract the timestamp, log level, interface name, optional `cost:<number>ms`, and ERROR message.
- Keep only records between the requested start and end timestamps, including both endpoints.
- Insert records into MySQL with JDBC prepared statements and 500-row transactions.
- Count ERROR records and print an alert only when `count > threshold`.
- Report missing files, invalid lines, and database failures with console messages.

## Installation

Prerequisites:

- JDK 8 or newer
- Maven 3.x
- MySQL 8.x or a compatible MySQL server

Build the executable JAR from the project root:

```powershell
mvn clean package
```

Create the database and table by running `schema.sql` in MySQL Workbench or the
MySQL client:

```powershell
mysql -u root -p -e "source schema.sql"
```

The build creates `target\log-parser-tool-1.0.0-jar-with-dependencies.jar`.

## Usage

Set the database connection variables in the same PowerShell window used to run
the JAR. Do not commit the password or put it in a source file:

```powershell
$env:LOGTOOL_DB_URL = 'jdbc:mysql://localhost:3306/log_parser_tool?useSSL=false&serverTimezone=Asia/Shanghai'
$env:LOGTOOL_DB_USER = 'root'
$env:LOGTOOL_DB_PASSWORD = 'your-password'
```

Run the program with four arguments:

```text
java -jar <jar-file> <file.log> <start: yyyy-MM-dd HH:mm:ss> <end: yyyy-MM-dd HH:mm:ss> <threshold>
```

Example:

```powershell
java -jar target\log-parser-tool-1.0.0-jar-with-dependencies.jar `
  sample.log '2026-09-28 00:00:00' '2026-09-28 23:59:59' 0
```

The start and end timestamps are inclusive. The threshold must be nonnegative.
Malformed lines and valid records outside the time range are both counted as
`skipped`. Re-running the same file inserts duplicate rows; the tool does not
deduplicate imports.

## Input and output example

Input file:

```text
2026-09-28 10:20:15 INFO  UserController - query user api, cost:23ms
2026-09-28 10:20:20 ERROR OrderController - database connection timeout
2026-09-28 10:20:21 WARN  UserController - slow query, cost:500ms
```

With the command above, the console output is:

```text
Imported: 3; skipped: 0; ERROR count: 1
ALERT: ERROR count 1 exceeds threshold 0.
```

The imported rows are written to `log_entries` with `log_time`, `level`,
`interface_name`, `request_cost_ms`, and `exception_message`. For INFO and WARN
rows, `exception_message` is `NULL`; for ERROR rows it contains the message.

Run the automated tests with:

```powershell
mvn test
```
