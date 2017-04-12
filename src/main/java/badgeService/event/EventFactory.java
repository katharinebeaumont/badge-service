package badgeService.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by katharine on 11/04/2017.
 */
@Component
public class EventFactory {

    private EventProperties properties;

    @Autowired
    public EventFactory(EventProperties properties) {
        this.properties = properties;
    }

    public Event factoryDevoxxUK() {
        Event event = new Event();
        event.setKey(properties.getKey());
        event.setName(properties.getName());
        event.setImageUrl(properties.getImageUrl());
        event.setBegin(properties.getBegin());
        event.setEnd(properties.getEnd());
        event.setLocation(properties.getLocation());
        event.setApiVersion(properties.getApiVersion());
        event.setOneDay(false);
        event.setExternal(true);
        return event;
    }
}
