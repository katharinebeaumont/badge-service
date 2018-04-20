package badgeService.util;

import badgeService.attendee.Attendee;
import com.opencsv.CSVReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
public class CSVParser {

    private String csvPath;
    private String entityMapping;

    public CSVParser(String csvPath, String entityMapping) {
        this.csvPath = csvPath;
        this.entityMapping = entityMapping;
    }

    public List<Attendee> parseEntitiesFromCSV() {
        List<Attendee> entities = new ArrayList<>();
        Resource resource = new ClassPathResource(csvPath);
        long changedSince = System.currentTimeMillis();

        try (Reader isReader = new InputStreamReader(resource.getInputStream()); CSVReader reader = new CSVReader(isReader)){

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader((new InputStreamReader(resource.getInputStream(), Charset.forName("UTF-8"))))){
            CSVAttendeeMapper mapper = new CSVAttendeeMapper(entityMapping, br.readLine());
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                Attendee entity = mapper.getEntity(line);
                entity.setChangedSince(changedSince);
                entities.add(entity);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        return entities;
    }
}
