import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by anshuman.tripat on 7/27/17.
 */

public class RxQueryTest {

    private Timestamp startStamp;
    private Timestamp endStamp;

    QueryWorker worker;


    @Before
    public void setUp(){
        startStamp = Timestamp.valueOf(LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0));
        endStamp = Timestamp.valueOf(startStamp.toLocalDateTime().plusMonths(1));
    }


    @Test
    public void testQuery(){

        ConcurrentHashMap<String, Timestamp> range = new ConcurrentHashMap<>();
        range.put("from", startStamp);
        range.put("to", endStamp);
        worker = Mockito.spy(new QueryWorker(range));
        List<Person> actualPeople = worker.querySingle();

        Assert.assertArrayEquals(actualPeople.toArray(), new QueryWorker(range).querySingle().toArray());
    }

    @After
    public void tearDown(){
        System.out.println("Test Ended");
    }
}
