# C4 markdown

## Context diagram (level 1)

This diagram deliberately does not show any internal structure, technologies or the report content, only who uses the system and which external systems it talks to.
It serves QA-3 because the POS systems and their SFTP uploads are the source of the link failures, and QA-5 because the branch manager and the analyst are separate roles with different access rights.
Open question for the client: should the finance director also be able to open the report, or is the e-mail alone enough?

## Container diagram (level 2)

This diagram deliberately does not show packages, classes, the report formats in detail, or user login and accounts, which come in later labs.
The report batch serves QA-1 (3 M rows in 60 s or less), and the web report page serves QA-5 (a manager sees only their own branch).
Open question for the client: do branch managers get the report on the same morning as head office, or may they receive it later?
