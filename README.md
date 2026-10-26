# Attendance Monitoring & Shortage Prediction System

Core Java mini project for **CSC2104 – Object Oriented Programming** (Project #9).

The application imports attendance records from a CSV file, calculates subject-wise
percentages, detects attendance shortages, and tells a student exactly how many classes
they must attend (or can still miss) to meet the required percentage. It also projects
whether a student can still recover with the classes remaining in the semester.

## Features

| Feature | Description |
|---|---|
| CSV import | Reads `studentId,studentName,subject,conducted,attended`; bad lines are skipped and reported, never crash the import |
| Percentage calculation | Per subject and overall for each student |
| Shortage detection | Lists every (student, subject) below the required percentage, worst first |
| Required-class calculation | Consecutive classes a student must attend to reach the threshold |
| Bunk allowance | Classes a student can still miss while staying above the threshold |
| Projection | With *R* classes remaining, is the target still achievable? |
| Subject-wise statistics | Average, highest, lowest and shortage count per subject |
| Pluggable policy | Standard (75%) or Strict (85%) |
| Report export | Full text report written to `reports/attendance_report.txt` |

## OOP and Core Java concepts used

| Concept | Where |
|---|---|
| Abstraction | `Person` (abstract class, abstract `getRole()`), `AttendancePolicy` (interface) |
| Inheritance | `Student extends Person` |
| Polymorphism | `AttendancePolicy` implemented by `StandardPolicy` and `StrictPolicy`; `StrictPolicy` overrides `getWarningMargin()` |
| Encapsulation | Private final fields, validation in constructors (`SubjectAttendance`, `Person`) |
| Method overloading | `Student.addSubject(...)`, `AttendanceCalculator.percentage(...)` |
| Enum | `AttendanceStatus` (SAFE / WARNING / SHORTAGE) |
| Collections | `HashMap` (student lookup), `LinkedHashMap` (subjects), `TreeMap` (grouping), `ArrayList`, `Comparator` sorting |
| Exception handling | Custom checked exceptions `InvalidAttendanceException`, `StudentNotFoundException`; try-with-resources; exception chaining |
| File handling | `CsvImporter` (read), `ReportWriter` (write) using `java.nio.file` |
| Mathematical calculations | `AttendanceCalculator` (ceil/floor based formulas) |
| Packages, static, final | `attendance.model/policy/service/io/util/exception`, utility class with private constructor |
| Date-Time API | `LocalDate` in the report header |

## Project structure

```
attendance-monitor/
├── data/
│   ├── sample_attendance.csv      8 students x 5 subjects (40 records)
│   └── invalid_attendance.csv     deliberately bad rows for the exception demo
├── src/attendance/
│   ├── Main.java                  console menu and --demo mode
│   ├── model/                     Person, Student, SubjectAttendance, AttendanceStatus,
│   │                              Projection, SubjectStats, ShortageEntry
│   ├── policy/                    AttendancePolicy, StandardPolicy, StrictPolicy
│   ├── service/                   AttendanceManager, ReportFormatter
│   ├── io/                        CsvImporter, ReportWriter
│   ├── util/                      AttendanceCalculator
│   └── exception/                 InvalidAttendanceException, StudentNotFoundException
└── test/attendance/TestRunner.java   19 test cases, no external libraries
```

## Class relationships

```
Person (abstract) <|-- Student 1 ---- * SubjectAttendance
AttendancePolicy (interface) <|.. StandardPolicy, StrictPolicy
AttendanceManager o-- Student (HashMap)      AttendanceManager --> AttendancePolicy
AttendanceManager --> SubjectStats, ShortageEntry
CsvImporter --> AttendanceManager             ReportFormatter --> AttendanceManager
AttendanceCalculator --> Projection
```

## Formulas

With `t` = required %, `A` = classes attended, `C` = classes conducted, `R` = classes remaining:

* Percentage = `A / C x 100`
* Classes to attend in a row: `x = ceil((t*C - 100*A) / (100 - t))`
* Classes that can be missed: `y = floor(100*A / t - C)`
* Projection: `k = ceil(t*(C+R)/100 - A)`; achievable only if `k <= R`

Example: 20 of 30 classes attended, target 75% -> `x = ceil((2250 - 2000) / 25) = 10`
(check: 30/40 = 75%).

## Build and run

Requires JDK 8 or newer. Run all commands from the project root.

```
javac -d out -sourcepath src src/attendance/Main.java
java -cp out attendance.Main --demo      # scripted demonstration
java -cp out attendance.Main             # interactive menu
```

Run the tests:

```
javac -cp out -d out test/attendance/TestRunner.java
java -cp out attendance.TestRunner
```

## CSV format

```
studentId,studentName,subject,conducted,attended
24BCE1001,Aarav Sharma,Object Oriented Prog,40,36
```

Lines starting with `#` and blank lines are ignored. A line is skipped (and reported) if it has
the wrong number of fields, non-numeric counts, attended > conducted, or duplicates an earlier
(student, subject) record.

## Limitations and future work

* Works on cumulative counts per subject; it does not store day-by-day attendance.
* Console interface only.
* Future: day-wise attendance with `LocalDate`, a Swing/JavaFX UI, and per-subject custom thresholds.
