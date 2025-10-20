
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
                    "jdbc:mysql://161.35.177.175:3306/jdbctest", "testuser", "testuser");
            System.out.println("Connection Established");

            // get user input
            String v_major;
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter major");
            v_major = scan.nextLine();
            String query = "select s.studentid, studentmajor, courseid, grade"
                    + " from Student s, Enroll e"
                    + " where s.studentid = e.studentid"
                    + " and studentmajor = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1,v_major);
            ResultSet result = stmt.executeQuery();
            System.out.println("Processing Results");
            while(result.next()) {
                System.out.println("Sid " + result.getString("s.studentid"));
                System.out.println("Cid " + result.getString("courseid"));
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
}


