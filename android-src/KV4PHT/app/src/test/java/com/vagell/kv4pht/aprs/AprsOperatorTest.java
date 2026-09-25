package com.vagell.kv4pht.aprs;

import static org.junit.Assert.assertEquals;

import com.vagell.kv4pht.data.APRSMessage;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AprsOperatorTest {
    @Test
    public void rejectBeatsAck() {
        assertEquals(AprsOperator.DELIVERY_REJECTED, AprsOperator.deliveryAfterReport(true, true));
        assertEquals(AprsOperator.DELIVERY_ACKED, AprsOperator.deliveryAfterReport(true, false));
        assertEquals(AprsOperator.DELIVERY_PENDING, AprsOperator.deliveryAfterReport(false, false));
    }

    @Test
    public void maidenheadForPhiladelphia() {
        assertEquals("FM29kw", AprsOperator.maidenhead(39.9526, -75.1652));
    }

    @Test
    public void blankGridForMissingPosition() {
        assertEquals("", AprsOperator.maidenhead(0.0, 0.0));
    }

    @Test
    public void latestStationKeepsNewestFix() {
        APRSMessage older = station("W6MOW", 36.6, -121.6, 100);
        APRSMessage newer = station("w6mow", 36.7, -121.7, 200);
        APRSMessage chat = new APRSMessage();
        chat.type = APRSMessage.MESSAGE_TYPE;
        chat.fromCallsign = "W6MOW";
        chat.timestamp = 300;

        List<APRSMessage> stations = AprsOperator.latestStations(Arrays.asList(older, chat, newer));
        assertEquals(1, stations.size());
        assertEquals(200, stations.get(0).timestamp);
        assertEquals(Collections.singletonList(newer), stations);
    }

    private static APRSMessage station(String call, double lat, double lon, long timestamp) {
        APRSMessage message = new APRSMessage();
        message.type = APRSMessage.POSITION_TYPE;
        message.fromCallsign = call;
        message.positionLat = lat;
        message.positionLong = lon;
        message.timestamp = timestamp;
        return message;
    }
}
