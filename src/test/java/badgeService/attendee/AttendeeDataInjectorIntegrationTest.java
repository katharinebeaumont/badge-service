package badgeService.attendee;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class AttendeeDataInjectorIntegrationTest {

    static {
        System.setProperty("eventReport.path","test-report.csv");
        System.setProperty("eventReport.attendeeReportMapping","Order no.:Uuid,First Name:FirstName,Surname:LastName,Email:Email");
    }

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Test
    public void testGenerateAndSaveAttendees() {
        //Act
        List<Attendee> savedAttendees = attendeeRepository.findAll();

        //Assert
        assertEquals(7, savedAttendees.size());
    }

}
