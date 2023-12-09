package Server.MaterialCalculator;

import Material.Material;
import Server.WrappedReader;
import Server.WrappedWriter;
import Server.Calculator.CalculatorFunctions;
import Server.Inventory.InventoryFunctions;
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
                    Material mat = InventoryFunctions.GetTemplateMaterial(wR,wW);
                    if (mat == null) {
                        continue;
                    }
                    CalculatorFunctions.getMaterialsForBuildShallow(mat,)
                    continue;
                case "2":

                    continue;
                case "3":
                    return;
            })

        }
    }
}
