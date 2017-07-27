import com.github.davidmoten.rx.jdbc.Database;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;

/**
 * Created by anshuman.tripat on 7/26/17.
 */
public class QueryWorker implements Runnable {

    private ConcurrentHashMap<String, Timestamp> rangeMap;
    Phaser ph;


    public QueryWorker(ConcurrentHashMap<String, Timestamp> rangeMap, Phaser ph) {
        this.rangeMap = rangeMap;
        this.ph = ph;

    }

    @Override
    public void run() {
        ph.register();

        List<Person> people = PersonContext.getPeople();
        Database db = PersonContext.getInstance().getAsyncDb();

        //Query between each monthly period as an Observable
        db.select("select * from person where work_period between :from and :to ;")
                .parameters(rangeMap)
                .get(resultSet -> new Person(               //Get person from result set
                        resultSet.getInt("id"),
                        resultSet.getInt("age"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getTimestamp("work_period").toLocalDateTime()))
                .collect((Func0<List<Person>>) ArrayList::new,      //Collect all results to a List
                        (personList, person) -> {
                            System.out.println("Adding person: " + person.toString());
                            personList.add(person);
                        }).subscribeOn(Schedulers.newThread())
                .subscribe(
                        people1 -> {
                            people.addAll(people1);             // Add current results to the main result
                            PersonContext.setPeople(people);
                            db.close();             // End Database connection
                        },
                        e -> System.out.println("Error" + e),
                        () -> {
                            System.out.println("Completed adding everyone");
                            ph.arriveAndDeregister();       //Deregister Phaser on completion of current query thread
                            ph.arriveAndDeregister();
                        }
                );
    }
}
