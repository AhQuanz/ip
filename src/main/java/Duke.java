import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class Duke {
    private static String fileName;
    private static ArrayList<Task> taskList = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String task, keyword;
        LocalDateTime deadline;
        int firstSpace, firstSlash, option;

        //Loading from file
        System.out.println("Please enter file name : ");
        fileName = scanner.nextLine();
        loadTaskList();

        //Greet User
        printGreetings();
        String command = scanner.nextLine();
        while (!command.equalsIgnoreCase("bye")) {
            boolean change = true;
            printLine();

            firstSpace = command.indexOf(" ");
            keyword = firstSpace == -1 ? command : command.substring(0, firstSpace).toLowerCase();
            firstSlash = command.indexOf("/");

            try {
                switch (keyword) {
                case "list":
                    //Display all task added
                    listTasks();
                    change = false;
                    break;
                case "done":
                    // -1 as ArrayList starts from 0 , user input starts from 1
                    option = getChoice(command, firstSpace);
                    //Mark task of choice as done
                    completeTask(option);
                    break;
                case "delete":
                    option = getChoice(command, firstSpace);
                    deleteTask(option);
                    break;
                case "todo":
                    task = retrieveTask(command, firstSpace, command.length());
                    addTask(new Todo(task));
                    break;
                case "deadline":
                    task = retrieveTask(command, firstSpace, firstSlash);
                    deadline = retrieveDeadline(command, firstSlash);
                    addTask(new Deadline(task, deadline));
                    break;
                case "event":
                    task = retrieveTask(command, firstSpace, firstSlash);
                    LocalDateTime[] deadlines = retrieveEventDeadline(command, firstSlash);
                    addTask(new Event(task,deadlines[0], deadlines[1]));
                    break;
                default:
                    System.out.println("OOPS!!! I`m sorry. but i don`t know what that means :-(");
                    break;
                }
                if (change) {
                    saveTaskList();
                }
            } catch (DukeException e) {
                System.out.printf("OOPS!!! %s %s cannot be empty.\n", e.getMessage(), keyword);
            } catch (DukeDeadlineException e) {
                System.out.println(e.getMessage());
            }
            printLine();
            command = scanner.nextLine();
        }
        //Exits the program
        printLine();
        System.out.println("Bye. Hope to see you again soon!");
        printLine();
    }
    public static void loadTaskList() {
        try {
            File f = new File(fileName);
            f.createNewFile();
            Scanner s = new Scanner(f);
            while (s.hasNext()) {
                String strTask = s.nextLine();
                String[] taskArr = strTask.split(",");
                Task task = null;
                switch (taskArr[0]) {
                case "T":
                    task = new Todo(taskArr[2]);
                    break;
                case "E":
                    String[] deadlineArr = { taskArr[3] , taskArr[4]};
                    LocalDateTime[] deadlines = new LocalDateTime[2];
                    for(int i = 0 ; i < deadlineArr.length; i++) {
                        deadlines[i] = parseDate(deadlineArr[i]);
                    }
                    task = new Event(taskArr[2], deadlines[0], deadlines[1]);
                    break;
                case "D":
                    LocalDateTime deadline = parseDate(taskArr[3]);
                    task = new Deadline(taskArr[2], deadline);
                }
                if (taskArr[1] == "1") {
                    task.markAsDone();
                }
                taskList.add(task);
            }
        } catch (IOException e) {
            System.out.println("Unable to create file");
        }
    }

    public static void saveTaskList() {
        try{
            FileWriter fw = new FileWriter(fileName);
            for(Task t : taskList) {
                fw.write(t.save());
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("File cannot be opened");
        }
    }

    public static void deleteTask(int option) {
        Task removed_task = taskList.get(option);
        taskList.remove(option);
        System.out.println("Noted. I`ve removed this task:");
        printTask("", removed_task);
        printTaskNum();
    }

    public static int getChoice(String command, int firstSpace) {
        return Integer.parseInt(command.substring(firstSpace + 1)) - 1;
    }

    public static LocalDateTime retrieveDeadline(String command, int start) throws DukeDeadlineException{
        int nextSpace = getNextSpace(command,start);
        String deadline = command.substring(nextSpace+1);
        try {
            return parseDate(deadline);
        } catch (DateTimeParseException e) {
            throw new DukeDeadlineException("Format of the deadline of a deadline task should be (Year-Month-Day time (24 hours))");
        }
    }

    public static int getNextSpace(String command, int start) {
        return command.indexOf(" ",start);
    }

    public static LocalDateTime parseDate(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-M-d Hmm"));
    }

    public static LocalDateTime[] retrieveEventDeadline(String command, int start) throws DukeDeadlineException {
        int firstSpace = getNextSpace(command,start) + 1;
        int nextSpace = getNextSpace(command,firstSpace) + 1;
        String date = command.substring(firstSpace, nextSpace);
        String times = command.substring(nextSpace);
        String[] timeArr = times.split("-");
        if(timeArr.length != 2) {
            throw new DukeDeadlineException("Format of the deadline of a Event task should be (Year-Month-Day Time(24 hours)-Time(24 hours)");
        } else {
            LocalDateTime[] deadlineArr = new LocalDateTime[2];
            for (int i = 0; i < deadlineArr.length; i++) {
                StringBuilder str = new StringBuilder(date);
                str.append(timeArr[i]);
                deadlineArr[i] = parseDate(str.toString());
            }
            return deadlineArr;
        }
    }

    public static String retrieveTask(String command, int start, int end) throws DukeException {
        // RetrieveTask starts from the space after the keyword
        // RetrieveTask ends either end of the string or before / (deadline and event)
        // E.g <keyword> <description> (datetime/time)
        // if start == -1 --> e.g todo , deadline , event
        // if end == -1 --> deadline buy book , event buy book
        if (start == -1) {
            throw new DukeException("The description of a");
        } else if (end == -1) {
            throw new DukeException("The deadline of a");
        }
        return command.substring(start, end);
    }

    public static void addTask(Task newTask) {
        taskList.add(newTask);
        System.out.println("Got it. I`ve added this task:");
        printTask("", newTask);
        printTaskNum();
    }

    public static void printTaskNum() {
        System.out.printf("Now you have %d task in the list\n", taskList.size());
    }

    public static void completeTask(int option) {
        boolean res = false;
        String message = "Invalid option";
        // Option validation (More than 0 and not more than the number of task)
        if (option >= 0 && option < taskList.size()) {
            res = taskList.get(option).markAsDone();
            message = String.format("Task %d is already been marked as done", option + 1);
        }

        if (res) {
            System.out.println("Nice! I`ve marked this task as done:");
            printTask("", taskList.get(option));
        } else {
            System.out.println(message);
        }
    }

    public static void printTask(String numbering, Task task) {
        System.out.printf("%2s %s\n", numbering, task);
    }

    public static void listTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            printTask(i + 1 + ".", taskList.get(i));
        }
    }

    public static void printLine() {
        System.out.println("----------------------------------------------");
    }

    public static void printGreetings() {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println("Hello from\n" + logo);
        printLine();
        System.out.println("Hello! I`m Duke");
        System.out.println("How can i help you?");
        printLine();
    }
}