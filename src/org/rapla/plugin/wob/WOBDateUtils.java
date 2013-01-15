package org.rapla.plugin.wob;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.Repeating;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;

public final class WOBDateUtils extends WOBUtils {

	public WOBDateUtils(final RaplaContext context) throws RaplaException {
		super(context);
	}
	
	public String getOrtUndZeit(final Reservation reservation) throws RaplaException {
		
		final SimpleDateFormat startFormat = new SimpleDateFormat("EE HH-");
		final SimpleDateFormat endFormat = new SimpleDateFormat("HH");

		final Allocatable[] allocs = reservation.getResources();

		final StringBuffer sb = new StringBuffer();

		boolean createSimple = false;
		boolean reservationHasRooms = false;

		final List rooms = new ArrayList(allocs.length);
		
		for (int i = 0; i < allocs.length; i++) {
			Allocatable alloc = allocs[i];

			Classification clsf = alloc.getClassification();

			ClassificationFilter filterBitRooms = createFilter(getClientFacade(), getLectureMappings()
					.getProperties().getProperty("resource.type.BIT_ROOM"));
			ClassificationFilter filterRooms = createFilter(getClientFacade(), getLectureMappings()
					.getProperties().getProperty("resource.type.ROOM"));

			if (filterRooms.matches(clsf) || filterBitRooms.matches(clsf)) {
				reservationHasRooms = true;

				rooms.add(alloc);

				Appointment[] apps = reservation.getRestriction(alloc);

				if (apps == null || apps.length == 0) {
					if (i > 0)
						createSimple |= true;
					else
						createSimple = true;
				} else {
					createSimple = false;
				}
			}

		}

		if (!reservationHasRooms) {
			return null;
		}

		int size = rooms.size();
		Appointment[] appointments = reservation.getAppointments();

		if (createSimple) {
			if (!printAppointments(appointments, getLocale(), sb, startFormat,
					endFormat)) {
				// Appointments wurden nicht ausgegeben, d.h. keine wieder
				// kehrende reservierung.
				return null;
			}

			for (int i = 0; i < size; i++) {
				Allocatable alloc = (Allocatable) rooms.get(i);
				if (i > 0)
					sb.append(", ");
				sb.append(getValue(alloc.getName(getLocale())));
			}
		} else {
			for (int i = 0; i < size; i++) {
				Allocatable alloc = (Allocatable) rooms.get(i);
				if (i > 0)
					sb.append(", ");

				Appointment[] apps = reservation.getRestriction(alloc);
				if (apps == null || apps.length == 0) {
					if (!printAppointments(appointments, getLocale(), sb,
							startFormat, endFormat)) {
						// Appointments wurden nicht ausgegeben, d.h. keine
						// wieder kehrende reservierung.
						return null;
					}
				} else {
					if (!printAppointments(apps, getLocale(), sb, startFormat,
							endFormat)) {
						// Appointments wurden nicht ausgegeben, d.h. keine
						// wieder kehrende reservierung.
						return null;
					}
				}

				sb.append(getValue(alloc.getName(getLocale())));
			}
		}

		return sb.toString();
	}

    private boolean printAppointments(Appointment[] appointments,
            Locale locale2, StringBuffer sb, SimpleDateFormat startFormat,
            SimpleDateFormat endFormat) {

        for (int i = 0; i < appointments.length; i++) {
            Appointment app = appointments[i];

            // check repeating:
            Repeating repeating = app.getRepeating();
            if (repeating == null) {
                return false;
            }

            if (i > 0)
                sb.append(", ");

            final String startDate = getDate(app.getStart());
            final String endDate = getDate(app.getEnd());
            
            final String appointmentDate;
            if( startDate != null && startDate.equalsIgnoreCase(endDate)  ) {
            	appointmentDate = startDate;
            } else {
            	throw new RuntimeException();
            }
            
            sb.append( appointmentDate );
            sb.append( ' ' );
            
            sb.append(getTime(app.getStart()));
            sb.append("-");
            sb.append(getTime(app.getEnd()));
        }

        if (appointments.length > 0)
            sb.append(" ");

        return true;
    }

}
