package badgeService.event;

import badgeService.event.EventSecurity;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by katharine on 07/04/2017.
 */
public class EventSecurityTest {

    @Test
    public void encrypt() throws Exception {
        String payload = EventSecurity.encrypt("test", "payload");

        String decrypted = EventSecurity.decrypt("test", payload);
        assertEquals("payload",decrypted);

    }

}