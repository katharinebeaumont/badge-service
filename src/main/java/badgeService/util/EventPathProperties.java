package badgeService.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
@Component
@ConfigurationProperties(prefix="eventReport")
public class EventPathProperties {


    private String path;
    private String attendeeTitleMapping;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAttendeeTitleMapping() {
        return attendeeTitleMapping;
    }

    public void setAttendeeTitleMapping(String attendeeTitleMapping) {
        this.attendeeTitleMapping = attendeeTitleMapping;
    }

}
