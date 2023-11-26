package UI;
import java.util.ArrayList;

public class Table {
    public ArrayList<String> columnames = new ArrayList<String>();
    public ArrayList<ArrayList<String>> rowsandcolumns = new ArrayList<ArrayList<String>>();

    public void addRow(ArrayList<String> row) {
        rowsandcolumns.add(row);
    }

    public void addRow(String... vara) {
        ArrayList<String> res = new ArrayList<>();
        for (String iterable_element : vara) {
            res.add(iterable_element);
        }
        rowsandcolumns.add(res);
    }

    public void addColumnNames(String... names) {
        for (String element : names) {
            columnames.add(element);
        }
    }

    public static String getPrintedTable(Table table, boolean withheader) {
        ArrayList<ArrayList<String>> tempbuilder = new ArrayList<ArrayList<String>>();
        if (withheader) {
            tempbuilder.add(table.columnames);
        }
        tempbuilder.addAll(table.rowsandcolumns);

        // First we calculate the column widths
        ArrayList<Integer> columnwidth = new ArrayList<Integer>();
        // for all rows
        for (ArrayList<String> column : tempbuilder) {
            for (int i = 0; i < column.size(); i++) {
                try {
                    columnwidth.get(i);
                    columnwidth.set(i, Math.max(columnwidth.get(i), column.get(i).length()));
                } catch (Exception e) {
                    columnwidth.add(column.get(i).length());
                }

            }
        }

        StringBuilder builtable = new StringBuilder();

        // First the header of the table.
        /// +--------+-------------+
        builtable.append("+");
        for (Integer integer : columnwidth) {
            builtable.append(stringGenerator("-", integer));
            builtable.append("+");
        }
        builtable.append("\n");

        if (withheader) {
            // Second, the columnnames.
            /// |dkadjsdj|ddddddd331331|
            builtable.append("|");
            for (int i = 0; i < table.columnames.size(); i++) {
                builtable.append(stringOverlayer(stringGenerator(" ", columnwidth.get(i)), table.columnames.get(i)));
                builtable.append("|");
            }
            builtable.append("\n");

            // Third, another header
            /// +--------+-------------+
            builtable.append("+");
            for (Integer integer : columnwidth) {
                builtable.append(stringGenerator("-", integer));
                builtable.append("+");
            }
            builtable.append("\n");
        }
        // Fourth, now its time to render the items.
        // for all rows
        for (ArrayList<String> columns : table.rowsandcolumns) {
            builtable.append("|");
            for (int i = 0; i < columns.size(); i++) {
                builtable.append(stringOverlayer(stringGenerator(" ", columnwidth.get(i)), columns.get(i)));
                builtable.append("|");
            }
            builtable.append("\n");
        }

        // Fifth, now rendering the footer.
        /// +--------+-------------+
        builtable.append("+");
        for (Integer integer : columnwidth) {
            builtable.append(stringGenerator("-", integer));
            builtable.append("+");
        }
        builtable.append("\n");

        return builtable.toString();

    }

    public static String stringOverlayer(String base, String overlay) {
        char[] work = base.toCharArray();
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (char character : overlay.toCharArray()) {
            work[i] = character;
            i++;
        }
        for (char c : work) {
            result.append(c);
        }
        return result.toString();
    }

    public static String stringGenerator(String txt, Integer amount) {
        String result = "";
        for (int i = 0; i < amount; i++) {
            result += txt;
        }
        return result;
    }
}