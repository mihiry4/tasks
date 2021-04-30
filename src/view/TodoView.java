package view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import controller.TodoController;
import controller.TodoDueDateInPastException;
import controller.TodoEmptyTaskNameException;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Task;
import model.TaskList;
import model.TodoModel;

/**
 * 
 * @author Kaushal Bhat, Mihir Yadav, Shreyas Khandekar, Zachary Florez
 * 
 * Class that will hold the View of the ToDo application. 
 *
 */
public class TodoView extends Application implements Observer {
	
	private static BorderPane window;
	TodoModel model;
	TodoController controller;
	StackPane bottomPane;
	private MenuBar menuBar;
	private Menu filter;
	private Circle addTaskButton, transparentCircle;
	private CheckMenuItem showCompleted;
	private MenuItem newFile, saveFile, loadFile, showAll, hideAll;
	private MenuItem name, priority, category, dueDate, dateCreated;
	private Stage myStage;
	private VBox tasksBox;
	private VBox centerWindow;
	private GridPane columnHeaders;
	private Scene scene;
	private List<CheckMenuItem> categoryCheckBoxes;
	
	private void setup(File file) {
		if (file == null) {
			model = new TodoModel();
		} else {
			model = new TodoModel(file);
		}
		
		controller = new TodoController(model);
		model.addObserver(this); 
	}
	
	public void start(Stage stage) {
		
		setup(null);
		
		this.myStage = stage;
		window       = new BorderPane();
		menuBar      = new MenuBar();
		tasksBox         = new VBox();
		centerWindow = new VBox();
		columnHeaders   = new GridPane();
		
		// Menu for files and sorting and filtering
		Menu fileMenu   = new Menu("File");
		Menu viewMenu = new Menu("View");
		Menu sortBy     = new Menu("Sort By");
		filter = new Menu("Filter by Category");
		
		// Menu Items for fileMenu: 
		newFile  = new MenuItem("New File");
		saveFile = new MenuItem("Save...");
		loadFile = new MenuItem("Load...");
		fileMenu.getItems().addAll(newFile, saveFile, loadFile);
		
		// Menu Items for sortBy: (Menu inside Menu) 
		name        = new MenuItem("Name");
		priority    = new MenuItem("Priority");
		category    = new MenuItem("Category");
		dueDate     = new MenuItem("Due Date");
		dateCreated = new MenuItem("Date Created");
		sortBy.getItems().addAll(name, priority, category, dueDate, dateCreated);
		
		// Menu Item for Filter: (menu inside menu)
		showAll = new MenuItem("Show All");
		hideAll = new MenuItem("Hide All");
		
		showCompleted = new CheckMenuItem("Show Completed Tasks");
		viewMenu.getItems().addAll(sortBy, filter, showCompleted);
		
		// Add both Menus to MenuBar
		menuBar.getMenus().addAll(fileMenu, viewMenu);
		
		// Method called to set all event handlers for all
		// different drop down menus. 
		setEventHandlers();
			
		// Function called to create button on bottom of window
		// to add a task. 
		createAddTaskButton();
		
		// Setup the columns headers and do the initial setup
		createColumnHeaders();
		
		// Scrollbar
	    ScrollPane scrollPane = new ScrollPane(tasksBox);
	    scrollPane.setFitToHeight(true);
	    scrollPane.setFitToWidth(true);
		
		
		// Set Center Window (VBox) Items
		centerWindow.getChildren().addAll(columnHeaders, scrollPane);
		centerWindow.setSpacing(14.0);
		
		// set window layer
		window.setTop(menuBar);
		window.setBottom(bottomPane);
		window.setCenter(centerWindow);
		window.setAlignment(bottomPane, Pos.CENTER);
		
		// Set new scence and display. 
		scene = new Scene(window, 600, 600);
		stage.setScene(scene);
		stage.setTitle("ToDo Application");
		stage.show();
	}
	
	/**
	 * Function to create the top method headers displayed in
	 * the view.
	 * 
	 */
	private void createColumnHeaders() {
		
		Text completed = new Text(" Completed ");
		Text name = new Text(" Task ");
		Text priority = new Text(" Priority ");
		Text category    = new Text(" Category ");
		Text date = new Text(" Date ");
		Text reorder    = new Text(" Reorder ");
		
		name       .setFont(new Font(15));
		priority    .setFont(new Font(15));
		completed.setFont(new Font(15));
		category   .setFont(new Font(15));
		date.setFont(new Font(15));
		reorder   .setFont(new Font(15));
		
		
		columnHeaders.add(completed, 0, 0);
		columnHeaders.add(name, 1, 0);
		columnHeaders.add(priority, 2, 0);
		columnHeaders.add(category, 3, 0);
		columnHeaders.add(date,  4, 0);
		columnHeaders.add(reorder, 5, 0);
		
		addColumnConstraints(columnHeaders);
		
		columnHeaders.setGridLinesVisible(true);
		
		
		columnHeaders.setStyle("-fx-background-color:#C0C0C0");
	}
	
	private void addColumnConstraints(GridPane gridPane) {
		final int[] COLUMN_CONSTRAINTS_PERCENTS = new int[] {16, 20, 16, 16, 16, 16};  
		
		for (int i = 0; i < 6; i++) {
			ColumnConstraints tmpColumnConstraint = new ColumnConstraints();
			tmpColumnConstraint.setPercentWidth(COLUMN_CONSTRAINTS_PERCENTS[i]);
			gridPane.getColumnConstraints().add(tmpColumnConstraint);
		}
	}

	/*
	 * adds task rows to UI
	 */
	private GridPane addTaskRow(Task task) {
		GridPane taskRow = new GridPane();
		
		// creating checkbox for completion
        CheckBox completedCB = new CheckBox();
		if(task.isCompleted())
			completedCB.setSelected(true);
		completedCB.setPadding(new Insets(2, 2, 2, 2));
		
		//Setting action to check boxes
	    completedCB.selectedProperty().addListener(
	      (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
	         //System.out.println("Value of checkbox changed from " + old_val + " to " +new_val );
	    	 try {
				controller.modifyTask(task, task.getName(), task.getDescription(), task.getPriority(),
						 task.getCategory(), new_val, task.getDateDue(), task.getLocation());
			} catch (TodoDueDateInPastException e1) {
				showAlert(e1.toString());
			} catch (TodoEmptyTaskNameException e1) {
				showAlert(e1.toString());
			}
	      });
		// getting task name and converting it to task
		String name = task.getName();
		Text nameText = new Text(name);
		nameText.setOnMouseClicked(e -> {
			createPopUp(task, task.getName(), task.getDescription(), task.getPriority(), task.getCategory(), task.isCompleted(),
					task.getDateDue(), task.getLocation());
        });
		
		
		// getting priority and converting to text
		String priority = String.valueOf(task.getPriority());
		Text priorityText = new Text(priority);
		
		// getting category and converting it to text
		String category = task.getCategory();
		Text categoryText = new Text(category);
		
		// getting date and converting it to text
		String date = String.valueOf(task.getDateDue().getDate());
		String month =  String.valueOf(task.getDateDue().getMonth());
		String year =  String.valueOf(task.getDateDue().getYear() + 1900);
		String finDate = month + "/" + date  + "/" + year;
		Text dateText = new Text(finDate);

		// new hbox for reorder buttons and up/down buttons
		HBox hb = new HBox();
		hb.setPadding(new Insets(5, 5, 5, 5));
		Button up = new Button("up");
		Button down = new Button("down");
		up.setStyle("-fx-background-color: #008300; -fx-text-fill: white");
		down.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white");
		hb.getChildren().addAll(up,down);
		
		//reorder button actions
		up.setOnAction((event)->{
			controller.manualReorder(task, -1);
		});
		down.setOnAction((event)->{
			controller.manualReorder(task, 1);
		});
		
		// changing fonts
		nameText.setFont(new Font(15));
		priorityText.setFont(new Font(15));
		categoryText.setFont(new Font(15));
		dateText.setFont(new Font(15));
		
		// adding all items to task row
		taskRow.add(completedCB, 0, 0);
		taskRow.add(nameText, 1, 0);
		taskRow.add(priorityText, 2, 0);
		taskRow.add(categoryText, 3, 0);
		taskRow.add(dateText,  4, 0);
		taskRow.add(hb, 5, 0);
		
		// adding column constraints 
		addColumnConstraints(taskRow);
		
		taskRow.setGridLinesVisible(true);
		return taskRow;
		
		
	}
	
	
	
	
	private void showAlert(String string) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle("Error!");
		a.setContentText(string);
		a.setHeaderText("Invalid Input");
		a.showAndWait();
		
	}

	/**
	 * Function called from start() to set all of the event handlers 
	 * for all the different drop down menus. 
	 * 
	 * no return.
	 */
	private void setEventHandlers() {
		
		
		name.setOnAction((event) -> {
			controller.sortByName();
		});
		
		
		priority.setOnAction((event) -> {
			controller.sortByPriority();
		});
		
		
		category.setOnAction((event) -> {
			controller.sortByCategory();
		});
		
		
		dueDate.setOnAction((event) -> {
			controller.sortByDateDue();
		});
		
		
		dateCreated.setOnAction((event) -> {
			controller.sortByDateCreated();
		});
		
		
		newFile.setOnAction((event) -> {
			setup(null);
			controller.manualNotify();
			// TODO: Maybe in the future, add a pop-up to save the current file
			// before in the future. Also do that on close.
		});
		
		
		saveFile.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
		    fileChooser.setTitle("Save");
		    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.dat"));
		    //Adding action on the menu item
	        //Opening a dialog box
		    File file = fileChooser.showSaveDialog(myStage);
	        if (file != null) {
	        	String fileName = file.getName();
                try {
					FileOutputStream fout = new FileOutputStream(fileName);
					ObjectOutputStream oos = new ObjectOutputStream(fout);
					controller.writeToFile(oos);
				} catch (IOException e) {
					// catch block
					Alert a = new Alert(Alert.AlertType.INFORMATION);
					a.setTitle("Message");
					a.setContentText("YIKES!");
					a.setHeaderText("Unable to Save game");
					a.showAndWait();
				}
	        }
		});
		
		
		
		loadFile.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Resource File");
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Text Files", "*.dat"));
			File selectedFile = fileChooser.showOpenDialog(this.myStage);
			if (selectedFile != null) {
				setup(selectedFile);
				controller.manualNotify();
			}
		});
		
		
		showCompleted.setOnAction((event) -> {
			controller.updateShowCompleted(showCompleted.isSelected());
		});
		
		showAll.setOnAction((event) -> {
			for (CheckMenuItem categoryCheckBox : categoryCheckBoxes) {
				
				if (!categoryCheckBox.isSelected()) {
					categoryCheckBox.setSelected(true);
					updateShowCategory(categoryCheckBox);
				}
			}
			
		});
		
		hideAll.setOnAction((event) -> {
			for (CheckMenuItem categoryCheckBox : categoryCheckBoxes) {
				
				if (categoryCheckBox.isSelected()) {
					categoryCheckBox.setSelected(false);
					updateShowCategory(categoryCheckBox);
				}
			}
			
		});
	}
	
	
	/**
	 * Function called from start to create the Circle on bottom 
	 * of window and to set an event handler when circle is pressed. 
	 * 
	 * No return.
	 */
	private void createAddTaskButton() {
		bottomPane = new StackPane();
		bottomPane.setPadding(new Insets(10));
		
		// Plus Sign above Circle
		Text plusSign = new Text("+");
		plusSign.setFill(Color.WHITE);
		plusSign.setFont(new Font(40));
		plusSign.setBoundsType(TextBoundsType.VISUAL);
		
		// Circle
		double radius = 10 + getWidth(plusSign)/2;
		addTaskButton = new Circle();
		addTaskButton.setRadius(radius);
		addTaskButton.setFill(Color.GREEN);
		
		// Transparent Circle
		transparentCircle = new Circle();
		transparentCircle.setRadius(radius);
		transparentCircle.setFill(Color.TRANSPARENT);
		
		// Adding to stack pane
		bottomPane.getChildren().addAll(addTaskButton, plusSign, transparentCircle);
		
		transparentCircle.setOnMouseClicked((event) -> {
			addNewTask();
		});
	}
	
	private double getWidth(Text text) {
		new Scene(new Group(text));
		text.applyCss();
		return text.getLayoutBounds().getWidth();
	}
	
	
	/**
	 * Function called when addTask Circle is pressed on
	 * to get users input to create and add a new task. 
	 * 
	 * no return.
	 */
	private void addNewTask() {
		createPopUp(null, "", "", 1, "", false, new Date(),"");
	}
	
	/**
	 * Function called when addTask Circle is pressed on
	 * to get users input to customize an added new task. 
	 * 
	 * no return.
	 */
	private void modifyTask(Task task) {
		createPopUp(task, task.getName(), task.getDescription(), task.getPriority(), 
				task.getCategory(), task.isCompleted(), task.getDateDue(), task.getLocation());
	}
	
	
	private void createPopUp(Task task, String taskName, String description, int priority, String category, boolean completed,
			Date dateDue, String location) {
		boolean isNew = false;
		if(taskName.isEmpty()) {
			isNew = true;
		}
		
		// setting up a new stage
		final Stage dialog = new Stage();
		dialog.setResizable(false);
		dialog.setTitle("Add Task");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(myStage);
        HBox dialogHbox = new HBox(60);
        VBox primaryDetailsVbox = new VBox(20);
        VBox secondaryDetailsVbox = new VBox(20);
        primaryDetailsVbox.setPadding(new Insets(8, 8, 8, 8));
        
        //
        // adds name and description fields and completed checkbox
        //
        TextField nameField = new TextField();
        nameField.setText(taskName);
        TextArea DescriptionField = new TextArea();
        DescriptionField.setText(description);
        nameField.setPromptText("Name");
        nameField.setPrefWidth(180);
        nameField.setMaxWidth(200);
        DescriptionField.setPromptText("Description");
        DescriptionField.setPrefWidth(180);
        DescriptionField.setMaxWidth(200);
        DescriptionField.setPrefHeight(100);
        DescriptionField.setMaxHeight(100);
        CheckBox cb = new CheckBox("Completed");
        cb.setSelected(completed);
        primaryDetailsVbox.getChildren().addAll(nameField,DescriptionField,cb);

        //
        // adds category, priority, due date, Location and submit button elements
        //
        secondaryDetailsVbox.setPadding(new Insets(8, 8, 8, 8));
        Rectangle backGColor = new Rectangle(270,600);
        backGColor.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        StackPane stackPane = new StackPane();
        
        // category
        Text categoryText = new Text("Category");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        categoryField.setPrefWidth(160);
        categoryField.setMaxWidth(180);
        categoryField.setText(category);
        Text priorityText = new Text("Priority");
        final ComboBox<Integer> priorityComboBox = new ComboBox<Integer>();
        priorityComboBox.getItems().addAll(
            1,
            2,
            3,
            4,
            5
        );   
        priorityComboBox.setValue(priority);
        
        // dates
        Text dueDateText = new Text("Due Date");
        DatePicker datePicker = new DatePicker();
        // disables past dates
        datePicker.setValue(dateDue.toInstant()
        	      .atZone(ZoneId.systemDefault())
        	      .toLocalDate());
        datePicker.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0 );
            }
        });
        
        // location
        Text locationHeading = new Text("Location");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        locationField.setPrefWidth(160);
        locationField.setMaxWidth(180);
        locationField.setText(location);
        
        // hbox for buttons
        HBox buttonsHbox = new HBox();
        Button submitDetailsButton = new Button("Submit");  
        Button deleteTaskButton = new Button("Delete");
        buttonsHbox.getChildren().addAll(submitDetailsButton,deleteTaskButton);
        buttonsHbox.setSpacing(5);
        
        // printing out all the fields on submit button
        // TODO: tell controller to make new task out of given information
        submitDetailsButton.setOnAction((event)->{
        	String nameOutput = nameField.getText();
        	String descriptionOutput = DescriptionField.getText();
        	boolean isCompletedOutput = cb.isSelected(); // return a boolean
        	String categoryOutput = categoryField.getText();
        	int priorityOutput = priorityComboBox.getValue();
        	LocalDate localDateOutput = datePicker.getValue(); // returns in format 2021-04-28
        	String locationOutput = locationField.getText();
        	ZoneId defaultZoneId = ZoneId.systemDefault();
        	Date dateOutput = Date.from(localDateOutput.atStartOfDay(defaultZoneId).toInstant());
        	// Put the dueDate time to end of today (23:59:59)
        	dateOutput.setTime(dateOutput.getTime()+86399999);
        	Task temp = null;
        	if(task == null) { 
				try {
					temp = controller.createNewTask(nameOutput, descriptionOutput, priorityOutput, categoryOutput, isCompletedOutput, dateOutput, locationOutput);
				} catch (TodoDueDateInPastException e) {
					showAlert(e.toString());
				} catch (TodoEmptyTaskNameException e) {
					showAlert(e.toString());
				}
        	} else {
				try {
					controller.modifyTask(task, nameOutput, descriptionOutput, priorityOutput, categoryOutput, isCompletedOutput, dateOutput, locationOutput);
				} catch (TodoDueDateInPastException e) {
					showAlert(e.toString());
				} catch (TodoEmptyTaskNameException e) {
					showAlert(e.toString());
				}
			}
        	dialog.close();
        	
        });
        
        // delete task on clicking delte button
        deleteTaskButton.setOnAction((event)->{
        	controller.removeTask(task);
        	dialog.close();
        });
        
        // adding to children of Boxes wherever needed
        submitDetailsButton.setStyle("-fx-background-color: #008300; -fx-text-fill: white");
        deleteTaskButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white");

        secondaryDetailsVbox.getChildren().addAll(categoryText, categoryField, 
        		priorityText, priorityComboBox, dueDateText, 
        		datePicker,locationHeading, locationField, buttonsHbox);
        stackPane.getChildren().addAll(backGColor,secondaryDetailsVbox);

        dialogHbox.getChildren().addAll(primaryDetailsVbox,stackPane);
        Scene dialogScene = new Scene(dialogHbox, 500, 400);
        dialog.setScene(dialogScene);
        dialog.show();
	}
	
	private void updateShowCategory(CheckMenuItem tmpCheckMenuItem) {
		controller.updateShowCategory(tmpCheckMenuItem.getText(), tmpCheckMenuItem.isSelected());
	}

	@Override
	public void update(Observable o, Object arg) {
		
		tasksBox.getChildren().clear();
		TaskList tList = (TaskList) arg;
		List<Task> taskList = tList.getTaskList();
		Map<String, Boolean> categories = tList.getCategories();
		for(Task t: taskList) {
			if(t.isCompleted() && !tList.getShowCompleted()) {
				continue;
			}
			
			if (categories.get(t.getCategory())) {
			
				GridPane tempGP = addTaskRow(t);
				tasksBox.getChildren().add(tempGP);
				
			}
		}
		
		//Setting up category filter
		categoryCheckBoxes = new ArrayList<CheckMenuItem>();
		
		filter.getItems().clear();
		
		for (Map.Entry<String, Boolean> entry : categories.entrySet()) {
			String category = entry.getKey();
			boolean showing = entry.getValue();
			
			CheckMenuItem tmpCheckMenuItem = new CheckMenuItem(category);
			tmpCheckMenuItem.setSelected(showing);
			tmpCheckMenuItem.setOnAction((event) -> {
				updateShowCategory(tmpCheckMenuItem);
			});
			categoryCheckBoxes.add(tmpCheckMenuItem);
			filter.getItems().add(tmpCheckMenuItem);
			
			
			
		}
		filter.getItems().add(showAll);
		filter.getItems().add(hideAll);
		
		
	}
}
