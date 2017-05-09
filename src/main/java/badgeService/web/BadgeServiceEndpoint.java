package badgeService.web;

import badgeService.attendee.Attendee;
import badgeService.attendee.AttendeeRepository;
import badgeService.event.Event;
import badgeService.event.EventFactory;
import badgeService.event.EventSecurity;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

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

            ticketCodeToAttendee.put(uuid, EventSecurity.encrypt(uuid+"/"+uuid, toJson(attendee)));
        }

        String attendeesJson = toJson(ticketCodeToAttendee);
        return new ResponseEntity<>(attendeesJson,HttpStatus.OK);
    }

    @RequestMapping("/admin/api/events")
    public ResponseEntity getEventId() {
        Event devoxx = factory.factoryDevoxxUK();
        String devoxxJson = toJson(Arrays.asList(devoxx));
        return new ResponseEntity<>(devoxxJson,HttpStatus.OK);
    }

    @RequestMapping("/attendees/all")
    public ResponseEntity<List<Attendee>> getAllAttendeesJSON() {
        return ResponseEntity.ok(attendeeRepository.findAll());
    }

    @RequestMapping("/attendees/{id}/qr-code")
    public ResponseEntity<byte[]> getQrCode(@PathVariable("id") String attendeeId) {
        Predicate<String> empty = String::isEmpty;
        return Optional.ofNullable(attendeeId)
                .map(String::trim)
                .filter(empty.negate())
                .flatMap(s -> Optional.ofNullable(attendeeRepository.findOne(s)))
                .flatMap(attendee -> createQRCode(attendee.getUuid()))
                .map(bytes -> ResponseEntity.ok().header("Content-Type", "image/png").body(bytes))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public static String toJson(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    private static Optional<byte[]> createQRCode(String text) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 200, 200, hintMap);
            MatrixToImageWriter.writeToStream(matrix, "png", baos);
            return Optional.of(baos.toByteArray());
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
