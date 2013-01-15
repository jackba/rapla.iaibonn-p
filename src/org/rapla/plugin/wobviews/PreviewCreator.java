package org.rapla.plugin.wobviews;

import java.util.Date;

import org.rapla.entities.domain.Reservation;
import org.rapla.framework.RaplaException;

public interface PreviewCreator {

	public String export(final Date start, final Date end,
			final Reservation[] reservations) throws RaplaException;
}
