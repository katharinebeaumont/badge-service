package badgeService.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by katharine on 11/04/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class EventFactoryTest {

    @Autowired
    private EventFactory factory;

    static {
        System.setProperty("event.key","DevoxxUK2017");
        System.setProperty("event.name","Devoxx UK");
        System.setProperty("event.imageUrl","http://www.devoxx.co.uk/wp-content/uploads/2015/08/devoxx-letters-1.jpg");
        System.setProperty("event.begin","2017-05-11T07:00:00Z");
        System.setProperty("event.end","2017-05-12T18:00:00Z");
        System.setProperty("event.location","Business Design Centre, 52 Upper Street, London, N1 0QH");
        System.setProperty("event.apiVersion","17");
    }

    @Test
    public void factoryDevoxxUK() throws Exception {
        //Arrange
        Event devoxxUK = factory.factoryDevoxxUK();

        assertEquals(17, devoxxUK.getApiVersion());
        assertEquals("Devoxx UK", devoxxUK.getName());
    }

}