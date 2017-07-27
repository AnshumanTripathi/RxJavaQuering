import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by anshuman.tripat on 7/19/17.
 */
public class Person extends Object {


    private int id, age;
    private String firstName, lastName;
    private LocalDateTime workPeriod;

    public Person() {
    }

    public Person(int id, int age, String firstName, String lastName, LocalDateTime workPeriod) {
        this.id = id;
        this.age = age;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workPeriod = workPeriod;
    }



    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(LocalDateTime workPeriod) {
        this.workPeriod = workPeriod;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", age=" + age +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", workPeriod=" + workPeriod +
                '}';
    }
}
