/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;  
import java.nio.charset.StandardCharsets; 
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException; 
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
  
public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static String readText() {
		String input;
		while (true) {
			try {
				System.out.print("Ticketmaster_DB $: ");
				input = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage() + '\n');
				continue;
			}
		}
		return input;
	}

	public static int readInt() {
		int input;
		while (true) {
			try {
				System.out.print("Ticketmaster_DB $: ");
				input = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage() + '\n');
				continue;
			} 
		}
        return input;
	}

	public static long readLong() {
		long input;
		while (true) {
			try {
				System.out.print("Ticketmaster_DB $: ");
				input = Long.parseLong(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage() + '\n');
				continue;
			} 
		}
        return input;
	}
	
	public static byte[] getSHA(String input) throws NoSuchAlgorithmException 
    {  
        // Static getInstance method is called with hashing SHA  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        // digest() method called  
        // to calculate message digest of an input  
        // and return array of byte 
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    } 
    
    public static String toHexString(byte[] hash) 
    { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    } 

	public static void AddUser(Ticketmaster esql){//1
		String email, lname, fname, pwd;
		long phone;
		System.out.print("Please enter your email: \n");
		email = readText();
		System.out.print("Please enter your first name: \n");
		fname = readText();
		System.out.print("Please enter your last name: \n");
		lname = readText();
		System.out.print("Please enter your phone number 1234567890: \n");
		phone = readLong();
		System.out.print("Please enter your password: \n");
		pwd = readText();
		try {
			System.out.print("Encrypting password...\n");
			pwd = toHexString(getSHA(pwd));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown for incorrect algorithm: " + e.getMessage() + '\n');
		}
	
		String query = String.format(
                "INSERT INTO USERS(EMAIL,LNAME,FNAME,PHONE,PWD)"
                        + " VALUES('%s','%s','%s','%s','%s') ", email, lname, fname, phone, pwd);
 
		try {
			esql.executeUpdate(query);
			System.out.print("The following User has been added to the Database:" + '\n');
			System.out.print("Email: " + email + '\n');
			System.out.print("Last Name: " + lname + '\n');
			System.out.print("First Name: " + fname + '\n');
			System.out.print("Phone: " + phone + '\n');
			System.out.print("Password: " + pwd + '\n');
		} catch(Exception e) {
			System.out.print("An error occurred. Please reenter the User to the database. Your error message is " + e.getMessage() + '\n');
		}
	}
	
	public static void AddBooking(Ticketmaster esql){//2
		String status, bdate, btime, btz, email;
		String bdatetime;
		int bid, seats, sid;
		
		System.out.print("Please enter your booking ID: \n");
		bid = readInt();
		ArrayList<String> validStatus = new ArrayList<String>();
        validStatus.add("Pending");
        validStatus.add("Paid");
        validStatus.add("Cancelled");
		System.out.print("Please enter the status of your booking (Paid/Pending/Cancelled): \n");
		status = readText();
		boolean checkStatus = false;
		for (int i = 0; i < validStatus.size(); i++) {
			if (status.contains(validStatus.get(i))) {
				checkStatus = true;
			}
		}
		while (!checkStatus) {
			System.out.print("Your input is invalid! Please reenter the status of your booking (Paid/Pending/Cancelled): \n");
			status = readText();
			for (int i = 0; i < validStatus.size(); i++) {
				if (status.contains(validStatus.get(i))) {
					checkStatus = true;
				}
			}		
		}	
		System.out.print("Please enter the date of your booking (MM/DD/YYYY): \n");
		bdate = readText();
		System.out.print("Please enter the time of your booking (HH:MM (military time)): \n");
		btime = readText();
		bdatetime = bdate + " " + btime;
		System.out.print("Please enter the number of seats you have booked: \n");
		seats = readInt();
		System.out.print("Please enter the email that belongs to your booking: \n");
		email = readText();
		System.out.print("Please enter the show ID of your booking: \n");
		sid = readInt();
		String query = String.format(
                "INSERT INTO Bookings(BID, STATUS, BDATETIME, SEATS, SID, EMAIL)"
                        + " VALUES(%s,'%s','%s', %s, %s, '%s') ", bid, status, bdatetime, seats, sid, email);
		try {
			esql.executeUpdate(query);
			System.out.print("The following Booking has been added to the Database:" + '\n');
			System.out.print("Bid: " + bid + '\n');
			System.out.print("Status: " + status + '\n');
			System.out.print("Booking Date & Time: " + bdatetime + '\n');
			System.out.print("Seats: " + seats + '\n');
			System.out.print("Sid: " + sid + '\n');
			System.out.print("Email: " + email + '\n');
		} catch(Exception e) {
			System.out.print("An error occurred. Please reenter the Booking to the database. Your error message is " + e.getMessage() + '\n');
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		int tid, cid;
		System.out.print("Please enter the Theater you would like to Add a movie showing too. \n");
		System.out.print("Please enter the Theater's tid: \n");
		tid = readInt();
		System.out.print("Please enter the Theater's cid: \n");
		cid = readInt();
		
		String checkTheater = "SELECT tid FROM Theaters WHERE tid = " + tid + " AND cid = " + cid;
		try {
			while (esql.executeQueryAndPrintResult(checkTheater) == 0) {
				do {
					try {
						System.out.print("Theater does not exist. Please enter a valid cid and tid. \n");
						System.out.print("tid: \n");
						tid = readInt();
						System.out.print("cid: \n");
						cid = readInt();	
					} catch(Exception e) {
						System.out.print("Invalid input. Your exception is " + e.getMessage() + '\n');
					}
				}while(true);
			}
		} catch (Exception e) {
			System.out.print("Invalid input. Your exception is " + e.getMessage() + '\n');
		}
	
		int mvid;
		String title, rdate, country;

		System.out.print("Please enter the movie you would like to add. \n");
		System.out.print("Please enter the movie's mvid: \n");
		mvid = readInt();
		System.out.print("Please enter the movie's title: \n");
		title = readText();
		System.out.print("Please enter the movie's release date (MM/DD/YYYY): \n");
		rdate = readText();
		System.out.print("Please enter the movie's country: \n");
		country = readText();

		String query = String.format("INSERT INTO Movies(MVID, TITLE, RDATE, COUNTRY)"
		+ " VALUES(%s,'%s','%s', '%s') ", mvid, title, rdate, country);
		try {
			esql.executeUpdate(query);
			System.out.print("The following Movie has been added to the Database:" + '\n');
			System.out.print("Movie ID: " + mvid + '\n');
			System.out.print("Title: " + title + '\n');
			System.out.print("Release Date: " + rdate + '\n');
			System.out.print("Country: " + country + '\n');
		} catch(Exception e) {
			System.out.print("An error occurred. Please reenter the Movie to the database. Your error message is " + e.getMessage() + '\n');
		}

		int sid;
		String sdate, sttime, edtime;

		System.out.print("Please enter the showing for the movie. \n");
		System.out.print("Please enter the showing's sid: \n");
		sid = readInt();
		System.out.print("Please enter the showing's premier date (DD/MM/YYYY): \n");
		sdate = readText();
		System.out.print("Please enter the showing's start time (HH:MM (military time)): \n");
		sttime = readText();
		System.out.print("Please enter the showing's end time (HH:MM (military time)): \n");
		edtime = readText();

		String query2 = String.format("INSERT INTO Shows(SID, MVID, SDATE, STTIME, EDTIME)" 
		+ " VALUES(%s, %s, '%s','%s','%s') ", sid, mvid, sdate, sttime, edtime);
		try {
			esql.executeUpdate(query2);
			System.out.print("The following Show has been added to the Database:" + '\n');
			System.out.print("Show ID: " + sid + '\n');
			System.out.print("Movie ID: " + mvid + '\n');
			System.out.print("Premier Date: " + sdate + '\n');
			System.out.print("Start time: " + sttime + '\n');
			System.out.print("End time: " + edtime + '\n');
		} catch(Exception e) {
			System.out.print("An error occurred. Please reenter the Show to the database. Your error message is " + e.getMessage() + '\n');
		}
	}	
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		String query = "UPDATE Bookings SET status = 'Cancelled' WHERE status = 'Pending'";	 
		try {
			esql.executeUpdate(query);
			System.out.print("All Pending bookings are now have the status of Cancelled. \n");
		}
		catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
	}
	//EMAIL --> BID --> SSID.     
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		System.out.print("Please enter the email of your booking: \n");
		String email = readText();
		String bidQuery = String.format("SELECT bid FROM Bookings WHERE email = '%s'", email);
		String priceQuery = String.format("SELECT price FROM Showseats WHERE bid = (%s)", bidQuery);
		String sidQuery = String.format("SELECT sid FROM Showseats WHERE bid = (%s)", bidQuery);
		String findEmpty = String.format("SELECT ssid FROM Showseats WHERE sid = (%s) AND bid IS NULL AND price = (%s);", sidQuery, priceQuery);
		try {
			List<List<String>> availableSeats = esql.executeQueryAndReturnResult(findEmpty);
			System.out.print("Please select from the available seats you would like to change your seat too: \n");
			if (availableSeats.size() == 0) {
				System.out.print("No seats are available from the same theater at the same price: \n");
			}
			else {
				for (int i=0; i < availableSeats.size(); i++) {
					for (int j=0; j < ((availableSeats.get(i)).size()); j++) {
						System.out.print("Seat: " + (availableSeats.get(i)).get(j) + "\n");
					}
				}
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
		int newSeat = readInt();
		try {
			List<List<String>> bid = esql.executeQueryAndReturnResult(bidQuery);
			String oldBid = String.format("UPDATE Showseats SET bid = NULL WHERE bid = (%s)", bidQuery);
			String query = String.format("UPDATE Showseats SET bid = '%s' WHERE ssid = '%s'", (bid.get(0)).get(0), newSeat); 
			try {
				esql.executeUpdate(oldBid);
			} catch(Exception e) {
				System.out.print("An error occured. Your error message is " + e.getMessage());
			}
			try {
				esql.executeUpdate(query);
				System.out.print("Your booking has been updated. \n");
				System.out.print("Your new seat(s) is now: " + newSeat + "\n");
			} catch(Exception e) {
				System.out.print("An error occured. Your error message is " + e.getMessage());
			}
		} catch(Exception e) {
			System.out.print("An error occured. Your error message is " + e.getMessage());
		}
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		int bid;
		System.out.print("Please enter the bid of the Cancelled booking. \n" );
		bid = readInt();
		String query = String.format("DELETE FROM Payments WHERE bid = %s", bid);
		try {
			esql.executeUpdate(query);
			System.out.print("Payment with associated bid of " + bid + " has been removed. \n");
		}
		catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		String query = "DELETE FROM Bookings WHERE status = 'Cancelled'";
		try {
			esql.executeUpdate(query);
			System.out.print("Cancelled bookings have been removed from the database. \n");
		}
		catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		String date;
		System.out.print("Please enter the date which you would like to clear all existing shows for. \n");
		date = readText(); 
		String showQuery = String.format("SELECT sid FROM Shows WHERE sdate = '%s'", date);
		String bidQuery = String.format("SELECT bid FROM Shows WHERE sid IN (%s)", showQuery);
		String deletePlays = String.format("DELETE FROM Plays WHERE sid IN (%s)", showQuery);
		String deleteSeat = String.format("DELETE FROM ShowSeats WHERE sid IN (%s)", showQuery);
		String deletePayment = String.format("DELETE FROM Payments WHERE bid IN (%s)", bidQuery);
		String deleteBooking = String.format("DELETE FROM Bookings WHERE sid IN (%s)", showQuery);
		String query = String.format("DELETE FROM Shows WHERE sdate = '%s'", date);	
		try {
			esql.executeUpdate(deletePlays);
			try {
				esql.executeUpdate(deleteSeat);
				try {
					esql.executeUpdate(deletePayment);
					try {
						esql.executeUpdate(deleteBooking);
						try {
							esql.executeUpdate(query);
							System.out.print("Shows on the " + date + " have been removed from the database. \n");
						} catch(Exception e) {
							System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
						}
					} catch(Exception e) {
						System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');	
					}
				} catch(Exception e) {
					System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
				}
			} catch(Exception e) {
				System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		System.out.print("Please input the SID of your inquired show: \n");
		int sid;
		sid = readInt();
		String findTheaters = String.format("SELECT tname FROM Theaters WHERE tid IN (SELECT tid FROM Plays WHERE sid = %s);", sid);
		try {
			List<List<String>> theaters = esql.executeQueryAndReturnResult(findTheaters);
			System.out.print("Theaters currently playing your specified show: \n");
			if (theaters.size() == 0) {
				System.out.print("No theaters are currently playing your specified show: \n");
			}
			else {
				for (int i=0; i < theaters.size(); i++) {
					for (int j=0; j < ((theaters.get(i)).size()); j++) {
						System.out.print("Theater: " + (theaters.get(i)).get(j) + "\n");
					}
				}
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		String date, time;
		System.out.print("Please input the date and time you would like to see for all available shows: \n");
		System.out.print("Date (MM/DD/YYYY): \n");
		date = readText();
		System.out.print("Time (HH:MM (Military Time)): \n");
		time = readText();
		
		String findShows = String.format("SELECT title FROM Movies WHERE mvid IN (SELECT mvid FROM Shows WHERE sdate = '%s' AND sttime = '%s');", date, time);
		try {
			List<List<String>> shows = esql.executeQueryAndReturnResult(findShows);
			System.out.print("Shows playing on " + date + " " + time + ": \n");
			if (shows.size() == 0) {
				System.out.print("No shows match your given query. \n");
			}
			else {
				for (int i = 0; i < shows.size(); i++) {
					for (int j = 0; j < (shows.get(i)).size(); j++) {
						System.out.print("Showing: " + (shows.get(i)).get(j) + "\n");
					}
				}
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		String query = "SELECT title FROM Movies WHERE title LIKE '%Love%' AND rdate > '1/1/2011';";
		try {
			List<List<String>> movies = esql.executeQueryAndReturnResult(query);
			System.out.print("Movies containing 'Love' and released after '2010' are: \n");
			if (movies.size() == 0) {
				System.out.print("No movies match the given query. \n");
			}
			else {
				for (int i = 0; i < movies.size(); i++) {
					for (int j = 0; j < (movies.get(i)).size(); j++) {
						System.out.print("Movie: " + (movies.get(i)).get(j) + "\n");
					}
				}
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		String query = "SELECT fname, lname, email FROM Users WHERE email IN (SELECT email from Bookings WHERE status = 'Pending')";
		try {
			List<List<String>> pendingUsers = esql.executeQueryAndReturnResult(query);
			System.out.print("Users with pending bookings: \n");
			if (pendingUsers.size() == 0) {
				System.out.print("No users match the given query. \n");
			}
			else {
				for (int i = 0; i < pendingUsers.size(); i++) {
					for (int j = 0; j < (pendingUsers.get(i)).size(); j++) {
						if (j == (pendingUsers.get(i).size() - 1)) {
							System.out.print((pendingUsers.get(i)).get(j) + "\n");
						}
						else {
							System.out.print((pendingUsers.get(i)).get(j) + " ");
						}
						
					}
				}
			}
		} catch(Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}

	}
	// CID --> TID --> SID --> MVID --> PRINT
	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		System.out.print("Please enter the movie ID that you are inquiring?: \n");
		int movie = readInt();
		System.out.print("Please enter the cinema ID you would like to search at?: \n");
		int cinema = readInt();
		System.out.print("Between what dates?: \n");
		String date1 = readText();
		String date2 = readText();
		String subQuery = String.format("SELECT tid FROM Theaters WHERE cid = %s", cinema);
		String subQuery2 = String.format("SELECT sid FROM Plays WHERE tid = (%s)", subQuery);
		String Query3 = String.format("SELECT sdate, sttime FROM Shows WHERE sid = (%s) AND sdate BETWEEN '%s' AND '%s' AND mvid = '%s';", subQuery2, date1, date2, movie);
		String movieQuery = String.format("SELECT title, duration FROM Movies WHERE mvid = %s", movie);
		try {
			List<List<String>> movies = esql.executeQueryAndReturnResult(movieQuery);
			if (movies.size() == 0) {
				System.out.print("No movies match the given query. \n");
			}
			else {
				for (int i = 0; i < movies.size(); i++) {
					for (int j = 0; j < (movies.get(i)).size(); j++) {
						if (j == (movies.get(i).size() - 1)) {
							System.out.print((movies.get(i)).get(j) + " (Duration)\n");
						}
						else {
							System.out.print((movies.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}
		try {
			List<List<String>> shows = esql.executeQueryAndReturnResult(Query3);
			if (shows.size() == 0) {
				System.out.print("No Shows match the given query. \n");
			}
			else {
				for (int i = 0; i < shows.size(); i++) {
					for (int j = 0; j < (shows.get(i)).size(); j++) {
						if (j == (shows.get(i).size() - 1)) {
							System.out.print((shows.get(i)).get(j) + "\n");
						}
						else {
							System.out.print((shows.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');
		}
	}
	//EMAIL --> BOOKING(S) --> SID -->
	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		System.out.print("Please enter the email of the user you would like to view the booking info for: \n");
		String user = readText();
		String bidQuery = String.format("SELECT bid from Bookings WHERE email = '%s'", user);
		String showSeats = String.format("SELECT ssid from ShowSeats WHERE bid = (%s)", bidQuery);
		String subQuery = String.format("SELECT sid FROM Bookings WHERE email = '%s'", user);
		String showQuery = String.format("SELECT sdate, sttime FROM Shows WHERE sid = (%s)", subQuery);
		String subQuery2 = String.format("SELECT mvid FROM Shows WHERE sid = (%s)", subQuery);
		String movieQuery = String.format("SELECT title FROM Movies WHERE mvid = (%s)", subQuery2);
		String theaterQuery = String.format("SELECT tid FROM Plays WHERE sid = (%s)", subQuery);
		String thNameQuery = String.format("SELECT tname FROM Theaters WHERE tid = (%s)", theaterQuery);
		String cinemaSeat = String.format("SELECT sno FROM CinemaSeats WHERE tid = (%s)", theaterQuery);

		try {
			List<List<String>> movies = esql.executeQueryAndReturnResult(movieQuery);
			if (movies.size() == 0) {
				System.out.print("No movies booked for the user. \n");
			}
			else {
				for (int i = 0; i < movies.size(); i++) {
					for (int j = 0; j < (movies.get(i)).size(); j++) {
						if (j == (movies.get(i).size() - 1)) {
							System.out.print((movies.get(i)).get(j) +  "\n");
						}
						else {
							System.out.print((movies.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}

		try {
			List<List<String>> shows = esql.executeQueryAndReturnResult(showQuery);
			if (shows.size() == 0) {
				System.out.print("No Shows booked for the user. \n");
			}
			else {
				for (int i = 0; i < shows.size(); i++) {
					for (int j = 0; j < (shows.get(i)).size(); j++) {
						if (j == (shows.get(i).size() - 1)) {
							System.out.print((shows.get(i)).get(j) +  "\n");
						}
						else {
							System.out.print((shows.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}

		try {
			List<List<String>> theaters = esql.executeQueryAndReturnResult(thNameQuery);
			if (theaters.size() == 0) {
				System.out.print("No Theaters booked for the user. \n");
			}
			else {
				for (int i = 0; i < theaters.size(); i++) {
					for (int j = 0; j < (theaters.get(i)).size(); j++) {
						if (j == (theaters.get(i).size() - 1)) {
							System.out.print("Theater Name: " + (theaters.get(i)).get(j) +  "\n");
						}
						else {
							System.out.print((theaters.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}

		try {
			List<List<String>> seats = esql.executeQueryAndReturnResult(showSeats);
			if (seats.size() == 0) {
				System.out.print("No seats booked for the user. \n");
			}
			else {
				for (int i = 0; i < seats.size(); i++) {
					for (int j = 0; j < (seats.get(i)).size(); j++) {
						if (j == (seats.get(i).size() - 1)) {
							System.out.print("Cinema Seat number: " + (seats.get(i)).get(j) +  "\n");
						}
						else {
							System.out.print((seats.get(i)).get(j) + ", ");
						}
						
					}
				}
			}
		} catch (Exception e) {
			System.out.print("An error occurred. Your error message is " + e.getMessage() + " Please try again." + '\n');			
		}
	}
	
}