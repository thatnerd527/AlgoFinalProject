package Server.MaterialCalculator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import Material.Material;
import Material.MaterialComposite;
import Server.WrappedReader;
import Server.WrappedWriter;
import Server.Calculator.CalculatorFunctions;
import Server.Inventory.InventoryFunctions;
import UI.Menu;
import UI.Table;

public class MaterialC {

    static void TablePrinter(MaterialComposite materialComposite, WrappedWriter wW) {
        Table table = new Table();
        table.addColumnNames(
                "Material ID",
                "Name",
                "Description",
                "Search tags",
                "Differentiator",
                "Value per Quantity",
                "Quantity",
                "Life span start",
                "Life span end");
                DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd");
        materialComposite.materials().stream().forEach(x -> {
            table.addRow(
                    Integer.valueOf(x.MaterialID()).toString(),
                    x.name,
                    x.description,
                    x.tags.toString(),
                    x.differentiator,
                    ((Double) x.getValuePerQty()).toString(),
                    ((Double) x.quantity).toString(),
                                x.lifespanInSeconds > 0 ? LocalDateTime
                                        .ofInstant(x.getLifespanStart(), ZoneOffset.systemDefault())
                                        .format(formatter) : "",
                                x.lifespanInSeconds > 0 ? LocalDateTime
                                        .ofInstant(x.getLifespanStart().plusSeconds(x.lifespanInSeconds),ZoneOffset.systemDefault())
                                        .format(formatter) : "");
        });
        wW.write(Table.getPrintedTable(table, true) + "\n");
    }

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
                    mat = InventoryFunctions.ModifyMaterialQuantity(wR, wW, mat);
                    MaterialComposite missing = CalculatorFunctions.getMaterialsForBuildShallow(mat,Server.Server.currentlystored);
                    wW.write("Missing materials: \n");
                    TablePrinter(missing,wW);
                    continue;
                case "2":
                    Material mat2 = InventoryFunctions.GetTemplateMaterial(wR, wW);
                    if (mat2 == null) {
                        continue;
                    }
                    mat2 = InventoryFunctions.ModifyMaterialQuantity(wR, wW, mat2);
                    MaterialComposite missing2 = CalculatorFunctions.getMaterialsForBuild(mat2,
                            Server.Server.currentlystored);
                    wW.write("All required materials: \n");
                    TablePrinter(missing2,wW);
                    continue;
                case "3":
                    return;
            }

        }
    }
}
