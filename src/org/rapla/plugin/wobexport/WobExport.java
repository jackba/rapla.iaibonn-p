/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.plugin.wobexport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.rapla.entities.Category;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.Repeating;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.wob.LectureMappings;
import org.rapla.plugin.wob.WOBLectureUtils;

public class WobExport extends RaplaComponent
{
        final Locale locale;
       
        private final LectureMappings LECTURE_MAPPINGS;

        private static final String NL = "\n";

        private RaplaLocale raplaLocale;
        
		private boolean highlightProblems;

		
		private final WOBLectureUtils lectureUtils;
		
        public WobExport(final RaplaContext context) throws RaplaException {
            super( context);
            this.raplaLocale = getRaplaLocale();
            this.locale = raplaLocale.getLocale();
            
            this.LECTURE_MAPPINGS = LectureMappings.getInstance();
            this.lectureUtils = new WOBLectureUtils(context);
        }

        public List /* Strings, StyledEntries */ exportAsStringArray( Reservation[] reservations ) throws RaplaException {
        	final ClientFacade facade = getClientFacade();

        	final ArrayList lines = new ArrayList(100);

        	for (int i = 0; i < reservations.length; i++) {

                  String ortUndZeit = getOrtUndZeit(reservations[i], facade);
                  if (ortUndZeit == null) {
                      continue;
                  }

                  String lectureType = getLectureType(reservations[i], facade);
                  if (lectureType == null) {
                      continue;
                  }

                  lines.add("?=========================================================");
                  lines.add(NL);
                  lines.add("?class: class152" + NL);
                  lines.add("?parent: dummy" + NL);
                  lines.add(createLectureType(lectureType));
                  lines.add("?subtype: lehre_lv" + NL);
                  lines.add(createLehreTitle(reservations, i));
                  lines.add("?lehre_lv_ort_zeit: " + ortUndZeit + NL);
                  lines.add(createLehreVeranstalter(reservations, i));
                  lines.add(createLehreLvSort(reservations, i));
                  lines.add("?write_permission: "
                          + getValue(reservations[i].getOwner()) + NL);
                  lines.add("?create_permission: "
                          + getValue(reservations[i].getOwner()) + NL);
              }

        	return lines;
        }

        private String createLehreTitle(final Reservation[] reservations,
                final int positionInArray) {
            final String title = getValue(reservations[positionInArray]
                    .getName(locale));
            final String rval = "?lehre_lv_titel: " + title + NL;
            return rval;
        }

        private String createLectureType(String lectureType){
            return "?lehre_lv_typ: " + lectureType + NL;
        }

        private Object createLehreVeranstalter(final Reservation[] reservations,
                final int positionInArray) throws RaplaException {
            final String persons = this.lectureUtils.getPersons(reservations[positionInArray]);
            final String rval = "?lehre_lv_veranstalter: " + persons + NL;

            if (persons.length() < 1)
				return hightLightProblem(rval);

            return rval;
        }

        private Object hightLightProblem(String value) {
			if (highlightProblems) {
				return new StyledWobEntry(value, true);
			}else {
				return value;
			}
		}



		private Object createLehreLvSort(final Reservation[] reservations,
                final int positionInArray) throws RaplaException {
            final String personSort = getPersonsSort(reservations[positionInArray]);
            final String rval = "?lehre_lv_vv_sort: " + personSort + NL;

            if (personSort.length() < 1)
				return hightLightProblem(rval);

            return rval;
        }


        private String getLectureType(Reservation reservation, ClientFacade facade) throws RaplaException {
            Classification c = reservation.getClassification();

            ClassificationFilter infoType = createFilter(facade,
                    getProperty("veranstaltung.type.BIT"));

            ClassificationFilter infoType2 = createFilter(facade,
                    getProperty("veranstaltung.type.INFO"));

            if (!infoType.matches(c) && !infoType2.matches(c)) {
                return null;
            }

            Attribute a = c.getAttribute(getProperty("veranstaltung.type"));
            if (a == null)
                return "";

            Object o = c.getValue(a);
            if (o == null)
                return "";
            String raplaType = getValue(((Category) o).getName(locale));

            String wobType = "";
            wobType = (String) LECTURE_MAPPINGS.get(raplaType);
            if (wobType == null)
                wobType = "";

            return wobType;
        }

        private String getProperty(String name) {
            String prop = LECTURE_MAPPINGS.getProperties().getProperty(name);
            if (prop == null || prop.length() == 0) {
                throw new RuntimeException(
                        "In der RaplaExporterServlet.properties Datei fehlt ein Schl?ssel: name == "
                                + name);
            }
            return prop.trim();
        }

        private String getPersonsSort(Reservation reservation) {
            StringBuffer sb = new StringBuffer();

            Allocatable[] allocs = reservation.getPersons();
            for (int i = 0; i < allocs.length; i++) {
                Classification c = allocs[i].getClassification();

                try {

                    Attribute a = c.getAttribute(getProperty("person.sort"));
                    if (a == null)
                        continue;

                    Object o = c.getValue(a);
                    if (o == null)
                        continue;

                    String sort = getValue(((Category) o).getName(locale));
                    if (sort.length() > 2) {
                        String sortNr = sort.substring(0, 3);
                        sb.append(sortNr);
                        break;
                    }
                } catch (RuntimeException e) {
                    // sb.append(" ");
                    ;
                }
            }

            return sb.toString();
        }

          private String getOrtUndZeit(Reservation reservation, ClientFacade facade) throws RaplaException {
            final SimpleDateFormat startFormat = new SimpleDateFormat("EE HH-");
            final SimpleDateFormat endFormat = new SimpleDateFormat("HH");

            final Allocatable[] allocs = reservation.getResources();

            final StringBuffer sb = new StringBuffer();

            boolean createSimple = false;
            boolean reservationHasRooms = false;

            List rooms = new ArrayList(allocs.length);
            for (int i = 0; i < allocs.length; i++) {
                Allocatable alloc = allocs[i];

                Classification clsf = alloc.getClassification();
                ClassificationFilter filterBitRooms = createFilter(facade,
                        getProperty("resource.type.BIT_ROOM"));
                ClassificationFilter filterRooms = createFilter(facade,
                        getProperty("resource.type.ROOM"));

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
                if (!printAppointments(appointments, locale, sb, startFormat,
                        endFormat)) {
                    // Appointments wurden nicht ausgegeben, d.h. keine wieder
                    // kehrende reservierung.
                    return null;
                }

                for (int i = 0; i < size; i++) {
                    Allocatable alloc = (Allocatable) rooms.get(i);
                    if (i > 0)
                        sb.append(", ");
                    sb.append(getValue(alloc.getName(locale)));
                }
            } else {
                for (int i = 0; i < size; i++) {
                    Allocatable alloc = (Allocatable) rooms.get(i);
                    if (i > 0)
                        sb.append(", ");

                    Appointment[] apps = reservation.getRestriction(alloc);
                    if (apps == null || apps.length == 0) {
                        if (!printAppointments(appointments, locale, sb,
                                startFormat, endFormat)) {
                            //                       Appointments wurden nicht ausgegeben, d.h. keine
                            // wieder kehrende reservierung.
                            return null;
                        }
                    } else {
                        if (!printAppointments(apps, locale, sb, startFormat,
                                endFormat)) {
                            //                       Appointments wurden nicht ausgegeben, d.h. keine
                            // wieder kehrende reservierung.
                            return null;
                        }
                    }

                    sb.append(getValue(alloc.getName(locale)));
                }
            }

            return sb.toString();
        }

        private ClassificationFilter createFilter(ClientFacade facade,
                final String typeName) throws RaplaException {
            final DynamicType dtVorlesung;

            try {
                dtVorlesung = facade.getDynamicType(typeName);
            } catch (Exception x) {
                throw new RaplaException (
                        "kann einen dynamischen typen nicht holen. typ = "
                                + typeName, x);
            }

            return dtVorlesung.newClassificationFilter();
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

        private String getDate(Date end) {
        	final String date = raplaLocale.getWeekday(end); //raplaLocale.formatDate(end);
        	return date;
		}

		private String getTime(Date date) {
            final String time = raplaLocale.formatTime(date);

            int pos = time.indexOf(':');

            if (pos < 0) {
                throw new ValidationException("time-string has no ':'");
            }

            return time.substring(0, pos);
        }

        private String getValue(Object o) {
            if (o != null)
                return o.toString();
            return "";
        }


		public boolean isHighlightProblems() {
			return highlightProblems;
		}



		public void setHighlightProblems(boolean highlightProblems) {
			this.highlightProblems = highlightProblems;
		}

}

