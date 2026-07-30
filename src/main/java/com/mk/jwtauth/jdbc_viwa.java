package com.mk.jwtauth;
import java.sql.*;
import java.util.TimeZone;

public class jdbc_viwa {
    public static void main() throws SQLException {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        String url = "jdbc:postgresql://localhost/learning_platform";
        Connection conn = DriverManager.getConnection(url,"mohit","hello");

//        createTable(conn);
//        insertRecords(conn);

        scrollable(conn);
        updatable(conn);
    }

    public static void updatable(Connection conn) throws SQLException {

        Statement st1 = conn.createStatement(
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE
        );
        String readq = "select * from products";
        ResultSet srs = st1.executeQuery(readq);
        
        srs.first();
        printRow(srs);
        srs.updateInt("quantity",7);
        srs.updateRow();
        printRow(srs);
        
        srs.absolute(3);
        printRow(srs);
        srs.updateDouble("price",1999);
        srs.updateRow();
        printRow(srs);
        
        srs.absolute(6);
        printRow(srs);
        srs.updateInt("quantity",4);
        srs.updateDouble("price",37999);
        srs.updateRow();
        printRow(srs);
        

    }

    public static void scrollable(Connection conn) throws SQLException {

        Statement st1 = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        );
        String readq = "select * from products";
        ResultSet urs = st1.executeQuery(readq);

        urs.first();
        printRow(urs);

        urs.last();
        printRow(urs);

        urs.absolute(3);
        printRow(urs);

        urs.relative(2);
        printRow(urs);

        urs.next();
        printRow(urs);

        urs.previous();
        urs.previous();
        printRow(urs);
        System.out.println("\n ==============end of slrollable====================\n");
        
        urs.close();

    }

    public static void printRow(ResultSet rs) throws SQLException {
        String p = "Product-"+rs.getInt("pid")+" : ( "+rs.getString("pname")+", price: "+rs.getDouble("price")+", quantity: "+rs.getInt("quantity")+" )";
        System.out.println(p);
    }

    public static void insertRecords(Connection conn) throws SQLException {


        String[] pnames = {"laptop","mouse","keyboard","monitor","motherboard","graphicscard","processer","10w air cooler","liqued cooler","800w power supply"};
        int[] quantities = {2,12,3,4,9,11,17,29,4,1,16,23,8,19,27};
        double[] prices = {74999,530,2199,12999,14999,29999,22999,1239,5799,4899};

        String query = "insert into products (pname,price,quantity) values (?,?,?);";
        PreparedStatement pstmt = conn.prepareStatement(query);

        for (int i=0;i<pnames.length;i++){
            pstmt.setString(1,pnames[i]);
            pstmt.setInt(3,quantities[i]);
            pstmt.setDouble(2,prices[i]);
            pstmt.addBatch();
        }
        int arr[] = pstmt.executeBatch();
        pstmt.close();

    }

    public static void createTable(Connection conn) throws SQLException {

        String ctable = "CREATE TABLE products (  pid SERIAL PRIMARY KEY, pname VARCHAR(200) NOT NULL, price DOUBLE PRECISION NOT NULL,  quantity INT NOT NULL );";

        Statement stmt = conn.createStatement();
        stmt.execute(ctable);
        stmt.close();
    }
}
