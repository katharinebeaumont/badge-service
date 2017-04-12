package badgeService.util;

import badgeService.attendee.Attendee;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
public class CSVAttendeeMapperTest {

    private String mapping = "Order no.:Uuid,First Name:FirstName,Surname:LastName,Email:Email";
    private String csvTitleLine = "Order no.,Order Date,First Name,Surname,Email,Quantity,Ticket Type,Order Type,Total Paid,Eventbrite Fees,Eventbrite Payment Processing,Attendee Status,Home Address 1,Home Address 2,Home City,County of Residence,Home Postcode,Home Country";


    @Test
    public void testCreateAttendeeEntityMapper() {
        //Arrange
        List<Long> expectedKeys = Arrays.asList(new Long(0), new Long(2), new Long(3), new Long(4));

        //Act
        CSVAttendeeMapper entityMapper = new CSVAttendeeMapper(mapping, csvTitleLine);

        HashMap<Long, String> indexToField = entityMapper.getIndexToEntityFieldMapping();
        //Assert
        for (Long key : expectedKeys) {
            assertTrue(indexToField.containsKey(key));
        }
        assertEquals("Uuid", indexToField.get(new Long(0)));
        assertEquals("FirstName", indexToField.get(new Long(2)));
        assertEquals("LastName", indexToField.get(new Long(3)));
        assertEquals("Email", indexToField.get(new Long(4)));

    }

    @Test
    public void testEntity() {
        //Arrange
        String attendeeDetails = "90811297,2017-01-27 15:51:40+00:00,Katharine,Beaumont,katharine@devoxx.co.uk,1,RSVP,Free Order,0.00,0.00,0.00,Attending,,,,,,\n";

        //Act
        CSVAttendeeMapper entityMapper = new CSVAttendeeMapper(mapping, csvTitleLine);
        Attendee attendee = entityMapper.createAttendee(attendeeDetails);

        //Assert
        assertEquals("90811297", attendee.getUuid().toString());
        assertEquals("Katharine", attendee.getFirstName());
        assertEquals("Beaumont", attendee.getLastName());
        assertEquals("Katharine Beaumont", attendee.getFullName());
        assertEquals("katharine@devoxx.co.uk", attendee.getEmail());

    }

}