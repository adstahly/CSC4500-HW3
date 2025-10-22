
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

            // get user input
            String v_fname;
            String v_lname;
            Scanner scan = new Scanner(System.in);
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
        System.out.println("3. Show all Movies not Directed by a Chosen Director");
    }
}


