package badgeService.util;

import badgeService.attendee.Attendee;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by katharinevoxxed on 09/02/2017.
 * OK. This is awful.
 */
public class CSVAttendeeMapper<A> extends CSVEntityMapper {

    private HashMap<Long, String> indexToEntityFieldMapping = new HashMap<>();

    public CSVAttendeeMapper (String attendeeReportMapping, String csvTitleLine) {
        HashMap titleToEntityFieldMapping = getTitleToEntityFieldMapping(attendeeReportMapping);
        this.generateIndexToEntityFieldMapping(titleToEntityFieldMapping, csvTitleLine);
    }

    public HashMap getTitleToEntityFieldMapping(String titleToEntityField) {

        HashMap titleToEntityFieldMapping = new HashMap();
        Arrays.asList(titleToEntityField.split(",")) //gives an array with elements key:value
                .stream()
                .map(s -> s.split(":")) //gives 2 values, key,value
                .forEach(pair -> titleToEntityFieldMapping.put(pair[0],pair[1]));
        return titleToEntityFieldMapping;
    }

    public void generateIndexToEntityFieldMapping(HashMap titleToEntityFieldMapping, String csvTitleLine) {

        List<String> titles = Arrays.asList(csvTitleLine.split(","));
        for (String title : titles) {
            if (titleToEntityFieldMapping.containsKey(title)) {
                Long key = new Long(titles.indexOf(title));
                indexToEntityFieldMapping.put(key, (String)titleToEntityFieldMapping.get(title));
            }
        }
    }

    public HashMap getIndexToEntityFieldMapping() {
        return indexToEntityFieldMapping;
    }

    public Attendee getEntity(String csvLine) {
        return createAttendee(csvLine);
    }

    public Attendee createAttendee(String csvLine) {
        Attendee attendee = new Attendee();

        List<String> attendeeDetails = Arrays.asList(csvLine.split(","));
        for (Long key : indexToEntityFieldMapping.keySet()) {
            String details = attendeeDetails.get(key.intValue());
            String field = indexToEntityFieldMapping.get(key);

            try {
                Method method = null;
                method = Attendee.class.getMethod("set" + indexToEntityFieldMapping.get(key), String.class);

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
