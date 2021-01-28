import java.util.ArrayList;
import java.util.Collection;

public class TaskList {
    private ArrayList<Task> taskList;

    public TaskList(Collection<Task> taskCollection) {
        this.taskList = new ArrayList<>(taskCollection);
    }

    /**
     * Provides UI object the representation
            of all the task in the taskList for printing
     * @param ui (Ui object to do the actual printing)
     */
    public void printAllTask(Ui ui) {
        for (int i = 0; i < taskList.size(); i++) {
            ui.printTask(i + 1 + ".", taskList.get(i).toString());
        }
    }

    /**
     * Returns boolean that represent option being valid / invalid
     * @param ui UI object for printing error message
     * @param option Input for checking
     * @return
     */
    public boolean checkValidOption(Ui ui, int option) {
        boolean result = option < 0 || option >= this.taskList.size();
        if (result) {
            ui.showError("Invalid task Option");
        }
        return result;
    }

    /**
     * Marks a specific task based on the number given as done
     * @param ui Ui object to inform user if the task is already done
     * @param option task number entered by the user
     */
    public void markAsDone(Ui ui, int option) {
        if(!checkValidOption(ui, option)) {
            Task task = taskList.get(option);
            if (!task.markAsDone()) {
                ui.showError("Task is already marked done");
            } else {
                ui.showSuccessMarkDone(task.toString(), taskList.size());
            }
        }
    }

    /**
     * Deletes a task in the taskList based on the number given
     * @param ui Ui object to show message upon successfully deleting
     * @param option task number entered by the user
     */
    public void deleteTask(Ui ui, int option) {
        if(!checkValidOption(ui, option)) {
            Task t = taskList.remove(option);
            ui.showSuccessDeleteTask(t.toString(), taskList.size());
        }
    }

    /**
     * Adds a task into the taskList
     * @param ui UI object to show message upon successfully adding
     * @param task Task object to be adding into the taskList
     */
    public void addTask(Ui ui, Task task) {
        taskList.add(task);
        ui.showSuccessAddTask(task.toString(), taskList.size());
    }

    /**
     * Returns the TaskList
     * @return ArrayList<Task> taskList
     */
    public ArrayList<Task> getTaskList() {
        return taskList;
    }
}
