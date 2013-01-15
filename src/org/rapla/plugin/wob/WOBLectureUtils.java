package org.rapla.plugin.wob;

import org.rapla.entities.Category;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;

public class WOBLectureUtils extends WOBUtils {

	private ClassificationFilter informatikType;

	private ClassificationFilter informatikBitType;

	public WOBLectureUtils(RaplaContext context) throws RaplaException {
		super(context);

		informatikType = createFilter(getClientFacade(), getLectureMappings()
				.getProperties().getProperty("veranstaltung.type.BIT"));
		
		informatikBitType = createFilter(getClientFacade(),
				getLectureMappings().getProperties().getProperty(
						"veranstaltung.type.INFO"));
	}

	
	public boolean isReservationOfComputerScinceLecture(final Reservation reservation) throws RaplaException {
		if( reservation == null)
			throw new NullPointerException();
		
		final Classification classification = reservation.getClassification();
       
        return informatikBitType.matches(classification) || informatikType.matches(classification);
	}
	
	public String getRaplEventType(final Reservation reservation)
			throws RaplaException {
		final Classification c = reservation.getClassification();

		if (!isReservationOfComputerScinceLecture(reservation)) {
			return null;
		}

		final Attribute a = c.getAttribute(getLectureMappings().getProperties()
				.getProperty("veranstaltung.type"));
		
		if (a == null)
			return "";

		final Object o = c.getValue(a);
		if (o == null)
			return "";
		
		final String raplaType = getValue(((Category) o).getName(getLocale()));

		return raplaType;
	}

	public String getWobEventType(final Reservation reservation) throws RaplaException {
		final String raplaType = getRaplEventType(reservation);
		
		final String wobType = (String) getLectureMappings().get(raplaType);
		if (wobType == null)
			return "";

		return wobType;
	}
	
	public String[] getPersonsAsArray(final Reservation reservation) {
		
        final Allocatable[] allocs = reservation.getPersons();
        
        if( allocs == null || allocs.length < 1) return new String[0];
        
        final String[] rval = new String[allocs.length];
        
      
        for (int i = 0; i < allocs.length; i++) {
            Classification c = allocs[i].getClassification();
            rval[i] = createPersonName(c);
        }

        return rval;
	}
	
	private String createPersonName(final Classification classification) {
		final StringBuffer nameCache = new StringBuffer();

        String title = getValue(classification.getValue(getLectureMappings().getProperties().getProperty("person.title")))
                .trim();
        
        if (title.equalsIgnoreCase("professor")
                || title.equalsIgnoreCase("Professort")) {
            title = "Prof.";
        }
        
        if (title.length() > 0)
            title += " ";

        nameCache.append(title);
        
        nameCache.append(getValue(classification.getValue(getLectureMappings().getProperties().getProperty("person.forename"))))
                .append(" ");
        
        nameCache.append(getValue(classification.getValue(getLectureMappings().getProperties().getProperty("person.surname"))));
        
		return nameCache.toString();
	}


	public boolean reservedForPerson(final Reservation reservation, final String fullNameOfSelectedPerson) {
        if( fullNameOfSelectedPerson == null)
        	throw new NullPointerException();
        
		final Allocatable[] allocs = reservation.getPersons();
        
        if( allocs == null || allocs.length < 1) return false;
      
        for (int i = 0; i < allocs.length; i++) {
            final Classification c = allocs[i].getClassification();
            final String personName = createPersonName(c);
            if( personName == null) continue;
            if( personName.equals(fullNameOfSelectedPerson)) return true;
        }
        
		return false;
	}
	
    public String getPersons(Reservation reservation) {
        final StringBuffer sb = new StringBuffer();

        final Allocatable[] allocs = reservation.getPersons();
        for (int i = 0; i < allocs.length; i++) {
            Classification c = allocs[i].getClassification();

            if (i > 0)
                sb.append(", ");

            sb.append(createPersonName(c));
        }

        return sb.toString();
    }
    
    
    public String getLectureTitle(final Reservation reservation) {
        final String title = getValue(reservation.getName(getLocale()));
        
        return title;
    }


	public boolean hasEventType(final Reservation reservation) {
		final Classification c = reservation.getClassification();

		final Attribute a = c.getAttribute(getLectureMappings().getProperties()
				.getProperty("veranstaltung.type"));
		
		if (a == null)
			return false;

		final Object o = c.getValue(a);
		
		if (o == null)
			return false;
		
		final String raplaType = getValue(((Category) o).getName(getLocale()));

		return (raplaType != null && raplaType.length() > 0);
	}

}
