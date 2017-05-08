package badgeService.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
public class CSVParser<E> {

    private String csvPath;
    private String entityMapping;

    public CSVParser(String csvPath, String entityMapping) {
        this.csvPath = csvPath;
        this.entityMapping = entityMapping;
    }

    private CSVAttendeeMapper mapper;

    public List<E> parseEntitesFromCSV() {
        List<E> entities = new ArrayList<E>();
        BufferedReader br = null;
        Resource resource = new ClassPathResource(csvPath);
        InputStream resourceInputStream = null;

        try {
            resourceInputStream = resource.getInputStream();
            br = new BufferedReader((new InputStreamReader(resourceInputStream, Charset.forName("UTF-8"))));

            mapper = new CSVAttendeeMapper<E>(entityMapping, br.readLine());

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);

                E entity = (E)mapper.getEntity(line);
                entities.add(entity);
            }


        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception ne) {
                ne.printStackTrace();
            }
        }
        return entities;
    }
}
