package org.rapla.plugin.wob;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.rapla.framework.RaplaException;

public class LectureMappings extends HashMap {

	private static LectureMappings INSTANCE;
	
	
	private static final long serialVersionUID = 1L;

	private Properties props = new Properties();

	private static String PROPERTY_FILE_NAME = "WobExport.properties";

	public LectureMappings() throws RaplaException {
		initLectureNames();

		initAdditionalProperties();
	}

	private void initAdditionalProperties() throws RaplaException {
		final InputStream in = LectureMappings.class
				.getResourceAsStream(PROPERTY_FILE_NAME);

		if (in != null) {
			try {
				props.load(in);
			} catch (IOException e) {
				throw new RaplaException("can' read properties-file, cause "
						+ e, e);
			}
		} else {
			throw new RaplaException("can't find properties-file "
					+ PROPERTY_FILE_NAME + " in the package.");
		}
	}

	private void initLectureNames() {
		this.put("Vorlesung (Grundstudium)", "g1-Vorlesung-Grundstudium");
		this.put("Proseminar", "g2-Proseminar-Grundstudium");
		this
				.put("Programmierpraktikum",
						"g3-Programmierpraktikum-Grundstudium");
		this.put("Vorlesung (Hauptstudium)", "h1-Vorlesung-Hauptstudium");
		this.put("Seminar (Hauptstudium)", "h2-Seminar-Hauptstudium");
		this.put("Praktikum (Hauptstudium)", "h3-Praktikum-Hauptstudium");
		this.put("Arbeitsgemeinschaft", "h4-Arbeitsgemeinschaft-Hauptstudium");
		this.put("Projektgruppe", "h5-Projektgruppe-Hauptstudium");
		this.put("Dipl. Dokt. Seminar",
				"h6-Diplomanden_Doktoranden_Seminar-Hauptstudium");
		this.put("Oberseminar", "h7-Oberseminar-Hauptstudium");
		this.put("Kolloquium", "h8-Kolloquium-Hauptstudium");
	}

	public Properties getProperties() {
		return props;
	}
	
	public synchronized static final LectureMappings getInstance() throws RaplaException {
		if( INSTANCE == null )
			INSTANCE = new LectureMappings();
		
		
		return INSTANCE;
	}
	
}
