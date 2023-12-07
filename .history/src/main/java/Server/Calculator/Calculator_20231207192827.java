package Server.Calculator;

import java.util.HashMap;

import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Menu;

public class Calculator {
    public static void HandleCalculator(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String action = new Menu()
                    .withTitle("Calculator")
                    .withChoice("1", "Pax")
                    .withChoice("2", "")
                    .withChoice("3", "")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);



            switch (action) {
                case "1":
                    continue;
                case "b":
                    break;
            }

            break;
        }
    }
}
