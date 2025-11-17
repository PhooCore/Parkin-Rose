package modele.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ZoneTest.class,
    UsagerTest.class,
    StationnementTest.class,
    ParkingTest.class,
    PaiementTest.class
})
public class AllTests {
   
}