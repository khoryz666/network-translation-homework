import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;



public class PracTest extends Application { 
//	// For way 2
//	private Text text = new Text (60, 80, "JAVA Programming");
    @Override 
    public void start(Stage primaryStage) { 
		
      // Refer to (2)
    	Text text = new Text(60, 80, "JAVA Programming");
	    text.setFill(Color.BLACK);
	    text.setFont(Font.font("Courier", 20));
	    
	    Pane paneForText = new Pane(); 
	    paneForText.getChildren().add(text); 
    
	    RadioButton rbRed = new RadioButton("Red"); 
	    RadioButton rbBlue = new RadioButton("Blue");  
  
	    ToggleGroup group = new ToggleGroup(); 
	    rbRed.setToggleGroup(group);
	    rbBlue.setToggleGroup(group);
    
 	    HBox paneForRadioButtons = new HBox(5); 
	    paneForRadioButtons.getChildren().addAll(rbRed, rbBlue);
	    paneForRadioButtons.setAlignment(Pos.CENTER);

	// Refer to (3)
	    rbRed.setOnAction(e ->{
	    	text.setFill(Color.RED);
	    });
	    rbBlue.setOnAction(e ->{
	    	text.setFill(Color.BLUE);
	    });
	  
	    Button btLeft = new Button("Left"); 
	    Button btRight = new Button("Right");	    
	    HBox paneForButtons = new HBox(5);
	    paneForButtons.getChildren().addAll(btLeft, btRight);
	    paneForButtons.setAlignment(Pos.CENTER);
	    
	// Refer to (4)
	    // Way 1: Lambda Expression
	    btRight.setOnAction(e -> {
	    	text.setX(text.getX() + 2);
	    });
	    btLeft.setOnAction(e ->{
	    	text.setX(text.getX() - 2);
	    });
	    
	    // Way 2: Inner Class
	    btRight.setOnAction(new BtnRight());
	    btLeft.setOnAction(new BtnLeft());
	    
	    // Way 3: Anonymous Inner Class
	    btRight.setOnAction(new EventHandler<ActionEvent>(){
	    	@Override
	    	public void handle(ActionEvent e) {
	    		text.setX(text.getX() + 2);
	    	}
	    });
	    btLeft.setOnAction(new EventHandler<ActionEvent>(){
	    	@Override
	    	public void handle(ActionEvent e) {
	    		text.setX(text.getX() - 2);
	    	}
	    });
	    
	    BorderPane borderPane = new BorderPane(); 
	    BorderPane.setMargin(paneForRadioButtons, new Insets(10));
	    BorderPane.setMargin(paneForButtons, new Insets(10));
	    borderPane.setTop(paneForRadioButtons); 
	    borderPane.setCenter(paneForText); 
	    borderPane.setBottom(paneForButtons); 
	    
	// Refer to (5)
	    Scene scene = new Scene(borderPane, 300, 200);
	    primaryStage.setTitle("Practical Test");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	    
	}
	
    // Way 2
    class BtnRight implements EventHandler<ActionEvent>{
    	@Override
    	public void handle(ActionEvent e) {
    		text.setX(text.getX() + 2);
    	}
    	
    }
    
    class BtnLeft implements EventHandler<ActionEvent>{
    	@Override
    	public void handle(ActionEvent e) {
    		text.setX(text.getX() - 2);
    	}
    	
    }	
    
    //Refer to (6)
    public static void main(String[] args) {
    	launch(args);
    }
    


}
