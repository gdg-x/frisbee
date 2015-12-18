package org.gdg.frisbee.android.event;

import org.gdg.frisbee.android.api.model.EventFullDetails;

public interface EventDetailsListener {
    void updateFromFullEventDetails(String eventId, EventFullDetails eventFullDetails);
}
