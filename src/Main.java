
import java.sql.*;
import java.util.*;
public class Main {
    public static void main(String[] args){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (Exception e){
            System.out.println("Can't load driver");
        }

        try{
            System.out.println("Starting Connection........");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://161.35.177.175:3306/hw3astahly01", "astahly01", "p1");
            System.out.println("Connection Established");
            Scanner scan = new Scanner(System.in);
            int choice = scan.nextInt();
            scan.nextLine();
            printMenu();
            switch (choice) {
                case 1:
                    runQuery1(con);
                    break;
                case 2:
                    runQuery2(con, scan);
                    break;
                case 3:
                    runQuery3(con);
                    break;
                case 4:
                    runQuery4(con, scan);
                    break;
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage() + " Can't connect to database");
            while(e!=null){
                System.out.println("Message: "+e.getMessage());
                e= e.getNextException();
            }
        }
        catch (Exception e){
            System.out.println("Other Error");
        }
    }

    private static void printMenu() {
        System.out.println("1. Show the Director with the Most Movies Directed");
        System.out.println("2. Show all Movies not Directed by a Chosen Director");
        System.out.println("3. Show all Directors who have Directed Each Genre at Least Once");
        System.out.println("4. Show the Average Number of Movies Directed By a Chosed Director and if the average is below or above the average movies directed by all directors");
    }

    private static void runQuery2(Connection con, scan) {
        String v_fname;
        String v_lname;
        System.out.println("Enter A Director's First Name:");
        v_fname = scan.nextLine();
        System.out.println("Enter A Director's Last Name:");
        v_lname = scan.nextLine();
        String query = "select M.movie_ID, M.title"
                + " from Movies M"
                + " where M.movie_ID NOT IN ("
                + "SELECT MD.movie_ID"
                + " FROM Movie_Directors MD"
                + " JOIN Directors D ON MD.director_ID = D.director_ID"
                + " WHERE D.fname = ? AND D.lname = ?"
                + " )";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1,v_fname);
        stmt.setString(2,v_lname);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while(result.next()) {
            System.out.println("Mid: " + result.getString("M.movie_ID"));
            System.out.println("Title: " + result.getString("M.title"));
        }
        con.close();
    }

    private static void runQuery3(Connection con) {
        String query = "SELECT fname, lname"
                + " from Directors d"
                + " where not exists ("
                + "SELECT genre_ID"
                + " from Genre g"
                + " Except"
                + " SELECT genre_ID"
                + " from Movie_Genres mg JOIN Movie_Directors md on"
                + "mg.movie_ID = md.movie_ID"
                + " where d.director_ID = md.director_ID";
        PreparedStatement stmt = con.prepareStatement(query);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while(result.next()) {
            System.out.println("First Name: " + result.getString("M.movie_ID"));
            System.out.println("Last Name: " + result.getString("M.title"));
        }
    }
}


