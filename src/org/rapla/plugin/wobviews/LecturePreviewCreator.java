package org.rapla.plugin.wobviews;

import java.util.Calendar;
import java.util.Date;

import org.rapla.entities.domain.Reservation;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.wob.WOBDateUtils;
import org.rapla.plugin.wob.WOBLectureUtils;

public class LecturePreviewCreator extends RaplaComponent implements PreviewCreator {

	private final WOBDateUtils dateUtils;

	private final WOBLectureUtils lectureUtils;

	public LecturePreviewCreator(final RaplaContext context) throws RaplaException {
		super(context);

		dateUtils = new WOBDateUtils(context);
		lectureUtils = new WOBLectureUtils(context);
	}

	public String export(final Date start, final Date end,
			final Reservation[] reservations) throws RaplaException {
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

		exportCategory("Vorlesung (Grundstudium)", rval, reservations);

		exportCategory("Proseminar", rval, reservations);

		exportCategory("Programmierpraktikum", rval, reservations);

		exportCategory("Vorlesung (Hauptstudium)", rval, reservations);

		exportCategory("Seminar (Hauptstudium)", rval, reservations);

		exportCategory("Praktikum (Hauptstudium)", rval, reservations);

		exportCategory("Arbeitsgemeinschaft", rval, reservations);

		exportCategory("Projektgruppe", rval, reservations);

		exportCategory("Dipl. Dokt. Seminar", rval, reservations);

		exportCategory("Oberseminar", rval, reservations);

		exportCategory("Kolloquium", rval, reservations);

		exportCategory(null, rval, reservations);

		rval.append("</ul>");
		rval.append("</body>");
		return rval.toString();
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
	 * @param selectedLectureType
	 *            (rapla typen die veranstaltungen kategorisieren. bsp..:
	 *            Vorlesung (hauptstudium). null == ohne Kategorie.
	 * @return
	 */
	private void exportCategory(final String selectedLectureType,
			final StringBuffer output, final Reservation[] reservations)
			throws RaplaException {

		if (output == null)
			throw new NullPointerException();

		if (reservations == null)
			throw new NullPointerException();

		boolean headerPrinted = false;
		for (int i = 0; i < reservations.length; i++) {
			final String lectureType = lectureUtils
					.getRaplEventType(reservations[i]);

			if (selectedLectureType == null) {
				if ( lectureUtils.hasEventType( reservations[i] ) ) {
					continue;
				}

				if (!headerPrinted) {
					outputLectureType(output, "Ohne Veranstaltungstyp");
					headerPrinted = true;
				}

				printEntryBlock(output, reservations, i);
			} else if (selectedLectureType.equals(lectureType)) {
				if (!headerPrinted) {
					outputLectureType(output, lectureType);
					headerPrinted = true;
				}

				printEntryBlock(output, reservations, i);
			}
		}

		if (headerPrinted) {
			output.append("<br><br></li>");
		}
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
