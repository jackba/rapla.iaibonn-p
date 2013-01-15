package org.rapla.plugin.wob;

import java.util.Date;

import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.wobexport.ValidationException;

public class WOBUtils extends RaplaComponent {

	private final LectureMappings lectureMappings;
	
	public WOBUtils(RaplaContext context) throws RaplaException {
		super(context);
		
		lectureMappings = new LectureMappings();
	}

	public LectureMappings getLectureMappings() {
		return lectureMappings;
	}
	
	protected ClassificationFilter createFilter(ClientFacade facade,
			final String typeName) throws RaplaException {
		final DynamicType dtVorlesung;

		try {
			dtVorlesung = facade.getDynamicType(typeName);
		} catch (Exception x) {
			throw new RaplaException(
					"kann einen dynamischen typen nicht holen. typ = "
							+ typeName, x);
		}

		return dtVorlesung.newClassificationFilter();
	}
	
    
    public String getValue(Object o) {
        if (o != null)
            return o.toString();
        return "";
    }
    
    public String getDate(Date end) {
    	final String date = getRaplaLocale().getWeekday(end); //raplaLocale.formatDate(end);
    	return date;
	}

	public String getTime(Date date) {
        final String time = getRaplaLocale().formatTime(date);

        int pos = time.indexOf(':');

        if (pos < 0) {
            throw new ValidationException("time-string has no ':'");
        }

        return time.substring(0, pos);
    }
	
}
