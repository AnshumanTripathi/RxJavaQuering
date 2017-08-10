import com.github.davidmoten.rx.jdbc.Database;
import rx.Observable;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by anshuman.tripat on 7/14/17.
 */
public class RxJDBCExperiments {

    public static void main(String args[]) throws IOException {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        int numOfMonths = 1;
        int fromYear = 0;
        //Thread pool
        ExecutorService threadPool = Executors.newFixedThreadPool(12);
        //Result of the query
        List<Person> people = new ArrayList<>();

        try {
            // Input from user.
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter Y for yearly reports, M for monthly reports: ");
            String selectedOption = sc.nextLine();

            if (selectedOption.equalsIgnoreCase("M")) {
                System.out.println("Enter number the number of months: ");
                numOfMonths = sc.nextInt();
            } else if (selectedOption.equalsIgnoreCase("Y")) {
                System.out.println("Enter the year: ");
                fromYear = sc.nextInt();
            } else {
                System.out.println("Wrong option");
                System.exit(-1);
            }
            if (selectedOption.equalsIgnoreCase("Y") ||
                    (selectedOption.equalsIgnoreCase("M") && numOfMonths > 3)) {
                //Divide the query in a period of months. The range will be decided based on the user input
                startDate = LocalDateTime.of(fromYear, Month.JANUARY, 1, 0, 0);
                endDate = startDate.plusYears(1);
                List<QueryWorker> workers = new ArrayList<>();
                while (startDate.isBefore(endDate)) {
                    HashMap<String, Timestamp> range = new HashMap<>();
                    range.put("from", Timestamp.valueOf(startDate));
                    startDate = startDate.plusMonths(1);
                    range.put("to", Timestamp.valueOf(startDate));
                    workers.add(new QueryWorker(range));
                }
                List<Future<List<Person>>> peopleFuture =  threadPool.invokeAll(workers);
                peopleFuture.forEach(listFuture -> {
                    try {
                        PersonContext.getPeople().addAll(listFuture.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                threadPool.shutdown();
            } else {
                Database db = Database.from("jdbc:mysql://localhost/test");
                System.out.println("Generating monthly report.....");
                //Generate Monthly periods floor rounded to hours
                endDate = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
                startDate = endDate.minusMonths(numOfMonths).truncatedTo(ChronoUnit.HOURS);
                HashMap<String, Timestamp> range = new HashMap<>();
                range.put("from", Timestamp.valueOf(startDate));
                range.put("to", Timestamp.valueOf(endDate));
                Observable<Person> dbObservable = Observable.defer(() ->
                        db.select("select * from person where work_period between :from and :to ;")
                                .parameters(range)
                                .get(resultSet -> new Person(
                                        resultSet.getInt("id"),
                                        resultSet.getInt("age"),
                                        resultSet.getString("first_name"),
                                        resultSet.getString("last_name"),
                                        resultSet.getTimestamp("work_period").toLocalDateTime())));
                dbObservable.subscribe(
                        person -> {
                            //Fetch all records and add to result set
                            people.add(person);
                            db.close();
                        },
                        e -> System.out.println("Error" + e),
                        () -> System.out.println("Query Completed")
                );
            }
            //Output all the results
            System.out.println("Presenting all people");
            PersonContext.getPeople().forEach(value -> System.out.println(value.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}