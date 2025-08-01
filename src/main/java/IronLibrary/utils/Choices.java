package IronLibrary.utils;

import IronLibrary.model.Author;
import IronLibrary.model.Book;
import IronLibrary.model.Issue;
import IronLibrary.model.Student;

import java.io.*;
import java.util.Scanner;

import static IronLibrary.menu.LibraryMenu.STUDENTS_FILE;
import static IronLibrary.menu.LibraryMenu.BOOKS_FILE;
import static IronLibrary.menu.LibraryMenu.ISSUES_FILE;
import static IronLibrary.utils.Prints.printIssue;
import static IronLibrary.utils.Utils.*;
import static IronLibrary.utils.Utils.doesStudentExist;

public class Choices {
    // Options
    // Option 1: Add a Book to CSV (books.csv)
    public static void addABook(Scanner scanner) {
        System.out.println("[Add a Book]");

        try {
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            System.out.print("Enter category: ");
            String category = scanner.nextLine();
            System.out.print("Enter Author name: ");
            String authorName = scanner.nextLine();
            System.out.print("Enter Author mail: ");
            String authorMail = scanner.nextLine();
            System.out.print("Enter number of Copies: ");
            int numBooks = scanner.nextInt();

            if (numBooks < 0) {
                System.out.println("The number of copies cannot be negative.");
                return;
            }

            // Create the Book object (without Author reference)
            Book book = new Book(isbn, title, category, numBooks);

            // Create the Author object referencing the Book
            Author author = new Author(authorName, authorMail, book);

            // Save the book data in the CSV file (one line per book)
            FileWriter csv = new FileWriter(BOOKS_FILE, true);

            // If the CSV file is empty, write the header
            File file = new File(BOOKS_FILE);
            if (file.length() == 0 || !file.exists()) {
                csv.write("isbn,title,category,quantity,author,email\n"); // Header CSV
            }
            // Write data to CSV (books.csv)
            csv.write(book.getIsbn() + "," +
                    book.getTitle() + "," +
                    book.getCategory() + "," +
                    book.getQuantity() + "," +
                    author.getName() + "," +
                    author.getEmail() + "\n");
            csv.close(); // Close File

            // Print confirmation of the added book and author details
            System.out.println("----------------------------------------------");
            System.out.println("\n[Book successfully created]\n"
                    + "ISBN: " + book.getIsbn() + "\n"
                    + "Title: " + book.getTitle() + "\n"
                    + "Category: " + book.getCategory() + "\n"
                    + "Author Name: " + author.getName() + "\n"
                    + "Author Mail: " + author.getEmail() + "\n"
                    + "Copies: " + book.getQuantity() + "\n");
            System.out.println("----------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error saving the book: " + e.getMessage());
        }
    }

    // Search using searchBooks()
    // Option 2 - Search a Book by Title
    public static void searchBookByTitle(Scanner scanner) {
        System.out.println("[Search by Title]");
        System.out.print("Enter a title: ");
        String title = scanner.nextLine();
        try {
            searchBooks(BOOKS_FILE, title, "title");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Option 3 - Search a Book by Category
    public static void searchBookByCategory(Scanner scanner) {
        System.out.println("[Search by Category]");
        System.out.print("Enter a category: ");
        String category = scanner.nextLine();
        try {
            searchBooks(BOOKS_FILE, category, "category");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Option 4 - Search a Book by Author
    public static void searchBookByAuthor(Scanner scanner) {
        System.out.println("[Search by Author]");
        System.out.print("Enter author name or email: ");
        String author = scanner.nextLine();
        try {
            searchBooks(BOOKS_FILE, author, "author");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Option 5 - List All Books with Author
    public static void listAllBooksWithAuthor(Scanner scanner) {
        try {
            searchBooks(BOOKS_FILE, "", "all");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Option 6: Assign a Book to a Student (Issue)
    public static void issueBookToStudent(Scanner scanner) {
        System.out.println("[Create Issue]");

        System.out.print("Enter the student's USN: ");
        String usn = scanner.nextLine();
        System.out.print("Enter the ISBN of the book to issue: ");
        String isbn = scanner.nextLine();

        try {
            // Check if Student already exist
            if (!doesStudentExist(usn)) {
                System.out.println("Student with USN " + usn + " not found. Please add the student first.");
                return;
            }

            // Extract data from CSV (students.csv)
            Student student = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(STUDENTS_FILE))) {
                reader.readLine(); // First line (Header) omitted
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 2 && data[0].trim().equals(usn.trim())) {
                        student = new Student(data[0], data[1]);
                        break;
                    }
                }
            }

            if (student == null) {
                System.out.println("Error loading student data.");
                return;
            }

            // Extract data from CSV (books.csv)
            Book book = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
                reader.readLine(); // First line (Header) omitted
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 6 && data[0].trim().equals(isbn.trim())) {
                        book = new Book(data[0], data[1], data[2], Integer.parseInt(data[3]));
                        int quantity = Integer.parseInt(data[3]);
                        System.out.println(book.quantity);
                        updateCsv(isbn, (quantity - 1));
                        System.out.println(book.quantity);
                        break;
                    }
                }
            }

            if (book == null) {
                System.out.println("Book with ISBN " + isbn + " not found.");
                return;
            }

            // Create Issue (current date and return date +7 days)
            String issueDate = java.time.LocalDate.now().toString(); // "YYYY-MM-DD"
            Issue issue = new Issue(issueDate, student, book); // Using Issue's internal logic to calc returnDate

            // Write new issue data to CSV (issues.csv)
            File issuesFile = new File(ISSUES_FILE);
            try (FileWriter csv = new FileWriter(ISSUES_FILE, true)) {
                if (issuesFile.length() == 0 || !issuesFile.exists())
                    csv.write("usn,name,isbn,bookTitle,issueDate,returnDate\n");
                csv.write(student.getUsn() + "," +
                        student.getName() + "," +
                        book.getIsbn() + "," +
                        book.getTitle() + "," +
                        issue.getIssueDate() + "," +
                        issue.getReturnDate() + "\n");
            }
            // Print confirmation of new Issue created
            printIssue(issue, book, student);
        } catch (Exception e) {
            System.out.println("Error creating new Issue: " + e.getMessage());
        }
    }

    // Option 7 - List ALL the Books rented by a Student (USN)
    public static void listBooksByUsn(Scanner scanner) {
        System.out.println("[List Books by USN]");
        System.out.print("Enter the student's USN: ");
        String usn = scanner.nextLine();

        // Read the issues CSV (issues.csv)
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(ISSUES_FILE))) {
            reader.readLine(); // First line (Header) omitted
            String line;
            System.out.println("\nBooks issued to USN: " + usn);

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6 && data[0].trim().equalsIgnoreCase(usn)) {
                    found = true;
                    String bookTitle = data[3];
                    String returnDate = data[5];

                    // Print the list of issued books (show title, returnDate)
                    System.out.println("----------------------------------------------");
                    System.out.println("Book Title: " + bookTitle);
                    System.out.println("Return Date: " + returnDate);
                    System.out.println("----------------------------------------------");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading issued books: " + e.getMessage());
        }

        if (!found) {
            System.out.println("No books found for this USN.");
        }
    }

    // [EXTRA] Option 8 - add a Student to a CSV (students.csv)
    public static void addAStudent(Scanner scanner) {
        System.out.println("[Add a Student]");

        try {
            System.out.print("Enter USN: ");
            String usn = scanner.nextLine();
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            // Check if Student already exist
            if (doesStudentExist(usn)) {
                System.out.println("Student with USN " + usn + " already exists.");
                return;
            }

            // Create the Student object
            Student student = new Student(usn, name);

            // Save the student data in the CSV file (one line per student)
            FileWriter csv = new FileWriter(STUDENTS_FILE, true);

            // If the CSV file is empty, write the header
            File file = new File(STUDENTS_FILE);
            if (file.length() == 0 || !file.exists()) {
                csv.write("usn,name\n"); // Header CSV
            }
            csv.write(student.getUsn() + "," +
                    student.getName() + "\n");
            csv.close(); // Close File

            // Print confirmation of the added student
            System.out.println("----------------------------------------------");
            System.out.println("[Student successfully created]\n"
                    + "USN: " + student.getUsn() + "\n"
                    + "Name: " + student.getName());
            System.out.println("----------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error saving the student: " + e.getMessage());
        }
    }
}
