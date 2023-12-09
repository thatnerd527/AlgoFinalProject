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

    public static Material CreateMaterial(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            HashMap<String, String> result = new InputForm(wR, wW)
                    .withField("Name", true)
                    .withField("Description", true)
                    .withField("Differentiator (optional)", false)
                    .withField("Value per Qty", true)
                    .withField("Custom value (optional)", false)
                    .withTitle("Add item to templates by ID: Basic Data")
                    .receiveInput();
            // #region Input validation
            try {
                if (Double.valueOf(result.get("Value per Qty")) <= 0) {
                    throw new Exception();
                }

            } catch (Exception e) {
                wW.write("Value per Qty has to be a valid positive number.");
                continue;
            }
            try {
                if (result.get("Custom value (optional)").length() > 0) {
                    if (Double.valueOf(result.get("Custom value (optional)")) <= 0) {
                        throw new Exception();
                    }
                }

            } catch (Exception e) {
                wW.write("Custom value has to be a valid positive number.");
                continue;
            }
            // #endregion

            MaterialBuilder builder = new MaterialBuilder()
                    .withName(result.get("Name"))
                    .withDescription(result.get("Description"))
                    .withDifferentiator(result.get("Differentiator (optional)"))
                    .withValuePerQty(Double.valueOf(result.get("Value per Qty")))
                    .withQuantity(0);
            if (result.get("Custom value (optional)").length() > 0) {
                builder = builder.withOverrideValue(Double.valueOf(result.get("Custom value (optional)")));
            }

            boolean hasLifespan = new Menu()
                    .withTitle("Does this item have a lifespan?")
                    .withChoice("Y", "Yes")
                    .withChoice("N", "No")
                    .makeASelection(wW, wR).equals("Y");

            if (hasLifespan) {
                boolean specifyStart = new Menu()
                        .withTitle("Specify the item's start lifespan (for calculation)?")
                        .withChoice("Y", "Yes")
                        .withChoice("N", "No (uses now)")
                        .makeASelection(wW, wR).equals("Y");

                Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                if (specifyStart) {
                    while (true) {
                        try {
                            HashMap<String, String> dateinput = new InputForm(wW, wR)
                                    .withField("Date (2 digits)", true)
                                    .withField("Month (2 digits)", true)
                                    .withField("Year (4 digits)", true)
                                    .withTitle("Specify the item's start lifespan (for calculation)")
                                    .receiveInput();
                            String stringDate = dateinput.get("Date (2 digits)") + "/"
                                    + dateinput.get("Month (2 digits)") + "/"
                                    + dateinput.get("Year (4 digits)");
                            String pattern = "dd/MM/yyyy";
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                            startLifespan = LocalDate.parse(stringDate, dateFormatter)
                                    .atStartOfDay(ZoneId.systemDefault()).toInstant();

                        } catch (Exception e) {
                            wW.write("Invalid date format.\n");
                            continue;
                        }
                    }
                }
                while (true) {
                    try {
                        HashMap<String, String> dateinput = new InputForm(wW, wR)
                                .withField("Date (2 digits)", true)
                                .withField("Month (2 digits)", true)
                                .withField("Year (4 digits)", true)
                                .withTitle("Specify the item's end lifespan")
                                .receiveInput();
                        String stringDate = dateinput.get("Date (2 digits)") + "/"
                                + dateinput.get("Month (2 digits)") + "/"
                                + dateinput.get("Year (4 digits)");
                        String pattern = "dd/MM/yyyy";
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                        Instant endLifespan = LocalDate.parse(stringDate, dateFormatter)
                                .atStartOfDay(ZoneId.systemDefault()).toInstant();
                        if (startLifespan.isAfter(endLifespan)) {
                            wW.write("Start date cannot be after end date.\n");
                            continue;
                        }
                        if (!specifyStart) {
                            startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                                    .toInstant();
                        }
                        builder = builder.withLifespanStart(startLifespan)
                                .withLifespanSeconds(
                                        endLifespan.getEpochSecond() - startLifespan.getEpochSecond());
                        break;
                    } catch (Exception e) {
                        wW.write("Invalid date format.\n");
                        continue;
                    }
                }

            }

            boolean isComposite = new Menu()
                    .withTitle("Is this item a composite?")
                    .withChoice("Y", "Yes")
                    .withChoice("N", "No")
                    .makeASelection(wW, wR).equals("Y");
            if (isComposite) {
                MaterialComposite composite = new MaterialComposite();
                while (true) {
                    String choice = new Menu()
                            .withTitle("Material Composite adder\n| Amount of materials in composite: "
                                    + composite.getMaterials().size())
                            .withChoice("+", "Add material to composite")
                            .withChoice("-", "Remove material from composite")
                            .withChoice("F", "Finish")
                            .makeASelection(wW, wR);
                    switch (choice) {
                        case "+":
                            while (true) {
                                HashMap<String, String> result2 = new InputForm(wR, wW)
                                        .withField("Template ID", true)
                                        .withField("Amount (above 0)", true)
                                        .withTitle("Add existing template material to composite")
                                        .receiveInput();
                                try {
                                    if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    wW.write("Amount has to be a valid positive number.");
                                    continue;
                                }
                                Optional<Material> getmat = Server.Server.templatematerials.materials()
                                        .stream()
                                        .filter(x -> result2.get("Template ID")
                                                .equals(x.MaterialID().toString()))
                                        .findFirst();
                                if (!getmat.isPresent()) {
                                    wW.write("Invalid template ID.\n");
                                    break;
                                }
                                getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                composite.addMaterial(getmat.get());
                            }
                            continue;
                        case "-":
                            while (true) {
                                HashMap<String, String> result2 = new InputForm(wR, wW)
                                        .withField("Template ID", true)
                                        .withField("Amount (above 0)", true)
                                        .withTitle("Remove existing template material in composite")
                                        .receiveInput();
                                try {
                                    if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    wW.write("Amount has to be a valid positive number.");
                                    continue;
                                }
                                Optional<Material> getmat = composite.materials()
                                        .stream()
                                        .filter(x -> result2.get("Template ID")
                                                .equals(x.MaterialID().toString()))
                                        .findFirst();
                                if (!getmat.isPresent()) {
                                    wW.write("Invalid template ID.\n");
                                    break;
                                }
                                getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                composite.removeMaterial(getmat.get());
                            }
                            continue;
                        case "F":
                            builder = builder.withComposite(composite);
                            break;

                        default:
                            break;
                    }
                    break;

                }
            }

            wW.write("Material ID: " + builder.build().MaterialID() + "\n");
            return builder.build();
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
                                    while (true) {
                                        try {
                                            HashMap<String, String> dateinput = new InputForm(wW, wR)
                                                    .withField("Date (2 digits)", true)
                                                    .withField("Month (2 digits)", true)
                                                    .withField("Year (4 digits)", true)
                                                    .withTitle("Specify the item's start lifespan")
                                                    .receiveInput();
                                            String stringDate = dateinput.get("Date (2 digits)") + "/"
                                                    + dateinput.get("Month (2 digits)") + "/"
                                                    + dateinput.get("Year (4 digits)");
                                            String pattern = "dd/MM/yyyy";
                                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                                            startLifespan = LocalDate.parse(stringDate, dateFormatter)
                                                    .atStartOfDay(ZoneId.systemDefault()).toInstant();

                                        } catch (Exception e) {
                                            wW.write("Invalid date format.\n");
                                            continue;
                                        }
                                    }
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
                    while (true) {
                        HashMap<String, String> result = new InputForm(wR, wW)
                                .withField("Name", true)
                                .withField("Description", true)
                                .withField("Differentiator (optional)", false)
                                .withField("Value per Qty", true)
                                .withField("Custom value (optional)", false)
                                .withTitle("Add item to templates by ID: Basic Data")
                                .receiveInput();
                        // #region Input validation
                        try {
                            if (Double.valueOf(result.get("Value per Qty")) <= 0) {
                                throw new Exception();
                            }

                        } catch (Exception e) {
                            wW.write("Value per Qty has to be a valid positive number.");
                            continue;
                        }
                        try {
                            if (result.get("Custom value (optional)").length() > 0) {
                                if (Double.valueOf(result.get("Custom value (optional)")) <= 0) {
                                    throw new Exception();
                                }
                            }

                        } catch (Exception e) {
                            wW.write("Custom value has to be a valid positive number.");
                            continue;
                        }
                        // #endregion

                        MaterialBuilder builder = new MaterialBuilder()
                                .withName(result.get("Name"))
                                .withDescription(result.get("Description"))
                                .withDifferentiator(result.get("Differentiator (optional)"))
                                .withValuePerQty(Double.valueOf(result.get("Value per Qty")))
                                .withQuantity(0);
                        if (result.get("Custom value (optional)").length() > 0) {
                            builder = builder.withOverrideValue(Double.valueOf(result.get("Custom value (optional)")));
                        }

                        boolean hasLifespan = new Menu()
                                .withTitle("Does this item have a lifespan?")
                                .withChoice("Y", "Yes")
                                .withChoice("N", "No")
                                .makeASelection(wW, wR).equals("Y");

                        if (hasLifespan) {
                            boolean specifyStart = new Menu()
                                    .withTitle("Specify the item's start lifespan (for calculation)?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No (uses now)")
                                    .makeASelection(wW, wR).equals("Y");

                            Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                            if (specifyStart) {
                                while (true) {
                                    try {
                                        HashMap<String, String> dateinput = new InputForm(wW, wR)
                                                .withField("Date (2 digits)", true)
                                                .withField("Month (2 digits)", true)
                                                .withField("Year (4 digits)", true)
                                                .withTitle("Specify the item's start lifespan (for calculation)")
                                                .receiveInput();
                                        String stringDate = dateinput.get("Date (2 digits)") + "/"
                                                + dateinput.get("Month (2 digits)") + "/"
                                                + dateinput.get("Year (4 digits)");
                                        String pattern = "dd/MM/yyyy";
                                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                                        startLifespan = LocalDate.parse(stringDate, dateFormatter)
                                                .atStartOfDay(ZoneId.systemDefault()).toInstant();

                                    } catch (Exception e) {
                                        wW.write("Invalid date format.\n");
                                        continue;
                                    }
                                }
                            }
                            while (true) {
                                try {
                                    HashMap<String, String> dateinput = new InputForm(wW, wR)
                                            .withField("Date (2 digits)", true)
                                            .withField("Month (2 digits)", true)
                                            .withField("Year (4 digits)", true)
                                            .withTitle("Specify the item's end lifespan")
                                            .receiveInput();
                                    String stringDate = dateinput.get("Date (2 digits)") + "/"
                                            + dateinput.get("Month (2 digits)") + "/"
                                            + dateinput.get("Year (4 digits)");
                                    String pattern = "dd/MM/yyyy";
                                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                                    Instant endLifespan = LocalDate.parse(stringDate, dateFormatter)
                                            .atStartOfDay(ZoneId.systemDefault()).toInstant();
                                    if (startLifespan.isAfter(endLifespan)) {
                                        wW.write("Start date cannot be after end date.\n");
                                        continue;
                                    }
                                    if (!specifyStart) {
                                        startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                                                .toInstant();
                                    }
                                    builder = builder.withLifespanStart(startLifespan)
                                            .withLifespanSeconds(
                                                    endLifespan.getEpochSecond() - startLifespan.getEpochSecond());
                                    break;
                                } catch (Exception e) {
                                    wW.write("Invalid date format.\n");
                                    continue;
                                }
                            }

                        }

                        boolean isComposite = new Menu()
                                .withTitle("Is this item a composite?")
                                .withChoice("Y", "Yes")
                                .withChoice("N", "No")
                                .makeASelection(wW, wR).equals("Y");
                        if (isComposite) {
                            MaterialComposite composite = new MaterialComposite();
                            while (true) {
                                String choice = new Menu()
                                        .withTitle("Material Composite adder\n| Amount of materials in composite: "
                                                + composite.getMaterials().size())
                                        .withChoice("+", "Add material to composite")
                                        .withChoice("-", "Remove material from composite")
                                        .withChoice("F", "Finish")
                                        .makeASelection(wW, wR);
                                switch (choice) {
                                    case "+":
                                        while (true) {
                                            HashMap<String, String> result2 = new InputForm(wR, wW)
                                                    .withField("Template ID", true)
                                                    .withField("Amount (above 0)", true)
                                                    .withTitle("Add existing template material to composite")
                                                    .receiveInput();
                                            try {
                                                if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                                    throw new Exception();
                                                }
                                            } catch (Exception e) {
                                                wW.write("Amount has to be a valid positive number.");
                                                continue;
                                            }
                                            Optional<Material> getmat = Server.Server.templatematerials.materials()
                                                    .stream()
                                                    .filter(x -> result2.get("Template ID")
                                                            .equals(x.MaterialID().toString()))
                                                    .findFirst();
                                            if (!getmat.isPresent()) {
                                                wW.write("Invalid template ID.\n");
                                                break;
                                            }
                                            getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                            composite.addMaterial(getmat.get());
                                        }
                                        continue;
                                    case "-":
                                        while (true) {
                                            HashMap<String, String> result2 = new InputForm(wR, wW)
                                                    .withField("Template ID", true)
                                                    .withField("Amount (above 0)", true)
                                                    .withTitle("Remove existing template material in composite")
                                                    .receiveInput();
                                            try {
                                                if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                                    throw new Exception();
                                                }
                                            } catch (Exception e) {
                                                wW.write("Amount has to be a valid positive number.");
                                                continue;
                                            }
                                            Optional<Material> getmat = composite.materials()
                                                    .stream()
                                                    .filter(x -> result2.get("Template ID")
                                                            .equals(x.MaterialID().toString()))
                                                    .findFirst();
                                            if (!getmat.isPresent()) {
                                                wW.write("Invalid template ID.\n");
                                                break;
                                            }
                                            getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                            composite.removeMaterial(getmat.get());
                                        }
                                        continue;
                                    case "F":
                                        builder = builder.withComposite(composite);
                                        break;

                                    default:
                                        break;
                                }
                                break;

                            }
                        }

                        wW.write("Material ID: " + builder.build().MaterialID() + "\n");
                        Server.Server.templatematerials.addMaterial(builder.build());
                        Server.Server.SaveAll();

                        break;
                    }
                    continue;
                case "5":
                    while (true) {
                        HashMap<String, String> result = new InputForm(wR, wW)
                                .withField("Name", true)
                                .withField("Description", true)
                                .withField("Differentiator (optional)", false)
                                .withField("Value per Qty", true)
                                .withField("Quantity (above 0)", true)
                                .withField("Custom value (optional)", false)
                                .withTitle("Add item to stock by ID: Basic Data")
                                .receiveInput();
                        // #region Input validation
                        try {
                            if (Double.valueOf(result.get("Value per Qty")) <= 0) {
                                throw new Exception();
                            }

                        } catch (Exception e) {
                            wW.write("Value per Qty has to be a valid positive number.");
                            continue;
                        }
                        try {
                            if (Double.valueOf(result.get("Quantity (above 0)")) <= 0) {
                                throw new Exception();
                            }

                        } catch (Exception e) {
                            wW.write("Quantity has to be a valid positive number.");
                            continue;
                        }
                        try {
                            if (result.get("Custom value (optional)").length() > 0) {
                                if (Double.valueOf(result.get("Custom value (optional)")) <= 0) {
                                    throw new Exception();
                                }
                            }

                        } catch (Exception e) {
                            wW.write("Custom value has to be a valid positive number.");
                            continue;
                        }
                        // #endregion

                        MaterialBuilder builder = new MaterialBuilder()
                                .withName(result.get("Name"))
                                .withDescription(result.get("Description"))
                                .withDifferentiator(result.get("Differentiator (optional)"))
                                .withValuePerQty(Double.valueOf(result.get("Value per Qty")))
                                .withQuantity(Double.valueOf(result.get("Quantity (above 0)")));
                        if (result.get("Custom value (optional)").length() > 0) {
                            builder = builder.withOverrideValue(Double.valueOf(result.get("Custom value (optional)")));
                        }

                        boolean hasLifespan = new Menu()
                                .withTitle("Does this item have a lifespan?")
                                .withChoice("Y", "Yes")
                                .withChoice("N", "No")
                                .makeASelection(wW, wR).equals("Y");

                        if (hasLifespan) {
                            boolean specifyStart = new Menu()
                                    .withTitle("Specify the item's start lifespan?")
                                    .withChoice("Y", "Yes")
                                    .withChoice("N", "No (uses now)")
                                    .makeASelection(wW, wR).equals("Yes");

                            Instant startLifespan = Instant.now();
                            if (specifyStart) {
                                while (true) {
                                    try {
                                        HashMap<String, String> dateinput = new InputForm(wW, wR)
                                                .withField("Date (2 digits)", true)
                                                .withField("Month (2 digits)", true)
                                                .withField("Year (4 digits)", true)
                                                .withTitle("Specify the item's start lifespan")
                                                .receiveInput();
                                        String stringDate = dateinput.get("Date (2 digits)") + "/"
                                                + dateinput.get("Month (2 digits)") + "/"
                                                + dateinput.get("Year (4 digits)");
                                        String pattern = "dd/MM/yyyy";
                                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                                        startLifespan = LocalDate.parse(stringDate, dateFormatter)
                                                .atStartOfDay(ZoneId.systemDefault()).toInstant();

                                    } catch (Exception e) {
                                        wW.write("Invalid date format.\n");
                                        continue;
                                    }
                                }
                            }
                            while (true) {
                                try {
                                    HashMap<String, String> dateinput = new InputForm(wW, wR)
                                            .withField("Date (2 digits)", true)
                                            .withField("Month (2 digits)", true)
                                            .withField("Year (4 digits)", true)
                                            .withTitle("Specify the item's end lifespan")
                                            .receiveInput();
                                    String stringDate = dateinput.get("Date (2 digits)") + "/"
                                            + dateinput.get("Month (2 digits)") + "/"
                                            + dateinput.get("Year (4 digits)");
                                    String pattern = "dd/MM/yyyy";
                                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                                    Instant endLifespan = LocalDate.parse(stringDate, dateFormatter)
                                            .atStartOfDay(ZoneId.systemDefault()).toInstant();
                                    if (startLifespan.isAfter(endLifespan)) {
                                        wW.write("Start date cannot be after end date.\n");
                                        continue;
                                    }
                                    if (!specifyStart) {
                                        startLifespan = Instant.now();
                                    }
                                    builder = builder.withLifespanStart(startLifespan)
                                            .withLifespanSeconds(
                                                    endLifespan.getEpochSecond() - startLifespan.getEpochSecond());
                                    break;
                                } catch (Exception e) {
                                    wW.write("Invalid date format.\n");
                                    continue;
                                }
                            }

                        }
                        Server.Server.currentlystored.addMaterial(builder.build());
                        Server.Server.SaveAll();

                        break;
                    }
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
