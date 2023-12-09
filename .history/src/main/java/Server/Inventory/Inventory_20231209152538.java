package Server.Inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;

import Material.Material;
import Material.MaterialBuilder;
import Material.MaterialComposite;
import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Menu;
import UI.Table;

public class Inventory {

    public static void HandleInventory(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String action = new Menu().withTitle("Inventory management")
                    .withChoice("1", "Show currently in stock items")
                    .withChoice("2", "Show all template materials")
                    .withChoice("3", "Add new item from template ID")
                    .withChoice("4", "Add new item type to templates")
                    .withChoice("5", "Add custom item")
                    .withChoice("6", "Remove item")
                    .withChoice("7", "Edit template item")
                    .withChoice("8", "Edit item in stock")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);

            switch (action) {
                case "1":
                    Table table = new Table();
                    table.addColumnNames(
                            "Material ID", "Name",
                            "Description",
                            "Search tags",
                            "Differentiator",
                            "Value per Quantity",
                            "Quantity",
                            "Life span start",
                            "Life span end",
                            "Is expired");
                    Server.Server.currentlystored.materialsSub().forEach(x -> {
                        table.addRow(
                                x.MaterialID().toString(),
                                x.name,
                                x.description,
                                x.tags.toString(),
                                x.differentiator,
                                ((Double) x
                                        .getValuePerQty()).toString(),
                                ((Double) x.quantity).toString(),
                                x.getLifespanStart().toString(),
                                x.getLifespanStart().plusSeconds(x.lifespanInSeconds).toString(),
                                x.isExpired().toString());
                    });
                    wW.write(Table.getPrintedTable(table, true) + "\n");

                    continue;
                case "2":
                    Table table2 = new Table();
                    table2.addColumnNames(
                            "Material ID", "Name",
                            "Description",
                            "Search tags",
                            "Differentiator",
                            "Value per Quantity",
                            "Life span");
                    Server.Server.templatematerials.materialsSub().forEach(x -> {
                        table2.addRow(
                                x.MaterialID().toString(),
                                x.name,
                                x.description,
                                x.tags.toString(),
                                x.differentiator,
                                ((Double) x
                                        .getValuePerQty()).toString(),
                                ((Double) (((Double) Double.valueOf(x.lifespanInSeconds)) / ((Double) 86400d)))
                                        .toString() + " days");
                    });
                    wW.write(Table.getPrintedTable(table2, true) + "\n");
                    continue;
                case "3":
                    while (true) {
                        HashMap<String, String> result = new InputForm(wR, wW)
                                .withField("Template ID", true)
                                .withField("Quantity", true)
                                .withTitle("Add item to stock by ID")
                                .receiveInput();
                        Optional<Material> findmat = Server.Server.templatematerials.materialsSub().stream()
                                .filter((Material mat) -> {
                                    return result.get("Template ID").equals(mat.MaterialID().toString());
                                }).findFirst();
                        if (!findmat.isPresent()) {
                            wW.write("That Template ID is not present in the database.\n");
                            break;
                        } else {
                            try {
                                Double.valueOf(result.get("Quantity"));
                            } catch (Exception e) {
                                wW.write("Invalid Quantity value.\n");
                                continue;
                            }
                            findmat.get().quantity = 0;
                            findmat.get().quantity += Double.valueOf(result.get("Quantity"));
                            Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                            if (findmat.get().lifespanInSeconds > 0) {
                                boolean specifyStart = new Menu()
                                        .withTitle("Change the item's start lifespan?")
                                        .withChoice("Y", "Yes")
                                        .withChoice("N", "No (uses now)")
                                        .makeASelection(wW, wR).equals("Y");
                                if (specifyStart) {
                                    startLifespan = InventoryFunctions.DateEntry(wR, wW);
                                }
                                findmat.get().setLifespanStart(startLifespan);

                            }

                            Server.Server.currentlystored.addMaterial(findmat.get().clone());
                            Server.Server.SaveAll();
                            wW.write("Added material.\n");
                            break;
                        }
                    }
                    continue;
                case "4":
                    Server.Server.templatematerials.addMaterial(InventoryFunctions.CreateMaterial(wR, wW));
                    Server.Server.SaveAll();

                    break;
                case "5":

                    Server.Server.currentlystored.addMaterial(InventoryFunctions.CreateMaterialWithQuantity(wR, wW));
                    Server.Server.SaveAll();

                    continue;
                case "6":
                    HashMap<String, String> result = new InputForm(wR, wW)
                            .withField("Material ID", true)
                            .withField("Quantity", true)
                            .withTitle("Remove item from stock by ID")
                            .receiveInput();
                    Optional<Material> findmat = Server.Server.currentlystored.materialsSub().stream()
                            .filter((Material mat) -> {
                                return result.get("Material ID").equals(mat.MaterialID().toString());
                            }).findFirst();
                    if (!findmat.isPresent()) {
                        wW.write("That Material ID is not present in the database.\n");
                        break;
                    }
                    try {
                        if (Double.valueOf(result.get("Quantity")) <= 0) {
                            throw new Exception();
                        }

                    } catch (Exception e) {
                        wW.write("Quantity has to be a valid positive number.");
                        continue;
                    }
                    findmat.get().quantity = Double.valueOf(result.get("Quantity"));
                    Server.Server.currentlystored.removeMaterial(findmat.get());

                    continue;
                case "7":
                    break;
                case "8":
                    break;

                case "b":
                    break;

                default:
                    continue;
            }
            break;
        }
    }
}
