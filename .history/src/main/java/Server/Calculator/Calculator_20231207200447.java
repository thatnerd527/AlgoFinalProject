package Server.Calculator;

import java.util.HashMap;

import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Menu;

class InternalCalculator {

}

public class Calculator {

    private static HashMap<String, InternalCalculator> Calculator = new HashMap<>();

    public static void HandleCalculator(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String action = new Menu()
                    .withTitle("Calculator")
                    .withChoice("1", "Make a new calculator")
                    .withChoice("2", "Remove a calculator")
                    .withChoice("3", "View a calculator")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);
            switch (action) {
                case "1":
                    // 1. Take input from a user
                    HashMap<String, String> result = new InputForm(wR, wW)
                            .withField("Name", true).withTitle("Calculator").receiveInput();
                    // 2. Make a new internal calculator
                    Calculator.put(result.get("Name"), new InternalCalculator());
                    // 3. Put inside a hashmap by using the "key" from a user

                    // IF calculator exists, do not make a new calculator
                    if()
                    continue;
                case "2":
                    HashMap<String, String> delete = new InputForm(wR, wW)
                            .withField("Name", true).withTitle("Calculator").receiveInput();
                    if (!Calculator.containsKey(delete.get("Name"))) {
                        wW.write("Calculator ini tidak ada\n");
                        continue;
                    }
                    Calculator.remove(delete.get("Name"));
                    wW.write("Calculator ini berhasil untuk di remove \n");

                    continue;

                case "3": 
                    
                    continue;

                case "b":
                    break;
            }

            break;
        }
    }
}
