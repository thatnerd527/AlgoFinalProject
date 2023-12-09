package Server.Calculator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import Server.WrappedReader;
import Server.WrappedWriter;
import Server.Inventory.InventoryFunctions;
import UI.InputForm;
import UI.Menu;
import UI.Table;
import Material.Material;
import Material.MaterialComposite;
import Material.MaterialDatabase;
import Server.Server;

class Sandbox {
    double currentValue;

    public Sandbox(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
}

enum TransactionType {
    ADD,
    REMOVE,
}

class Transaction {
    TransactionType transactionType;
    Material material;
    public String id = UUID.randomUUID().toString();

    public Transaction(TransactionType transactionType, Material material) {
        this.transactionType = transactionType;
        this.material = material;
    }

}

class Totaller {
    private MaterialComposite materialComposite;

    private ArrayList<Transaction> transactions = new ArrayList<>();

    Totaller(MaterialComposite materialComposite) {
        this.materialComposite = materialComposite.clone();
    }

    public MaterialComposite addTransaction(Transaction transaction) {
        transactions.add(transaction);
        return crunchData();
    }

    

    MaterialComposite crunchData() {
        MaterialComposite clone = materialComposite.clone();
        transactions.stream().forEach(x -> {
            if (x.transactionType == TransactionType.ADD) {
                clone.addMaterial(x.material);
            } else if (x.transactionType == TransactionType.REMOVE) {
                clone.removeMaterial(x.material);
            }
        });
        return clone;
    }

}

class InternalCalculator {

    private MaterialComposite purchased = new MaterialComposite();

    private MaterialComposite sold = new MaterialComposite();

    private MaterialComposite stolen = new MaterialComposite();

    private MaterialComposite expired = new MaterialComposite();

    private MaterialComposite built = new MaterialComposite();

    void TablePrinter(MaterialComposite materialComposite, WrappedWriter wW) {
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
        materialComposite.materials().stream().forEach(x -> {
            table.addRow(
                    Integer.valueOf(x.MaterialID()).toString(),
                    x.name,
                    x.description,
                    x.tags.toString(),
                    x.differentiator,
                    ((Double) x.getValuePerQty()).toString(),
                    ((Double) x.quantity).toString(),
                    x.getLifespanStart().toString(),
                    x.getLifespanStart().plusSeconds(x.lifespanInSeconds).toString());
        });
        wW.write(Table.getPrintedTable(table, true) + "\n");
    }

    double ShowExpiredItems(WrappedWriter wW) {
        MaterialComposite expired =new MaterialComposite(new ArrayList<Material>(Server.currentlystored.materials()
                .stream().filter(x -> x.isExpired()).collect(Collectors.toList())));
        TablePrinter(expired, wW);
        Sandbox sandbox = new Sandbox(0);
        expired.materials().stream().forEach(x -> {
            sandbox.setCurrentValue(sandbox.getCurrentValue() + x.getValue());
        });
        wW.write("Total value of expired items: " + sandbox.getCurrentValue() + "\n");
        return sandbox.getCurrentValue();
    }

    double ShowPurchasedItems(WrappedWriter wW) {
        TablePrinter(purchased, wW);
        Sandbox sandbox4 = new Sandbox(0);
        built.materials().stream().forEach(x -> {
            sandbox4.setCurrentValue(sandbox4.getCurrentValue() + x.getValue());
        });
        wW.write("Total value of purchased items: " + sandbox4.getCurrentValue() + "\n");
        return sandbox4.getCurrentValue();
    }

    double ShowSoldItems(WrappedWriter wW) {
        TablePrinter(sold, wW);
        Sandbox sandbox3 = new Sandbox(0);
        sold.materials().stream().forEach(x -> {
            sandbox3.setCurrentValue(sandbox3.getCurrentValue() + x.getValue());
        });
        wW.write("Total value of sold items: " + sandbox3.getCurrentValue() + "\n");
        return sandbox3.getCurrentValue();
    }

    double ShowStolenItems(WrappedWriter wW) {
        TablePrinter(stolen, wW);
        Sandbox sandbox2 = new Sandbox(0);
        stolen.materials().stream().forEach(x -> {
            sandbox2.setCurrentValue(sandbox2.getCurrentValue() + x.getValue());
        });
        wW.write("Total value of stolen items: " + sandbox2.getCurrentValue() + "\n");
        return sandbox2.getCurrentValue();
    }

    double ShowBuiltItems(WrappedWriter wW) {
        TablePrinter(built, wW);
        Sandbox sandbox = new Sandbox(0);
        MaterialComposite matdclone = Server.currentlystored.clone();
        built.materials().stream().forEach(x -> {
            x.getComposite().materials().forEach(x2 -> {
                Optional<Material> materialinstock = matdclone.materials().stream()
                        .filter(x3 -> x3.MaterialID() == x2.MaterialID()).findFirst();
                if (materialinstock.isPresent()) {
                    Material cloned_materialinstock = materialinstock.get().clone();
                    if (cloned_materialinstock.quantity >= x2.quantity) {
                        cloned_materialinstock.quantity = x2.quantity;
                        sandbox.setCurrentValue(sandbox.getCurrentValue()
                                + (cloned_materialinstock.quantity
                                        * cloned_materialinstock.getValuePerQty()));
                        matdclone.removeMaterial(cloned_materialinstock);
                    } else {
                        // first we calculate the defficient.
                        double defficient = cloned_materialinstock.quantity - x2.quantity;
                        // then we remove the defficient from the stock.
                        matdclone.removeMaterial(cloned_materialinstock);

                        // then we add the defficient amount
                        sandbox.setCurrentValue(sandbox.getCurrentValue()
                                + (defficient * cloned_materialinstock.getValuePerQty()));

                    }
                    return;
                }
                sandbox.setCurrentValue(sandbox.getCurrentValue() + (x2.quantity * x2.getValuePerQty()));
            });
        });
        wW.write("Total value of built items: " + sandbox.getCurrentValue() + "\n");
        return sandbox.getCurrentValue();
    }

    public void CalculatorUI(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            String action = new Menu()
                    .withTitle("Calculator")
                    .withChoice("1.1", "Show items that are expired")
                    .withChoice("1.2", "Show purchased items")
                    .withChoice("1.3", "Show sold items")
                    .withChoice("1.4", "Show stolen items")
                    .withChoice("1.5", "Show built items")
                    .withChoice("2.1", "Remove expired items")
                    .withChoice("2.2", "Add purchased item to record")
                    .withChoice("2.3", "Add sold item to record")
                    .withChoice("2.4", "Add stolen item to record")
                    .withChoice("2.5", "Add built item to record")
                    .withChoice("3", "Show final table.")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);
            switch (action) {
                case "1.1":
                    ShowExpiredItems(wW);
                    continue;
                case "1.2":
                    ShowPurchasedItems(wW);
                    continue;
                case "1.3":
                    ShowSoldItems(wW);
                    continue;
                case "1.4":
                    ShowStolenItems(wW);
                    continue;
                case "1.5":
                    ShowBuiltItems(wW);
                    continue;

                case "2.1":
                    MaterialComposite toremove = new MaterialComposite(
                            new ArrayList<Material>(Server.currentlystored.materials()
                                    .stream().filter(x -> x.isExpired()).collect(Collectors.toList())));
                    TablePrinter(toremove, wW);
                    String choice = new Menu()
                            .withTitle("Are you sure you want to remove all expired items?")
                            .withChoice("Y", "Yes")
                            .withChoice("N", "No")
                            .makeASelection(wW, wR);
                    if (choice.equals("Y")) {
                        toremove.materials().stream().forEach(x -> {
                            expired.addMaterial(x);
                        });
                        wW.write("All expired items will be removed, after commit to database\n");
                        continue;
                    }
                    wW.write("Expired items have not been removed.\n");
                    continue;
                case "2.2":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetTemplateMaterial(wR, wW);
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW, inStockFromTemplate);
                        Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                        if (inStockFromTemplate.lifespanInSeconds > 0) {
                            boolean specifyStart = new Menu()
                                    .withTitle("Change the item's start lifespan?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No (uses now)")
                                    .makeASelection(wW, wR).equals("Y");
                            if (specifyStart) {
                                startLifespan = InventoryFunctions.DateEntry(wR, wW);
                            }
                            inStockFromTemplate.setLifespanStart(startLifespan);

                        }
                        purchased.addMaterial(inStockFromTemplate.clone());
                        wW.write("Added material to purchased.\n");
                        break;

                    }
                    continue;
                case "2.3":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetInStockMaterial(wR, wW);
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW, inStockFromTemplate);
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialCost(wR,wW,inStockFromTemplate);
                        if (inStockFromTemplate.isExpired()) {
                            String choice2 = new Menu()
                                    .withTitle("This item is expired, are you sure you want to sell it?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No")
                                    .makeASelection(wW, wR);
                            if (choice2.equals("N")) {
                                wW.write("Item has not been sold.\n");
                                break;
                            }
                        }

                        sold.addMaterial(inStockFromTemplate.clone());
                        wW.write("Added material to sold.\n");
                        break;

                    }
                    continue;
                case "2.4":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetInStockMaterial(wR, wW);
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW, inStockFromTemplate);
                        stolen.addMaterial(inStockFromTemplate.clone());
                        wW.write("Added material to stolen.\n");
                        break;

                    }
                    continue;
                case "2.5":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetTemplateMaterial(wR, wW);
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW, inStockFromTemplate);
                        Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                        if (inStockFromTemplate.lifespanInSeconds > 0) {
                            boolean specifyStart = new Menu()
                                    .withTitle("Change the item's start lifespan?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No (uses now)")
                                    .makeASelection(wW, wR).equals("Y");
                            if (specifyStart) {
                                startLifespan = InventoryFunctions.DateEntry(wR, wW);
                            }
                            inStockFromTemplate.setLifespanStart(startLifespan);

                        }

                        built.addMaterial(inStockFromTemplate.clone());
                        wW.write("Added built material.\n");
                        break;

                    }
                    continue;
                case "3":
                    MaterialComposite matdclone = Server.currentlystored.clone();
                    double total = 0;
                    for (Material mat : purchased.materials()) {
                        matdclone.addMaterial(mat);
                        total -= mat.getValue();
                    }
                    for (Material mat : expired.materials()) {
                        matdclone.removeMaterial(mat);
                        total -= mat.getValue();
                    }
                    for (Material mat : sold.materials()) {
                        matdclone.removeMaterial(mat);
                        total -= mat.getValue();
                    }

                    wW.write("Expired items: \n");
                    ShowExpiredItems(wW);
                    wW.write("Purchased items: \n");
                    ShowPurchasedItems(wW);
                    wW.write("Sold items: \n");
                    ShowSoldItems(wW);
                    wW.write("Stolen items: \n");
                    ShowStolenItems(wW);
                    wW.write("Built items: \n");
                    ShowBuiltItems(wW);
                    continue;

                default:
                    continue;
            }
        }
    }
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

                    if (Calculator.containsKey(result.get("Name"))) {
                        wW.write("Calculator ini sudah ada\n");
                        continue;
                    }

                    // 2. Make a new internal calculator
                    Calculator.put(result.get("Name"), new InternalCalculator());
                    wW.write("Calculator berhasil untuk ditambahkan \n");
                    // 3. Put inside a hashmap by using the "key" from a user

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
                    HashMap<String, String> result2 = new InputForm(wR, wW)
                            .withField("Name", true).withTitle("Calculator").receiveInput();

                    if (!Calculator.containsKey(result2.get("Name"))) {
                        wW.write("Calculator ini tidak ada\n");
                        continue;
                    }
                    Calculator.get(result2.get("Name")).CalculatorUI(wR, wW);

                    continue;

                case "b":
                    break;
            }

            break;
        }
    }
}
