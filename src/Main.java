
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
            boolean exit = false;
            while (!exit) {
                printMenu();
                System.out.print("Enter your Choice: ");
                int choice = scan.nextInt();
                scan.nextLine();
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
                        runQuery5(con);
                        break;
                    case 6:
                        runQuery6(con, scan);
                        break;
                    case 7:
                        exit = true;
                        System.out.println("Exiting...");
                        con.close();
                        break;
                    default:
                        System.out.println("Invalid Input. Please enter a number between 1-7");
                        break;
                }

            }
        } catch (SQLException e) {
            SQLException throwables = e;
            System.out.println(throwables.getMessage() + " Can't connect to database");
            while (throwables != null) {
                System.out.println("Message: " + throwables.getMessage());
                throwables = throwables.getNextException();
            }
        } catch (Exception e) {
            System.out.println("Other Error");
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Show How Many Movies Each director Has Directed and Classify Them as Highly Active, Moderately Active, or Less Active Based on the Total Number of Movies");
        System.out.println("2. Show all Movies not Directed by a Chosen Director");
        System.out.println("3. Show all Directors who have Directed Every Genre at Least Twice");
        System.out.println("4. Show the Number of Movies Directed By a Chosen Director and Whether it's Below or Above the Average Number of Movies Directed by All Directors");
        System.out.println("5. Show the Number of Directors who have Worked with the Each Genre and Average Rating of All Movies For Each Genre");
        System.out.println("6. Show all Directors who have made at Least One Movie in Every Genre if the Movie is Greater than or Equal to a Chosen Rating");
        System.out.println("7. Exit the Program");
    }

    private static void runQuery1(Connection con) {
        String query = """
                SELECT
                    fname,
                    lname,
                    totalMovies,
                    CASE
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
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            System.out.println("Processing Results");
            while (result.next()) {
                System.out.println("First Name: " + result.getString("fname"));
                System.out.println("Last Name: " + result.getString("lname"));
                System.out.println("Total Movies: " + result.getString("totalMovies"));
                System.out.println("Activity: " + result.getString("activityLevel"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }

    private static void runQuery2(Connection con, Scanner scan) {
        String v_fname;
        String v_lname;
        System.out.println("Enter A Director's First Name:");
        v_fname = scan.nextLine();
        System.out.println("Enter A Director's Last Name:");
        v_lname = scan.nextLine();
        String query = """
                SELECT
                 M.movie_ID,
                 M.title,
                 M.rating,
                 M.runtime
                FROM
                 Movies M
                WHERE
                 M.movie_ID NOT IN (
                  SELECT
                   MD.movie_ID
                  FROM
                   Movie_Directors MD
                  JOIN
                   Directors D ON MD.director_ID = D.director_ID
                  WHERE
                    D.fname = 'Leo' AND D.lname = 'Martinez'
                );
                """;
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            stmt.setString(1, v_fname);
            stmt.setString(2, v_lname);
            System.out.println("Processing Results");
            boolean resultsFound = false;
            while (result.next()) {
                resultsFound = true;
                System.out.println("Movie ID: " + result.getString("M.movie_ID"));
                System.out.println("Title: " + result.getString("M.title"));
                System.out.println("Rating: " + result.getString("M.rating"));
                System.out.println("Runtime: " + result.getString("M.runtime"));
            }

            if (!resultsFound) {
                System.out.println("No Movies Found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }

    private static void runQuery3(Connection con) {
        String query = """
                SELECT
                    d.fname,
                    d.lname
                FROM
                    Directors d
                WHERE
                    NOT EXISTS (
                        SELECT
                            g.genre_ID
                        FROM
                            Genre g
                        WHERE
                            g.genre_ID NOT IN (
                                SELECT
                                    mg.genre_ID
                                FROM
                                    Movie_Genres mg
                                JOIN
                                    Movie_Directors md ON mg.movie_ID = md.movie_ID
                                WHERE
                                    md.director_ID = d.director_ID
                                GROUP BY
                                    mg.genre_ID
                                HAVING
                                    COUNT(mg.movie_ID) >= 2
                            )
                    );
                """;
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            System.out.println("Processing Results");
            boolean resultsFound = false;
            while (result.next()) {
                resultsFound = true;
                System.out.println("First Name: " + result.getString("d.fname"));
                System.out.println("Last Name: " + result.getString("d.lname"));
            }

            if (!resultsFound) {
                System.out.println("No Directors Found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }

    private static void runQuery4(Connection con, Scanner scan) {
        String v_fname;
        String v_lname;
        System.out.println("Enter A Director's First Name:");
        v_fname = scan.nextLine();
        System.out.println("Enter A Director's Last Name:");
        v_lname = scan.nextLine();
        String query = """
                WITH mtable AS (
                 SELECT  director_id, COUNT(*) AS moviecount
                 FROM  Movie_Directors
                 GROUP BY director_id
                ),
                AllDirectors AS (
                 SELECT  d.director_id, mt.moviecount, AVG(COALESCE(mt.moviecount, 0)) OVER () AS allmovieavg"
                 FROM Directors d 
                 LEFT JOIN mtable mt ON d.director_id = mt.director_id"
                )
                SELECT COALESCE(ad.moviecount, 0) AS moviecount,"
                 CASE
                  WHEN ad.allmovieavg < COALESCE(ad.moviecount, 0) THEN 'Above average'"
                  WHEN ad.allmovieavg > COALESCE(ad.moviecount, 0) THEN 'Below average'"
                  ELSE 'Average'
                 END AS Comparison
                FROM Directors d LEFT JOIN AllDirectors ad ON d.director_id = ad.director_id
                WHERE d.fname = ? AND d.lname = ?
                """;
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            stmt.setString(1, v_fname);
            stmt.setString(2, v_lname);
            System.out.println("Processing Results");
            while (result.next()) {
                System.out.println("Movies Directed: " + result.getString("moviecount"));
                System.out.println("Comparison to Average Movies Directed: " + result.getString("Comparison"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }

    public static void runQuery5(Connection con) {
        String query = """
                SELECT
                    g.genrename,
                    COUNT(DISTINCT md.director_ID) AS directors_in_genre,
                    ROUND(AVG(m.rating), 2) AS avg_rating,
                    CASE
                        WHEN AVG(m.rating) >= 8.0 THEN 'Highly Rated'
                        ELSE 'Not Highly Rated'
                    END rating_label
                FROM Genre g
                JOIN Movie_Genres mg ON g.genre_ID = mg.genre_ID
                JOIN Movie_Directors md ON mg.movie_ID = md.movie_ID
                JOIN Movies m ON m.movie_ID = mg.movie_ID
                GROUP BY g.genre_ID, g.genrename
                ORDER BY directors_in_genre DESC, g.genrename;
                """;
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            System.out.println("Processing Results");
            while (result.next()) {
                System.out.println("\nGenre: " + result.getString("G.genrename"));
                System.out.println("Directors In Genre: " + result.getString("directors_in_genre"));
                System.out.println("Average Rating of All Movies in Genre: " + result.getString("avg_rating"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }

    public static void runQuery6(Connection con, Scanner scan) {
        String v_rating;
        System.out.println("Enter A Rating:");
        v_rating = scan.nextLine();
        String query = """
                SELECT d.director_ID, d.fname, d.lname
                FROM Directors d
                WHERE NOT EXISTS (
                 SELECT g.genre_ID
                 FROM Genre g
                 WHERE g.genre_ID NOT IN (
                  SELECT mg.genre_ID
                  FROM Movie_Directors md
                  JOIN Movie_Genres mg ON md.movie_ID = mg.movie_ID
                  JOIN Movies m ON m.movie_ID = mg.movie_ID
                  WHERE md.director_ID = d.director_ID
                  AND m.rating >= ?
                 )
                )
                ORDER BY d.lname, d.fname;
                """;
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet result = stmt.executeQuery()) {
            stmt.setString(1, v_rating);
            System.out.println("Processing Results");
            boolean resultsFound = false;
            while (result.next()) {
                resultsFound = true;
                System.out.println("Director ID: " + result.getString("D.director_ID"));
                System.out.println("First Name: " + result.getString("D.fname"));
                System.out.println("Last Name: " + result.getString("D.lname"));
            }

            if (!resultsFound) {
                System.out.println("No Directors Found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " Can't connect to database");
        }
    }
}


