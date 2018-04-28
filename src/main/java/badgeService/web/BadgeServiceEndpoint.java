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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RestController
public class BadgeServiceEndpoint {

    private final AttendeeRepository attendeeRepository;
    private final EventFactory factory;

    private final ConfigurableEnvironment environment;
    private final Set<String> categoriesWithCheckInDate;

    private static final String ALFIO_TIMESTAMP_HEADER = "Alfio-TIME";

    @Autowired
    public BadgeServiceEndpoint(AttendeeRepository attendeeRepository, EventFactory factory, ConfigurableEnvironment environment) {
        this.attendeeRepository = attendeeRepository;
        this.factory = factory;
        this.environment = environment;

        //
        categoriesWithCheckInDate = StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .filter(s -> s.startsWith("attendee.checkInDate"))
                .map(s -> {
                    String a = s.replace("attendee.checkInDate.", "");
                    return a.substring(0, Math.max(a.indexOf(".to"), a.indexOf(".from")));
                })
                .collect(Collectors.toSet());
        //

    }


    /* core api */
    @RequestMapping("/admin/api/events")
    public ResponseEntity getEventId() {
        Event devoxx = factory.factoryDevoxxUK();
        String devoxxJson = toJson(Collections.singletonList(devoxx));
        return new ResponseEntity<>(devoxxJson,HttpStatus.OK);
    }

    @RequestMapping(value = "/admin/api/check-in/{eventName}/offline-identifiers", method = GET)
    public List<Integer> getOfflineIdentifiers(@PathVariable("eventName") String eventName,
                                               @RequestParam(value = "changedSince", required = false) Long changedSince,
                                               HttpServletResponse resp) {
        long since = changedSince == null ? 0 : changedSince;

        Long nextChangedSince = attendeeRepository.findNextChangedSince(since);
        List<Integer> ids = attendeeRepository.findIds(since);

        resp.setHeader(ALFIO_TIMESTAMP_HEADER, Long.toString(nextChangedSince));
        return ids;
    }

    @RequestMapping(value = "/admin/api/check-in/{eventName}/label-layout", method = GET)
    public void getLabelLayout(HttpServletResponse res) throws IOException {
        String s = "{\"qrCode\":{\"additionalInfo\":[\"company\"],\"infoSeparator\":\"::\"},\"content\":{\"thirdRow\":[\"company\"]},\"general\":{\"printPartialID\":true}}";
        res.setContentType("application/json");
        res.getWriter().write(s);
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
            info.put("status", ticket.getStatus());
            info.put("uuid", ticket.getUuid());
            info.put("category", ticket.getTicketCategory());
            if(ticket.getCompany() != null) {
                info.put("additionalInfoJson", new Gson().toJson(Collections.singletonMap("company", ticket.getCompany())));
            }
            //
            String categoryKey = getCategoryKey(ticket.getTicketCategory());
            if(categoriesWithCheckInDate.contains(categoryKey)) {
                String prefixKey = "attendee.checkInDate."+categoryKey;
                info.put("validCheckInFrom", Long.toString(OffsetDateTime.parse(environment.getProperty(prefixKey + ".from")).toEpochSecond()));
                info.put("validCheckInTo", Long.toString(OffsetDateTime.parse(environment.getProperty(prefixKey + ".to")).toEpochSecond()));
            }
            String uuid = ticket.getUuid();

            return EventSecurity.encrypt(uuid+"/"+uuid, toJson(info));
        };
        return foundAttendee
                .stream()
                .collect(Collectors.toMap(keyExtractor, encryptedBody));
    }

    private static String getCategoryKey(String category) {
        return category.replaceAll("\\s", "").toLowerCase();
    }

    @RequestMapping(value = "/admin/api/check-in/event/{eventName}/ticket/{ticketIdentifier:.*}", method = POST)
    public TicketAndCheckInResult checkIn(@PathVariable("eventName") String eventName,
                                          @PathVariable("ticketIdentifier") String ticketIdentifier,
                                          @RequestBody TicketCode ticketCode,
                                          @RequestParam(value = "offlineUser", required = false) String offlineUser) {
        return new TicketAndCheckInResult(new Ticket(), new DefaultCheckInResult(CheckInStatus.SUCCESS, ""));
    }

    public static class TicketCode {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class Ticket {
    }

    public static class TicketAndCheckInResult {
        private final Ticket ticket;
        private final CheckInResult result;

        public TicketAndCheckInResult(Ticket ticket, CheckInResult checkInResult) {
            this.ticket = ticket;
            this.result = checkInResult;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public CheckInResult getResult() {
            return result;
        }

    }

    public interface CheckInResult {
        CheckInStatus getStatus();
    }

    public enum CheckInStatus {
        EVENT_NOT_FOUND, TICKET_NOT_FOUND, EMPTY_TICKET_CODE, INVALID_TICKET_CODE, INVALID_TICKET_STATE, ALREADY_CHECK_IN, MUST_PAY, OK_READY_TO_BE_CHECKED_IN, SUCCESS, INVALID_TICKET_CATEGORY_CHECK_IN_DATE
    }

    public static class DefaultCheckInResult implements CheckInResult {
        private final CheckInStatus status;
        private final String message;

        public DefaultCheckInResult(CheckInStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public CheckInStatus getStatus() {
            return status;
        }
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
