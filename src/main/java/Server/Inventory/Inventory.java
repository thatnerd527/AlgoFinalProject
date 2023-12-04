package Server.Inventory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

import Material.Material;
import Material.MaterialBuilder;
import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Menu;
import UI.Table;

public class Inventory {
    public static void HandleInventory(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String action = new Menu().withTitle("Main Menu")
                    .withChoice("1", "Show currently in stock items.")
                    .withChoice("2", "Add new item from template ID")
                    .withChoice("3", "Add new item type to templates")
                    .withChoice("4", "Add custom item")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);

            switch (action) {
                case "1":
                    Table table = new Table();
                    table.addColumnNames(
                            "Material ID", "Name",
                            "Description",
                            "Search tags",
                            "Value per Quantity",
                            "Quantity",
                            "Life span start",
                            "Life span end");
                    Server.Server.currentlystored.materialsSub().forEach(x -> {
                        table.addRow(
                                x.MaterialID().toString(),
                                x.name,
                                x.description,
                                x.tags.toString(),
                                ((Double) x.valuePerQty).toString(),
                                ((Double) x.quantity).toString(),
                                x.lifespanStart.toString(),
                                x.lifespanStart.plusSeconds(x.lifespanInSeconds).toString());
                    });
                    continue;
                case "2":
                    while (true) {
                        HashMap<String, String> result = new InputForm(wR, wW)
                                .withField("Template ID", true)
                                .withField("Quantity", true)
                                .withTitle("Add item to stock by ID")
                                .receiveInput();
                        Optional<Material> findmat = Server.Server.templatematerials.materialsSub().stream()
                                .filter((Material mat) -> {
                                    return result.get("Template ID").equals(mat.MaterialID());
                                }).findFirst();
                        if (!findmat.isPresent()) {
                            wW.write("That Template ID is not present in the database.\n");
                            continue;
                        } else {
                            try {
                                Double.valueOf(result.get("Quantity"));
                            } catch (Exception e) {
                                wW.write("Invalid Quantity value.\n");
                                continue;
                            }
                            findmat.get().quantity += Double.valueOf(result.get("Quantity"));
                            Server.Server.currentlystored.addMaterial(findmat.get());
                            wW.write("Added material.\n");
                            break;
                        }
                    }
                    continue;
                case "3":
                    while (true) {
                        HashMap<String, String> result = new InputForm(wR, wW)
                                .withField("Name",true)
                                .withField("Description", true)
                                .withField("Differentiator (optional)", false)
                                .withField("Value per Qty", true)
                                .withField("Quantity (above 0)", true)
                                .withField("Custom value (optional)", false)
                                .withTitle("Add item to stock by ID")
                                .receiveInput();
                        MaterialBuilder builder = new MaterialBuilder()
                        .withName("")
                        .withDescription("")
                        .withDifferentiator("")
                        .withValuePerQty(1)
                        .withQuantity(11)
                        .withLifespanStart(Instant.now())
                                .withLifespanSeconds(23132);
                                break;

                    }
                    continue;
                case "4":

                    continue;

                case "b":
                    break;

                default:
                    continue;
            }
            break;
        }
    }
}
