package Server.Calculator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import Material.Material;
import Material.MaterialComposite;
import Server.Server;
import Server.WrappedReader;
import Server.WrappedWriter;
import Server.Inventory.InventoryFunctions;
import UI.InputForm;
import UI.Menu;
import UI.Table;

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
    EXTCHANGE
}

class Transaction {
    TransactionType transactionType;
    Material material;
    public String id = UUID.randomUUID().toString();

    public Transaction(TransactionType transactionType, Material material) {
        this.transactionType = transactionType;
        this.material = material;
    }

    public Transaction(TransactionType transactionType, Material material, String id) {
        this.transactionType = transactionType;
        this.material = material;
        this.id = id;
    }

    public Transaction clone() {
        return new Transaction(transactionType, material.clone(), String.valueOf(id));
    }

    @Override
    public String toString() {
        return String.format("Transaction: %s %s", transactionType, Integer.valueOf(material.MaterialID()).toString(),
                id);

    }

}

class Totaller {
    private MaterialComposite materialComposite;

    private ArrayList<Transaction> transactions = new ArrayList<>();

    private Transaction lastadded = null;

    Totaller(MaterialComposite materialComposite) {
        this.materialComposite = materialComposite.clone();
    }

    public MaterialComposite addTransaction(Transaction transaction) {
        transactions.add(transaction);
        lastadded = transaction.clone();
        return crunchData();
    }

    public MaterialComposite removeTransaction(String transactionID) {
        transactions.removeIf(x -> x.id.equals(transactionID));
        lastadded = null;
        return crunchData();
    }

    public Totaller withTransaction(Transaction transaction) {
        transactions.add(transaction);
        return this;
    }

    public Totaller withoutTransaction(String transactionID) {
        transactions.removeIf(x -> x.id.equals(transactionID));
        return this;
    }

    public Transaction lastAdded() {
        return lastadded;
    }

    public ArrayList<Transaction> transactionList() {
        return new ArrayList<Transaction>(transactions.stream().map(x -> x.clone()).collect(Collectors.toList()));
    }

    MaterialComposite crunchData() {
        MaterialComposite clone = materialComposite.clone();
        transactions.stream().forEach(x -> {
            if (x.transactionType == TransactionType.ADD) {
                x.material.oneshotMetadata = x.id;
                clone.addMaterial(x.material);
            } else if (x.transactionType == TransactionType.REMOVE) {
                clone.removeMaterial(x.material);
            }
        });
        return clone;
    }

}

class InternalCalculator {

    private Totaller TPS = new Totaller(Server.currentlystored);

    private MaterialComposite purchased = new MaterialComposite();

    private MaterialComposite sold = new MaterialComposite();

    private MaterialComposite stolen = new MaterialComposite();

    private MaterialComposite expired = new MaterialComposite();

    private MaterialComposite built = new MaterialComposite();

    private String sortby = "none";

    private boolean comparator(Material a, Material b) {
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

    private void sortTable(Material array[]) {
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

    // Get all of the materials that are required to build one unit of x, and if the
    // material is not present in the current database then also add it to the list,
    // it will go down to eventually materials that have no composite.

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
        Material[] mats = materialComposite.materials().toArray(new Material[0]);
        sortTable(mats);
        ArrayList<Material> result = new ArrayList<>();
        for (Material material : mats) {
            result.add(material);
        }
        result.stream().forEach(x -> {
            table.addRow(
                    Integer.valueOf(x.MaterialID()).toString(),
                    x.name,
                    x.description,
                    x.tags.toString(),
                    x.differentiator,
                    ((Double) x.getValuePerQty()).toString(),
                    ((Double) x.quantity).toString(),
                    x.lifespanInSeconds > 0 ? x.getLifespanStart().toString() : "",
                    x.lifespanInSeconds > 0 ? x.getLifespanStart().plusSeconds(x.lifespanInSeconds).toString() : "");
        });
        wW.write(Table.getPrintedTable(table, true) + "\n");
    }

    double ShowExpiredItems(WrappedWriter wW) {
        MaterialComposite expired = new MaterialComposite(new ArrayList<Material>(TPS.crunchData().materials()
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
        purchased.materials().stream().forEach(x -> {
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
                    .withChoice("0.1", "Sorting settings")
                    .withChoice("1.1", "Show items that are expired")
                    .withChoice("1.2", "Show purchased items")
                    .withChoice("1.3", "Show sold items")
                    .withChoice("1.4", "Show stolen items")
                    .withChoice("1.5", "Show built items")
                    .withChoice("2.1", "Throw away expired items")
                    .withChoice("2.2", "Add purchased item to record")
                    .withChoice("2.3", "Add sold item to record")
                    .withChoice("2.4", "Add stolen item to record")
                    .withChoice("2.5", "Add built item to record")
                    .withChoice("3", "Show final table.")
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);
            switch (action) {
                case "0.1":
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
                    this.sortby = sortby;
                    continue;
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
                            new ArrayList<Material>(TPS.crunchData().materials()
                                    .stream().filter(x -> x.isExpired()).map(x -> x.clone())
                                    .collect(Collectors.toList())));
                    TablePrinter(toremove, wW);
                    String choice = new Menu()
                            .withTitle("Are you sure you want to remove all expired items?")
                            .withChoice("Y", "Yes")
                            .withChoice("N", "No")
                            .makeASelection(wW, wR);
                    if (choice.equals("Y")) {
                        toremove.materials().stream().forEach(x -> {

                            TPS.addTransaction(new Transaction(TransactionType.REMOVE, x, "expire_" + x.MaterialID()));
                        });
                        wW.write("All expired items will be removed, after commit to database\n");
                        continue;
                    }
                    wW.write("Expired items have not been removed.\n");
                    continue;
                case "2.2":
                    while (true) {
                        Material inStockFromTemplate = null;
                        String newitemtype = new Menu().withTitle("Was a new item type purchased?")
                                .withChoice("Y", "Yes")
                                .withChoice("N", "No")
                                .makeASelection(wW, wR);
                        if (newitemtype.equals("Y")) {
                            inStockFromTemplate = InventoryFunctions.CreateMaterial(wR, wW);
                            inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW,
                                    inStockFromTemplate);
                        } else {
                            inStockFromTemplate = InventoryFunctions.GetTemplateMaterial(wR, wW);
                            if (inStockFromTemplate == null) {
                                break;
                            }
                            inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW,
                                    inStockFromTemplate);
                            inStockFromTemplate = InventoryFunctions.ModifyMaterialCost(wR, wW, inStockFromTemplate);

                        }
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

                        if (newitemtype.equals("Y")) {
                            String addtotemplateitems = new Menu().withTitle("Add this item to template items?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No")
                                    .makeASelection(wW, wR);
                            if (addtotemplateitems.equals("Y")) {
                                TPS.addTransaction(new Transaction(TransactionType.EXTCHANGE, inStockFromTemplate,
                                        "addtotemplate_" + inStockFromTemplate.MaterialID()));
                            }
                        }
                        purchased.addMaterial(inStockFromTemplate);

                        TPS.addTransaction(new Transaction(TransactionType.ADD, inStockFromTemplate,
                                "purchase_" + inStockFromTemplate.MaterialID()));
                        wW.write("Added material to purchased.\n");
                        break;

                    }
                    continue;
                case "2.3":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetInStockMaterial(wR, wW, TPS.crunchData());
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        if (inStockFromTemplate.overrideValue == 0) {
                            inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW,
                                    inStockFromTemplate);
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialCost(wR, wW, inStockFromTemplate);
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
                        sold.addMaterial(inStockFromTemplate);
                        TPS.addTransaction(new Transaction(TransactionType.REMOVE, inStockFromTemplate,
                                "sell_" + inStockFromTemplate.MaterialID()));
                        wW.write("Added material to sold.\n");
                        break;

                    }
                    continue;
                case "2.4":
                    while (true) {
                        Material inStockFromTemplate = InventoryFunctions.GetInStockMaterial(wR, wW, TPS.crunchData());
                        if (inStockFromTemplate == null) {
                            break;
                        }
                        inStockFromTemplate = InventoryFunctions.ModifyMaterialQuantity(wR, wW, inStockFromTemplate);
                        // inStockFromTemplate.overrideValue = -inStockFromTemplate.getValue();
                        stolen.addMaterial(inStockFromTemplate);
                        TPS.addTransaction(new Transaction(TransactionType.REMOVE, inStockFromTemplate,
                                "steal_" + inStockFromTemplate.MaterialID()));
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
                        CalculatorFunctions.getMaterialsForBuild(inStockFromTemplate, TPS.crunchData())
                                .materials().stream().forEach(x -> {
                                    TPS.addTransaction(new Transaction(TransactionType.REMOVE, x,
                                            "build_submaterial_" + x.MaterialID()));
                                });
                        built.addMaterial(inStockFromTemplate);
                        TPS.addTransaction(new Transaction(TransactionType.ADD, inStockFromTemplate,
                                "built_" + inStockFromTemplate.MaterialID()));
                        wW.write("Added built material.\n");
                        break;

                    }
                    continue;
                case "3":
                    double netRevenue = 0;
                    for (Transaction x : TPS.transactionList()) {
                        if (x.transactionType == TransactionType.REMOVE) {
                            if (x.id.startsWith("expire_")) {
                                netRevenue -= x.material.getValue();
                            }
                            if (x.id.startsWith("sell_")) {
                                netRevenue += x.material.getValue();
                            }
                            if (x.id.startsWith("steal_")) {
                                netRevenue -= x.material.getValue();
                            }
                            if (x.id.startsWith("build_submaterial_")) {
                                netRevenue -= x.material.getValue();
                            }
                            if (x.id.startsWith("built_")) {
                                netRevenue += x.material.getValue();
                            }
                        } else if (x.transactionType == TransactionType.ADD) {
                            if (x.id.startsWith("purchase_")) {
                                netRevenue -= x.material.getValue();
                            }

                        }
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
                    wW.write("Tabulated (after changes) items: \n");
                    TablePrinter(TPS.crunchData(), wW);
                    wW.write("Net revenue: " + netRevenue + "\n");
                    String choice2 = new Menu()
                            .withTitle("Commit changes?")
                            .withChoice("Y", "Yes")
                            .withChoice("N", "No")
                            .makeASelection(wW, wR);
                    if (choice2.equals("Y")) {
                        Server.currentlystored.setMaterials(TPS.crunchData().materials());
                        Totaller temp = new Totaller(Server.templatematerials);
                        TPS.transactionList().stream().filter(x -> x.transactionType == TransactionType.EXTCHANGE
                                && x.id.startsWith("addtotemplate_"))
                                .forEach(x -> {
                                    x.material.quantity = 1;
                                    temp.addTransaction(new Transaction(TransactionType.ADD, x.material));
                                });
                        Server.SaveAll();
                        wW.write("Changes committed, thanks for using us for your business.\n");
                    } else {
                        wW.write("Changes ignored.\n");
                    }
                    String choice3 = new Menu()
                            .withTitle("Do you want to save all details to a file?")
                            .withChoice("Y", "Yes")
                            .withChoice("N", "No")
                            .makeASelection(wW, wR);
                    if (choice3.equals("Y")) {
                        while (true) {
                            String filename = new InputForm(wR, wW)
                                    .withField("Filename", true).withTitle("Transaction log").receiveInput()
                                    .get("Filename");
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                                writer.write("Expired items: \n");
                                ShowExpiredItems(new WrappedWriter(writer));
                                writer.write("Purchased items: \n");
                                ShowPurchasedItems(new WrappedWriter(writer));
                                writer.write("Sold items: \n");
                                ShowSoldItems(new WrappedWriter(writer));
                                writer.write("Stolen items: \n");
                                ShowStolenItems(new WrappedWriter(writer));
                                writer.write("Built items: \n");
                                ShowBuiltItems(new WrappedWriter(writer));
                                writer.write("Tabulated (after changes) items: \n");
                                TablePrinter(TPS.crunchData(), new WrappedWriter(writer));
                                writer.write("Net revenue: " + netRevenue + "\n");
                                writer.write("Changes committed, thanks for using us for your business.\n");
                                writer.write("Transaction log: \n");
                                for (Transaction x : TPS.transactionList()) {
                                    writer.write(x.toString() + "\n");
                                }
                                writer.close();
                                break;

                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                continue;
                            }
                        }
                    }

                    continue;
                case "b":
                    return;

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
