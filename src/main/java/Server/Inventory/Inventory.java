package Server.Inventory;

import Server.Server;
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
                            "Material ID","Name",
                            "Description",
                            "Search tags",
                            "Value per Quantity",
                            "Quantity",
                            "Life span start",
                            "Life span end");
                    Server.currentlystored.materialsSub().forEach(x -> {
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
                    new InputForm(wR, wW)
                    .withField("ONEfield",false)
                            .withField("TWOfield", true)
                    .withTitle("something")
                            .receiveInput();
                    continue;
                case "3":

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
