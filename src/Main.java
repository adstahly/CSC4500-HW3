
import java.sql.*;
import java.util.*;
public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("Can't load driver");
        }

        try {
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
                case 5:
                    runQuery5(con, scan);
                    break;
                case 6:
                    runQuery6(con, scan);
                    break;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
            while (e != null) {
                System.out.println("Message: " + e.getMessage());
                e = e.getNextException();
            }
        } catch (Exception e) {
            System.out.println("Other Error");
        }
    }

    private static void printMenu() {
        System.out.println("1. Show the Director with the Most Movies Directed");
        System.out.println("2. Show all Movies not Directed by a Chosen Director");
        System.out.println("3. Show all Directors who have Directed Each Genre at Least Once");
        System.out.println("4. Show the Number of Movies Directed By a Chosen Director and Whether it's Below or Above the Average Number of Movies Directed by All Directors");
        System.out.println("5. Show a Chosen Genre, the Directors who have worked with the Chosen Genre at Least Once, and Average Rating of All Movies with the Chosen Genre");
        System.out.println("6. Show All Directors who have made at Least One Movie in Every Genre if the Movie is Greater than or Equal to a Chosen Rating");
    }

    private static void runQuery1(Connection con) {
        String query = """
                SELECT
                    fname,
                    lname,
                    totalMovies,
                    CASE\s
                        WHEN totalMovies >= 3 THEN 'Highly Active Director'
                        WHEN totalMovies = 2 THEN 'Moderately Active Director'
                        ELSE 'Less Active Director'
                    END AS activityLevel
                FROM (
                    SELECT
                        D.fname,
                        D.lname,
                        COUNT(DISTINCT M.movie_ID) AS totalMovies
                    FROM
                        Directors D
                    JOIN
                        Movie_Directors MD ON D.director_ID = MD.director_ID
                    JOIN
                        Movies M ON MD.movie_ID = M.movie_ID
                    GROUP BY
                        D.director_ID, D.fname, D.lname
                ) AS DirectorCounts;
                """;
        PreparedStatement stmt = con.prepareStatement(query);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while (result.next()) {
            System.out.println("Mid: " + result.getString("M.movie_ID"));
            System.out.println("Title: " + result.getString("M.title"));
        }
        con.close();
    }
    private static void runQuery2(Connection con, Scanner scan) {
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
        stmt.setString(1, v_fname);
        stmt.setString(2, v_lname);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while (result.next()) {
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
        while (result.next()) {
            System.out.println("First Name: " + result.getString("M.fname"));
            System.out.println("Last Name: " + result.getString("M.lname"));
        }
    }

    private static void runQuery4(Connection con, Scanner scan) {
        String query = "WITH mtable AS ("
                + " SELECT  director_id, COUNT(*) AS moviecount"
                + " FROM  Movie_Directors"
                + "  GROUP BY director_id"
                + " ),"
                + " AllDirectors AS ("
                + " SELECT  d.director_id, mt.moviecount, AVG(COALESCE(mt.moviecount, 0)) OVER () AS allmovieavg"
                + " FROM Directors d "
                + " LEFT JOIN mtable mt ON d.director_id = mt.director_id"
                + " )"
                + " SELECT COALESCE(ad.moviecount, 0) AS moviecount,"
                + "  CASE"
                + "  WHEN ad.allmovieavg < COALESCE(ad.moviecount, 0) THEN 'Above average'"
                + "  WHEN ad.allmovieavg > COALESCE(ad.moviecount, 0) THEN 'Below average'"
                + "  ELSE 'Average'"
                + " END AS Comparison"
                + " FROM Directors d LEFT JOIN AllDirectors ad ON d.director_id = ad.director_id"
                + " WHERE d.fname = ? AND d.lname = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while (result.next()) {
            System.out.println("First Name: " + result.getString("M.movie_ID"));
            System.out.println("Last Name: " + result.getString("M.title"));
        }
    }

    public static void runQuery5(Connection con, Scanner scan) {
        String v_generename;
        String query = """
                SELECT
                    G.genrename,
                    COUNT(DISTINCT MD.director_ID) AS directors_in_genre,
                    ROUND(AVG(M.rating), 2)        AS avg_rating,
                    CASE
                        WHEN AVG(M.rating) >= ? THEN 'Highly Rated'
                        ELSE 'Not Highly Rated'
                    END AS rating_label
                FROM Genre G
                JOIN Movie_Genres    MG ON G.genre_ID  = MG.genre_ID
                JOIN Movie_Directors MD ON MG.movie_ID = MD.movie_ID
                JOIN Movies          M  ON M.movie_ID  = MG.movie_ID
                WHERE (? IS NULL OR G.genrename = ?)
                GROUP BY G.genre_ID, G.genrename
                ORDER BY directors_in_genre DESC, G.genrename;
                """;
        System.out.println("Enter A Genre or Leave Black to See Them All:");
        v_generename = scan.nextLine();
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, v_generename);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while (result.next()) {
            System.out.println("Genre: " + result.getString("G.genrename"));
            System.out.println("Directors In Genre: " + result.getString("directors_in_genre"));
            System.out.println("Average Rating of All Movies in Genre: " + result.getString("avg_rating"));
        }
    }

    public static void runQuery6(Connection con, Scanner scan) {
        String v_rating;
        String query = """
SELECT D.director_ID, D.fname, D.lname
        FROM Directors D
        WHERE NOT EXISTS (
                SELECT 1
                FROM Genre G
                WHERE NOT EXISTS (
                        SELECT 1
                        FROM Movie_Directors MD
                        JOIN Movie_Genres   MG ON MD.movie_ID = MG.movie_ID
                        JOIN Movies         M  ON M.movie_ID  = MG.movie_ID
                        WHERE MD.director_ID = D.director_ID
                        AND MG.genre_ID    = G.genre_ID
                        AND M.rating      >= ?
                )
        )
        ORDER BY D.lname, D.fname;
""";
        System.out.println("Enter A Genre or Leave Black to See Them All:");
        v_rating = scan.nextLine();
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, v_rating);
        ResultSet result = stmt.executeQuery();
        System.out.println("Processing Results");
        while (result.next()) {
            System.out.println("Director ID: " + result.getString("D.director_ID"));
            System.out.println("First Name: " + result.getString("D.fname"));
            System.out.println("Last Name: " + result.getString("D.lname"));
        }
    }
}


