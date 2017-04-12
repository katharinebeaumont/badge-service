package badgeService.attendee;

import badgeService.util.CSVParser;
import badgeService.util.EventPathProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@Component
public class AttendeeDataInjector implements CommandLineRunner {

    private final AttendeeRepository attendeeRepository;
    private final EventPathProperties properties;

    @Autowired
    public AttendeeDataInjector(AttendeeRepository attendeeRepository, EventPathProperties properties) {
        this.attendeeRepository = attendeeRepository;
        this.properties = properties;
    }

    @Override
    public void run(String... strings) throws Exception {
        generateAndSaveAttendees();
    }

    public void generateAndSaveAttendees() {
        CSVParser<Attendee> csvParser = new CSVParser(properties.getPath(), properties.getAttendeeTitleMapping());
        List<Attendee> attendees = csvParser.parseEntitesFromCSV();
        attendeeRepository.save(attendees);
        Stream.of((attendeeRepository.findAll())).forEach(attendee -> System.out.print(attendee.toString()));
    }

}