package badgeService.util;

import badgeService.attendee.Attendee;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by katharinevoxxed on 09/02/2017.
 * Not sure I really need this as well as the mouthful that is AttendeeDataInjectorIntegrationTest
 */
public class CSVParserTest {

    private String mapping = "Order no.:Uuid,First Name:FirstName,Surname:LastName,Email:Email";

    @Test
    public void testParser() {
        //Arrange
        CSVParser parser = new CSVParser("test-report.csv", mapping);

        //Act
        List<Attendee> attendees = parser.parseEntitesFromCSV();

        //Assert
        for (Attendee att : attendees) {
            System.out.print(att.toString());
        }
        assertEquals(7, attendees.size());
    }

}