package Server.Inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private static String sortby = "none";

    private static boolean comparator(Material a, Material b) {
        switch (sortby) {
            case "materialid":
                return a.MaterialID() > b.MaterialID();
            case "lifespanstart":
                return a.lifespanStartUnixMilli > b.lifespanStartUnixMilli;
            case "lifespanend":
                return a.getLifespanStart().plusSeconds(a.lifespanInSeconds).getEpochSecond() > b.getLifespanStart()
                        .plusSeconds(b.lifespanInSeconds).getEpochSecond();
            case "quantity":
                return a.quantity > b.quantity;
            case "valueperqty":
                return a.getValuePerQty() > b.getValuePerQty();
            case "value":
                return a.getValue() > b.getValue();
            default:
                return false;
        }
    }

    private static void sortTable(Material array[]) {
        if (sortby.equals("none"))
            return;
        int n = array.length;
        for (int j = 1; j < n; j++) {
            Material key = array[j];
            int i = j - 1;
            while ((i > -1) && comparator(array[i], key)) {
                array[i + 1] = array[i];
                i--;
            }
            array[i + 1] = key;
        }
    }

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
                    .withChoice("9", "Edit sorting settings")
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
                    Material[] mats = Server.Server.currentlystored.materials().toArray(new Material[0]);
                    sortTable(mats);
                    ArrayList<Material> result2 = new ArrayList<>();
                    for (Material material : mats) {
                        result2.add(material);
                    }


                    DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("uuuu-MM-dd'T'hh:mm'Z'")
        .withZone(ZoneOffset.UTC);

// Fails for current time with error 'Field DayOfYear cannot be printed as the 
// value 148 exceeds the maximum print width of 2'
;

                    result2.forEach(x -> {
                        table.addRow(
                                Integer.valueOf(x.MaterialID())
                                        .toString(),
                                x.name,
                                x.description,
                                x.tags.toString(),
                                x.differentiator,
                                ((Double) x
                                        .getValuePerQty()).toString(),
                                ((Double) x.quantity).toString(),
                    x.lifespanInSeconds > 0 ? LocalDateTime
      .ofInstant(x.getLifespanStart(), ZoneOffset.UTC)
      .format(formatter) : "",
                    x.lifespanInSeconds > 0 ? LocalDateTime
      .ofInstant(x.getLifespanStart().plusSeconds(x.lifespanInSeconds), ZoneOffset.UTC)
      .format(formatter) : "",
                                x.lifespanInSeconds > 0 ? x.isExpired().toString() : "");
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
                                Integer.valueOf(x.MaterialID())
                                        .toString(),
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
                    Material templatematerial = InventoryFunctions.GetTemplateMaterial(wR, wW);
                    if (templatematerial == null) {
                        continue;
                    }
                    templatematerial = InventoryFunctions.ModifyMaterialQuantity(wR, wW, templatematerial);
                    templatematerial = InventoryFunctions.ModifyMaterialDifferentiator(wR, wW, templatematerial);
                    templatematerial = InventoryFunctions.ModifyMaterialCost(wR, wW, templatematerial);

                    Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                    if (templatematerial.lifespanInSeconds > 0) {
                        boolean specifyStart = new Menu()
                                .withTitle("Change the item's start lifespan?")
                                .withChoice("Y", "Yes")
                                .withChoice("N", "No (uses now)")
                                .makeASelection(wW, wR).equals("Y");
                        if (specifyStart) {
                            startLifespan = InventoryFunctions.DateEntry(wR, wW);
                        }
                        templatematerial.setLifespanStart(startLifespan);

                    }

                    Server.Server.currentlystored.addMaterial(templatematerial.clone());
                    Server.Server.SaveAll();
                    wW.write("Added material.\n");
                    continue;
                case "4":
                    Server.Server.templatematerials.addMaterial(InventoryFunctions.CreateMaterial(wR, wW));
                    Server.Server.SaveAll();

                    continue;
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
                                return result.get("Material ID").equals(Integer.valueOf(mat.MaterialID()).toString());
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
                    Server.Server.SaveAll();
                    continue;
                case "7":
                    Material target = InventoryFunctions.GetTemplateMaterial(wR, wW);
                    if (target == null) {
                        continue;
                    }
                    Material newmaterial = InventoryFunctions.CreateMaterial(wR, wW);
                    Server.Server.templatematerials.removeMaterial(target);
                    Server.Server.templatematerials.addMaterial(newmaterial);
                    Server.Server.SaveAll();
                    continue;
                case "8":
                    Material target2 = InventoryFunctions.GetInStockMaterial(wR, wW);
                    if (target2 == null) {
                        continue;
                    }
                    Material newmaterial2 = InventoryFunctions.ModifyMaterialQuantity(wR, wW,
                            InventoryFunctions.CreateMaterial(wR, wW));
                    Server.Server.templatematerials.removeMaterial(target2);
                    Server.Server.templatematerials.addMaterial(newmaterial2);
                    Server.Server.SaveAll();
                    continue;
                case "9":
                    String sortby = new Menu()
                            .withTitle("Sorting settings")
                            .withChoice("materialid", "Sort by material ID")
                            .withChoice("lifespanstart", "Sort by start of lifespan")
                            .withChoice("lifespanend", "Sort by end of lifespan")
                            .withChoice("quantity", "Sort by quantity")
                            .withChoice("valueperqty", "Sort by value per quantity")
                            .withChoice("value", "Sort by value")
                            .withChoice("none", "No sorting")
                            .makeASelection(wW, wR);
                    Inventory.sortby = sortby;
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
