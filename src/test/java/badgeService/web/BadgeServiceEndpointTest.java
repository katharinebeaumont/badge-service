package badgeService.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.equalTo;

import badgeService.attendee.Attendee;
import badgeService.attendee.AttendeeRepository;
import badgeService.event.Event;
import badgeService.event.EventFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import com.google.gson.Gson;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BadgeServiceEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EventFactory factory;

    private Attendee testAttendee;
    private Attendee testAttendee2;
    private String testAtendeeJson;
    private String eventData;

    @Before
    public void setup() {
        testAttendee = new Attendee("3423", "Katharine", "Beaumont", "k@k.com");
        testAttendee2 = new Attendee("3473", "Freddy", "Young", "f@y.com");
        attendeeRepository.save(Arrays.asList(testAttendee, testAttendee2));

        Gson gson = new Gson();
        testAtendeeJson = gson.toJson(testAttendee);

        eventData = gson.toJson(Arrays.asList(factory.factoryDevoxxUK()));
    }

    @Test
    public void getAttendee() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get( "/attendee?uuid=3423").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(testAtendeeJson)));
    }

    @Test
    public void getNonExistentAttendee() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/attendee?uuid=madeupId").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    //@Test
    //public void getAllAttendees() throws Exception {
      //  mvc.perform(MockMvcRequestBuilders.get("/offline").accept(MediaType.APPLICATION_JSON))
     //           .andExpect(status().is
    //}

    @Test
    public void getEvents() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/admin/api/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(eventData)));
    }
}