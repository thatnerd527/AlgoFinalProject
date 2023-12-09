package Server.MaterialCalculator;

import Server.WrappedReader;
import Server.WrappedWriter;
import UI.Menu;

public class MaterialC {
    public static void HandleMaterialCalculatorUI(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String choice = new Menu()
            .withChoice("1","Material requirement calculator, for purchases")
            .withChoice("2","Material requirement calculator, for building")
            .withChoice("3","Exit")
            .makeASelection(wW, wR);

            switch (choice) {
                case "1":

                    continue;
                case "2":

                    continue;
                case "3":
                    return;
            })

        }
    }
}
