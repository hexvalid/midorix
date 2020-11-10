package com.midorix.ui;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    public static boolean PRINT_UI = false;
    public static boolean PRINT_CONSOLE = false;

    final private static int MAX_LOGS = 256;
    final private static Font defaultFont = Font.font("Roboto Mono", 12);
    final private static Font boldFont = Font.font("Roboto Mono", FontWeight.BOLD, 12);
    final private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS ");

    public enum t {SCS, DBG, INF, WRN, ERR, CRI, UNK}

    public static void Print(t type, String s) {
        if (PRINT_UI) {
            Platform.runLater(() -> {
                TextFlow tf = new TextFlow();
                Text t = new Text();
                t.setFont(defaultFont);
                t.setText(s);
                Text t0 = new Text();
                t0.setFont(boldFont);
                switch (type) {
                    case SCS:
                        t0.setStyle("-fx-fill: #4F8A10;");
                        t0.setText("SCS: ");
                        break;
                    case DBG:
                        t0.setStyle("-fx-fill: #007476;");
                        t0.setText("DBG: ");
                        break;
                    case INF:
                        t0.setStyle("-fx-fill: #00529B;");
                        t0.setText("INF: ");
                        break;
                    case WRN:
                        t.setStyle("-fx-fill: #9F6000;");
                        t0.setStyle("-fx-fill: #9F6000;");
                        t0.setText("WRN: ");
                        break;
                    case ERR:
                        t.setStyle("-fx-fill: #D8000C;");
                        t0.setStyle("-fx-fill: #D8000C;");
                        t0.setText("ERR: ");
                        break;
                    case CRI:
                        t0.setStyle("-fx-fill: #FF0000;");
                        t0.setText("CRI: ");
                        t.setStyle("-fx-fill: #FF0000;");
                        break;
                    case UNK:
                        t0.setStyle("-fx-fill: #560094;");
                        t0.setText("UNK: ");
                        break;
                }
                tf.getChildren().add(t0);
                Text t1 = new Text();
                t1.setFont(defaultFont);
                t1.setStyle("-fx-fill: #666666;"); //setFill?
                t1.setText(dateFormat.format(new Date()));
                tf.getChildren().add(t1);
                tf.getChildren().add(t);
                tf.setPrefSize(420, Region.USE_COMPUTED_SIZE);
                if (Dash.Controller._logView.getItems().size() > MAX_LOGS) {
                    Dash.Controller._logView.getItems().remove(0);
                }
                Dash.Controller._logView.getItems().add(tf);
                Dash.Controller._logView.scrollTo(Dash.Controller._logView.getItems().size() - 1);
            });
        }
        if (PRINT_CONSOLE) {
            StringBuilder log = new StringBuilder();
            switch (type) {
                case SCS:
                    log.append("SCS: ");
                    break;
                case DBG:
                    log.append("DBG: ");
                    break;
                case INF:
                    log.append("INF: ");
                    break;
                case WRN:
                    log.append("WRN: ");
                    break;
                case ERR:
                    log.append("ERR: ");
                    break;
                case CRI:
                    log.append("CRI: ");
                    break;
                case UNK:
                    log.append("UNK: ");
                    break;
            }
            log.append(dateFormat.format(new Date()));
            log.append(s);
            System.out.println(log.toString());
        }
    }

    static void TestLog() {
        Print(t.SCS, "Task is successfully completed!");
        Print(t.DBG, "Debug info here: 001001011101011011");
        Print(t.INF, "This is a info message");
        Print(t.WRN, "Warning! Something is a wrong! (Maybe not?)");
        Print(t.ERR, "This is example error message!");
        Print(t.CRI, "This is so critical error!!!");
        Print(t.UNK, "Unknown thing happen. WTF?");
        Print(t.INF, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
    }
}