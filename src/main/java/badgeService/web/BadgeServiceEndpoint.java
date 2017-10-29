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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RestController
public class BadgeServiceEndpoint {

    private final AttendeeRepository attendeeRepository;
    private EventFactory factory;

    private static final String ALFIO_TIMESTAMP_HEADER = "Alfio-TIME";

    @Autowired
    public BadgeServiceEndpoint(AttendeeRepository attendeeRepository, EventFactory factory) {
        this.attendeeRepository = attendeeRepository;
        this.factory = factory;
    }


    /* core api */
    @RequestMapping("/admin/api/events")
    public ResponseEntity getEventId() {
        Event devoxx = factory.factoryDevoxxUK();
        String devoxxJson = toJson(Arrays.asList(devoxx));
        return new ResponseEntity<>(devoxxJson,HttpStatus.OK);
    }

    @RequestMapping(value = "/admin/api/check-in/{eventName}/offline-identifiers", method = GET)
    public List<Integer> getOfflineIdentifiers(@PathVariable("eventName") String eventName,
                                               @RequestParam(value = "changedSince", required = false) Long changedSince,
                                               HttpServletResponse resp) {
        Date since = changedSince == null ? new Date(0) : DateUtils.addSeconds(new Date(changedSince), -1);

        //FIXME add since support for delta loading
        List<Integer> ids = attendeeRepository.findIds(since);

        resp.setHeader(ALFIO_TIMESTAMP_HEADER, Long.toString(new Date().getTime()));
        return ids;
    }

    @RequestMapping(value = "/admin/api/check-in/{eventName}/offline", method = POST)
    public Map<String, String> getOfflineEncryptedInfo(@PathVariable("eventName") String eventName, @RequestBody List<Integer> ids) {

        List<Attendee> foundAttendee = attendeeRepository.findAllWithIds(ids);

        return encryptAttendees(foundAttendee);
    }

    private Map<String, String> encryptAttendees(List<Attendee> foundAttendee) {
        Function<Attendee, String> keyExtractor = ticket -> DigestUtils.sha256Hex(ticket.getUuid());

        Function<Attendee, String> encryptedBody = ticket -> {
            Map<String, String> info = new HashMap<>();
            info.put("firstName", ticket.getFirstName());
            info.put("lastName", ticket.getLastName());
            info.put("fullName", ticket.getFullName());
            info.put("email", ticket.getEmail());
            info.put("status", ticket.getStatus().toString());
            info.put("uuid", ticket.getUuid());
            info.put("category", ticket.getTicketCategory());
            //
            String uuid = ticket.getUuid();

            return EventSecurity.encrypt(uuid+"/"+uuid, toJson(info));
        };
        return foundAttendee
                .stream()
                .collect(Collectors.toMap(keyExtractor, encryptedBody));
    }

    /** **/


    /** non core api **/

    @RequestMapping("/attendee")
    public ResponseEntity getAttendee(@RequestParam(value="uuid") String id) {
        Attendee attendee = attendeeRepository.findOne(id);
        if (attendee == null) return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(toJson(attendee),HttpStatus.OK);
    }

    @RequestMapping("/attendees/all")
    public ResponseEntity<List<Attendee>> getAllAttendeesJSON() {
        return ResponseEntity.ok(attendeeRepository.findAll());
    }

    @RequestMapping("/offline")
    public Map<String, String> getAllAttendees() {
        List<Attendee> attendees = attendeeRepository.findAll();
        return encryptAttendees(attendees);
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
