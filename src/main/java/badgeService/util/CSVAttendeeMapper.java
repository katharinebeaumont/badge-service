package badgeService.util;

import badgeService.attendee.Attendee;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by katharinevoxxed on 09/02/2017.
 * OK. This is awful.
 */
public class CSVAttendeeMapper extends CSVEntityMapper {

    private HashMap<Long, String> indexToEntityFieldMapping = new HashMap<>();
    private CSVParser parser = new CSVParserBuilder().build();

    public CSVAttendeeMapper (String attendeeReportMapping, String csvTitleLine) {
        HashMap titleToEntityFieldMapping = getTitleToEntityFieldMapping(attendeeReportMapping);
        this.generateIndexToEntityFieldMapping(titleToEntityFieldMapping, csvTitleLine);
    }

    public HashMap getTitleToEntityFieldMapping(String titleToEntityField) {

        HashMap titleToEntityFieldMapping = new HashMap();
        Arrays.stream(titleToEntityField.split(","))
                .map(s -> s.split(":")) //gives 2 values, key,value
                .forEach(pair -> titleToEntityFieldMapping.put(pair[0],pair[1]));
        return titleToEntityFieldMapping;
    }

    public void generateIndexToEntityFieldMapping(HashMap titleToEntityFieldMapping, String csvTitleLine) {
        List<String> titles = Arrays.asList(safeParseLine(csvTitleLine));
        for (String title : titles) {
            if (titleToEntityFieldMapping.containsKey(title)) {
                indexToEntityFieldMapping.put((long) titles.indexOf(title), (String)titleToEntityFieldMapping.get(title));
            }
        }
    }

    public HashMap getIndexToEntityFieldMapping() {
        return indexToEntityFieldMapping;
    }

    public Attendee getEntity(String csvLine) {
        return createAttendee(csvLine);
    }

    private String[] safeParseLine(String line) {
        try {
            return parser.parseLine(line);
        } catch (IOException e) {
            System.err.println("Cannot parse "+line);
            e.printStackTrace();
            return new String[0];
        }
    }

    public Attendee createAttendee(String csvLine) {
        Attendee attendee = new Attendee();

        String[] attendeeDetails = safeParseLine(csvLine);
        for (Long key : indexToEntityFieldMapping.keySet()) {
            String details = attendeeDetails[key.intValue()];
            String field = indexToEntityFieldMapping.get(key);

            try {
                Method method = Attendee.class.getMethod("set" + indexToEntityFieldMapping.get(key), String.class);

                try {
                    method.invoke(attendee, details);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (NoSuchMethodException nsm) {
                nsm.printStackTrace();
            }
        }

        return attendee;
    }

}
