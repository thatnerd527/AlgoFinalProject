package Server.Calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import Server.WrappedReader;
import Server.WrappedWriter;
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

class InternalCalculator {

    private MaterialComposite purchased = new MaterialComposite();

    private MaterialComposite sold = new MaterialComposite();

    private MaterialComposite stolen = new MaterialComposite();

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
                    x.MaterialID().toString(),
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
                    .withChoice("b", "Back")
                    .makeASelection(wW, wR);
            switch (action) {
                case "1.1":
                    TablePrinter(new MaterialComposite(new ArrayList<Material>(Server.currentlystored.materials().stream().filter(x -> x.isExpired()).collect(Collectors.toList()))),wW);
                    break;
                case "1.2":
                    TablePrinter(purchased,wW);
                    break;
                case "1.3":
                    TablePrinter(sold,wW);
                    break;
                case "1.4":
                    TablePrinter(stolen, wW);
                    Sandbox sandbox2 = new Sandbox(0);
                    built.materials().stream().forEach(x -> {
                        //sandbox
                    });
                    break;
                case "1.5":
                    TablePrinter(built, wW);
                    Sandbox sandbox = new Sandbox(0);
                    MaterialComposite matdclone = Server.currentlystored.clone();
                    built.materials().stream().forEach(x -> {
                        x.getComposite().materials().forEach(x2 -> {
                            Optional<Material> materialinstock = matdclone.materials().stream().filter(x3 -> x3.MaterialID().intValue() == x2.MaterialID().intValue()).findFirst();
                            if (materialinstock.isPresent()) {
                                Material cloned_materialinstock = materialinstock.get().clone();
                                if (cloned_materialinstock.quantity >= x2.quantity) {
                                    cloned_materialinstock.quantity = x2.quantity;
                                    sandbox.setCurrentValue(sandbox.getCurrentValue()
                                            + (cloned_materialinstock.quantity * cloned_materialinstock.getValuePerQty()));
                                    matdclone.removeMaterial(cloned_materialinstock);
                                } else {
                                    // first we calculate the defficient.
                                    double defficient = cloned_materialinstock.quantity - x2.quantity;
                                    // then we remove the defficient from the stock.
                                    matdclone.removeMaterial(cloned_materialinstock);
                                    // then we calculate the value of the defficient.
                                    sandbox.setCurrentValue(sandbox.getCurrentValue()
                                            + (defficient * cloned_materialinstock.getValuePerQty()));

                                }
                                return;
                            }
                            sandbox.setCurrentValue(sandbox.getCurrentValue() + (x2.quantity * x2.valuePerQty));
                        });
                    });
                    wW.write("Total value of built items: " + sandbox.getCurrentValue() + "\n");

                    break;

                case "2.1":

                    break;
                case "2.2":

                    break;
                case "2.3":

                    break;
                case "2.4":

                    break;
                case "2.5":

                    break;

                default:
                    break;
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

                    continue;

                case "b":
                    break;
            }

            break;
        }
    }
}
