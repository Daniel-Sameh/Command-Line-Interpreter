# Command Line Interpreter (CLI)

A simple and extensible Command Line Interpreter (CLI) that allows users to execute various commands for file and directory management, as well as to filter output through a set of pipeline commands. This project serves as a demonstration of core principles of command-line interfaces and can be used as a basis for more complex CLI applications.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Available Commands](#available-commands)
- [Pipeline Filters](#pipeline-filters)
- [Contributors](#contributors)
- [Contributing](#contributing)

## Features

- Navigate the filesystem using commands like `cd`, `ls`, and `pwd`.
- Manage files and directories with commands such as `mkdir`, `rmdir`, `touch`, `mv`, and `rm`.
- View file contents using the `cat` command.
- Use pipeline commands (`less`, `more`, `uniq`, and `grep`) to filter output.
- Comprehensive help command that displays usage information for all available commands.

## Installation

To use this CLI, clone the repository and build the project using Maven:

```bash
git clone https://github.com/Daniel-Sameh/Command-Line-Interpreter.git
mvn install
```
Ensure you have [Java JDK](https://www.oracle.com/java/technologies/downloads/#java11?er=221886) installed on your machine.

## Available Commands
| Command | Description |
|---------|-------------|
| `pwd`   | Displays the current working directory.|
| `cd`   | Changes the current directory to `<directory>`.|
| `ls`   | Lists all files and directories in the current directory.|
| `mkdir`   | Creates a new directory named `<directory>`.|
| `rmdir`   | Removes the specified directory if it is empty.|
| `touch`   | Creates a new empty file named `<filename>`.|
| `mv`   | Moves or renames a file or directory.|
| `rm`   | Deletes the specified file.|
| `cat`   | Displays the contents of the specified file.|
| `help`   | Displays a help message with a list of available commands.|
| `>`   | Redirects the output of a command to a specified file. Creates the file if it doesn’t exist; if it does, the file’s original content is replaced. Example: `ls > test.txt`|
| `>>`   | Appends the output of a command to a specified file, creating it if it doesn’t exist. Example: `ls >> test.txt`|

## Pipeline Filters

| Filter | Description |
|---------|-------------|
| `more`   | Displays output one chunk at a time.|
| `less`   | Displays output one chunk at a time with the ability to go up and down one line.|
| `uniq`   | Filters out repeated lines.|
| `grep`   | Searches for a specified pattern in the output.|

## Contributors
- Daniel Sameh
- Sherif Youssef

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any enhancements, bug fixes, or feature requests.

- Fork the repository.
- Create your feature branch (`git checkout -b feature/YourFeature`).
- Commit your changes (`git commit -m 'Add some feature'`).
- Push to the branch (`git push origin feature/YourFeature`).
- Open a pull request.
