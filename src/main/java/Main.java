import java.util.ArrayList;

class Main {
  public static void main(String[] args) {
    Table testtable = new Table();
    testtable.columnames.add("Column 1");
    testtable.columnames.add("Column ke-2");
    testtable.columnames.add("Column 3dddddddddddddddddddddddddddddddddddddddddddd");
    {
      // Row 1
      ArrayList<String> row1 = new ArrayList<String>();
      row1.add("11111 1336673462734273 222192369242137");
      row1.add("22222231d");
      row1.add("2222223123132132d1");
      testtable.rowsandcolumns.add(row1);
    }

    {
      // Row 2
      ArrayList<String> row1 = new ArrayList<String>();
      row1.add("11111 1336673462734273 222192");
      row1.add("2222223123132132d1");
      row1.add("2222223123132132d1");
      testtable.rowsandcolumns.add(row1);
    }

    {
      // Row 3
      ArrayList<String> row1 = new ArrayList<String>();
      row1.add("11111 133667346");
      row1.add("222d1 12313 21");
      row1.add("2222223123132132d1");
      testtable.rowsandcolumns.add(row1);
    }

    System.out.print(Table.getPrintedTable(testtable,true));


  }
}