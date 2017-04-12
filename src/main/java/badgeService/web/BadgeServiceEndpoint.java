package badgeService.web;

import badgeService.attendee.Attendee;
import badgeService.attendee.AttendeeRepository;
import badgeService.event.Event;
import badgeService.event.EventFactory;
import badgeService.event.EventSecurity;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RestController
public class BadgeServiceEndpoint {

    private final AttendeeRepository attendeeRepository;
    private EventFactory factory;

    @Autowired
    public BadgeServiceEndpoint(AttendeeRepository attendeeRepository, EventFactory factory) {
        this.attendeeRepository = attendeeRepository;
        this.factory = factory;
    }

    @RequestMapping("/attendee")
    public ResponseEntity getAttendee(@RequestParam(value="uuid") String id) {
        Attendee attendee = attendeeRepository.findOne(id);
        if (attendee == null) return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(toJson(attendee),HttpStatus.OK);
    }


    @RequestMapping("/offline")
    public ResponseEntity getAllAttendees() {
        List<Attendee> attendees = attendeeRepository.findAll();
        HashMap ticketCodeToAttendee = new HashMap<String, Attendee>();

        for (Attendee attendee: attendees) {
            String uuid = attendee.getUuid();

            ticketCodeToAttendee.put(uuid, EventSecurity.encrypt(uuid, toJson(attendee)));
        }

        String attendeesJson = toJson(ticketCodeToAttendee);
        return new ResponseEntity<>(attendeesJson,HttpStatus.OK);
    }

    @RequestMapping("/resource")
    public ResponseEntity getEventId() {
        Event devoxx = factory.factoryDevoxxUK();
        String devoxxJson = toJson(Arrays.asList(devoxx));
        return new ResponseEntity<>(devoxxJson,HttpStatus.OK);
    }

    public static String toJson(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }
}
