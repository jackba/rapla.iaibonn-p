package org.rapla.plugin.wobviews;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.rapla.entities.domain.Reservation;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.wob.WOBDateUtils;
import org.rapla.plugin.wob.WOBLectureUtils;

public class PersonPreviewCreator extends RaplaComponent implements PreviewCreator {

	private final WOBDateUtils dateUtils;

	private final WOBLectureUtils lectureUtils;

	public PersonPreviewCreator(final RaplaContext context) throws RaplaException {
		super(context);

		dateUtils = new WOBDateUtils(context);
		lectureUtils = new WOBLectureUtils(context);
	}

	public String export(final Date start, final Date end,
			final Reservation[] reservations) throws RaplaException {
		
		final Set persons = getPersons(reservations);
		
		final StringBuffer rval = new StringBuffer();

		final String formatedStart = formatDate(start);
		final String formatedEnd = formatDate(end);

		rval.append("<body bgcolor=\"#faf4f4\">");

		rval.append("<h1>Vorschau der Veranstaltungen von ");
		rval.append(formatedStart);
		rval.append(" bis ");
		rval.append(formatedEnd);
		rval.append("</h1><br><br>");
		rval.append("<ul>");
		
		for (Iterator i = persons.iterator(); i.hasNext();) {
			final String person = (String) i.next();
			exportPerson(person, rval, reservations);	
		}

		rval.append("</ul>");
		rval.append("</body>");
		return rval.toString();
	}

	private Set getPersons(Reservation[] reservations) {
		final Set rval = new HashSet();
		
		for (int i = 0; i < reservations.length; i++) {
			final String[] persons = lectureUtils.getPersonsAsArray(reservations[i]);
			addPersonsToPersonMap(rval, persons);
		}
		
		return rval;
	}

	private void addPersonsToPersonMap(final Set setOfPersons, 
			final String[] persons) {
		for (int i = 0; i < persons.length; i++) {
			if( setOfPersons.contains(persons[i]))
				continue;
			
			setOfPersons.add(persons[i]);
		}
	}

	private String formatDate(Date name) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(name);

		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int month = calendar.get(Calendar.MONTH);
		final int year = calendar.get(Calendar.YEAR);

		return day + "." + month + "." + year;
	}

	/**
	 * 
	 * @param selectedPerson
	 *            (rapla typen die veranstaltungen kategorisieren. bsp..:
	 *            Vorlesung (hauptstudium). null == ohne Kategorie.
	 * @return
	 */
	private void exportPerson(final String selectedPerson,
			final StringBuffer output, final Reservation[] reservations)
			throws RaplaException {
		if( selectedPerson == null)
			throw new NullPointerException();
		
		if (output == null)
			throw new NullPointerException();

		if (reservations == null)
			throw new NullPointerException();

		boolean headerPrinted = false;
		
		for (int i = 0; i < reservations.length; i++) {
			final boolean reservedForPerson = lectureUtils
					.reservedForPerson(reservations[i], selectedPerson);

			if (reservedForPerson) {
				if (!headerPrinted) {
					outputLectureType(output, selectedPerson);
					headerPrinted = true;
				}

				printEntryBlock(output, reservations, i);
			}
		}

		if (headerPrinted) 
			output.append("<br><br></li>");
		
	}

	/**
	 * @param output
	 * @param lectureType
	 */
	private void outputLectureType(final StringBuffer output,
			final String lectureType) {
		output.append("<li><font size='+1'><b>");
		output.append(lectureType);
		output.append("</b></font><br><br>");
	}

	/**
	 * @param output
	 * @param reservations
	 * @param i
	 * @throws RaplaException
	 */
	private void printEntryBlock(final StringBuffer output,
			final Reservation[] reservations, int i) throws RaplaException {

		output.append("<font size='-1'>");
		output.append(lectureUtils.getLectureTitle(reservations[i]));
		output.append("</font>");
		output.append("<br>");
		output.append("<font size='-1'>");
		final String ortUndZeit = dateUtils.getOrtUndZeit(reservations[i]);
		output.append(ortUndZeit);

		output.append(" (");
		output.append(lectureUtils.getPersons(reservations[i]));
		output.append(")");
		output.append("</font>");
		output.append("<br><br>");
	}

}
