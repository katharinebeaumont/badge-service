package badgeService.attendee;

import badgeService.util.CSVParser;
import badgeService.util.EventPathProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@Component
public class AttendeeDataInjector implements CommandLineRunner {

    private final AttendeeRepository attendeeRepository;
    private final EventPathProperties properties;
    private final Environment environment;
    private final RestTemplate restTemplate;

    @Autowired
    public AttendeeDataInjector(AttendeeRepository attendeeRepository, EventPathProperties properties, Environment environment) {
        this.attendeeRepository = attendeeRepository;
        this.properties = properties;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void run(String... strings) throws Exception {
        String source = environment.getProperty("attendee.datasource");
        if("csv".equals(source)) {
            generateAndSaveAttendees();
        } else if ("remote".equals(source)){
            loadAttendeeFromRemote(environment.getProperty("attendee.datasource.url"),
                    environment.getProperty("attendee.datasource.header.name"),
                    environment.getProperty("attendee.datasource.header.value"));
        }
    }

    //TODO add job and save timestamp, so we can discard the older one
    private void loadAttendeeFromRemote(String url, String headerName, String headerValue) {
        HttpHeaders h = new HttpHeaders();
        h.set(headerName, headerValue);

        ResponseEntity<List<RemoteAttendee>> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(h), new ParameterizedTypeReference<List<RemoteAttendee>>() {});
        List<Attendee> attendees = res.getBody().stream().map(remoteAttendee -> new Attendee(remoteAttendee.getRegistrationCode(), remoteAttendee.getFirstName(), remoteAttendee.getLastName(), remoteAttendee.getEmail())).collect(Collectors.toList());
        attendeeRepository.save(attendees);
        Stream.of((attendeeRepository.findAll())).forEach(attendee -> System.out.print(attendee.toString()));
    }

    public void generateAndSaveAttendees() {
        CSVParser<Attendee> csvParser = new CSVParser(properties.getPath(), properties.getAttendeeTitleMapping());
        List<Attendee> attendees = csvParser.parseEntitesFromCSV();
        attendeeRepository.save(attendees);
        Stream.of((attendeeRepository.findAll())).forEach(attendee -> System.out.print(attendee.toString()));
    }

}