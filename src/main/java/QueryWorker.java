import com.github.davidmoten.rx.jdbc.Database;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;

/**
 * Created by anshuman.tripat on 7/26/17.
 */
public class QueryWorker implements Callable<List<Person>> {

    private HashMap<String, Timestamp> rangeMap;


    public QueryWorker(HashMap<String, Timestamp> rangeMap) {
        this.rangeMap = rangeMap;
    }

    @Override
    public List<Person> call() throws Exception {
        List<Person> people = new ArrayList<>();
        Database db = PersonContext.getInstance().getSyncDb();
        db.select("select * from person where work_period between ? and ?")
                .parameter(rangeMap.get("from"))
                .parameter(rangeMap.get("to"))
                .get(resultSet -> new Person(               //Get person from result set
                        resultSet.getInt("id"),
                        resultSet.getInt("age"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getTimestamp("work_period").toLocalDateTime()))
                .collect((Func0<List<Person>>) ArrayList::new,      //Collect all results to a List
                        List::add)
                .subscribe(
                        people1 -> {
                            people.addAll(people1);             // Add current results to the main result
                            db.close();             // End Database connection
                        },
                        e -> System.out.println("Error" + e),
                        () -> System.out.println("Completed adding people for " + rangeMap.get("from").
                                toLocalDateTime().getMonth().name())
                );
        return people;
    }

}
