import com.github.davidmoten.rx.jdbc.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by anshuman.tripat on 7/26/17.
 */
public class PersonContext {

    private static PersonContext instance = null;
    private static final Object lock = new Object();
    private static List<Person> people = new ArrayList<>();
    private Database syncDb = Database.from("jdbc:mysql://localhost/test","root","root");
    private Database asyncDb = Database.from("jdbc:mysql://localhost/test","root","root").asynchronous();


    private PersonContext() {
    }

    public static PersonContext getInstance() {

        if (instance == null) {
            synchronized (lock) {
                if (instance == null)
                    instance = new PersonContext();
                return instance;
            }
        }

        return instance;

    }

    public static List<Person> getPeople() {
        return Collections.synchronizedList(people);
    }

    public static void setPeople(List<Person> people) {
        PersonContext.people = people;
    }

    public Database getSyncDb() {
        return syncDb;
    }

    public Database getAsyncDb() {
        return asyncDb;
    }

}
