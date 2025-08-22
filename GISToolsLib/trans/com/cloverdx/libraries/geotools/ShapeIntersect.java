package com.cloverdx.libraries.geotools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.geometry.jts.GeometryBuilder;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataField;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldContainerType;
import org.jetel.metadata.DataFieldType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This is an example custom transformer. It shows how you can
 *  read records, process their values and write records.
 */
public class ShapeIntersect extends AbstractGenericTransform {
	public enum CompareMode {
		INTERSECT,CONTAIN,NO_REL,INTERSECT_OR_CONTAIN
	}
	
	public enum Shape {
		POINT,POLYLINE,POLYGON,MULTIPOINT
	}
	/*
	 * Component parameters' field names
	 */
	private static final String PRM_KEY = "MatchKey";
	private static final String PRM_MODE = "CompareMode";
	private static final String PRM_MASTER_FIELD = "MasterField";
	private static final String PRM_MASTER_SHAPE = "MasterShape";
	private static final String PRM_SLAVE_FIELD = "SlaveField";
	private static final String PRM_SLAVE_SHAPE = "SlaveShape";
	/*
	 * Variables
	 */
	protected Shape defaultMasterShape;
	protected Shape defaultSlaveShape;
	protected DataField masterField;
	protected DataField slaveField;
	
	protected int[] shapeFields = new int[2];
	protected int[] coords = new int[2];
	protected CompareMode comparison = CompareMode.INTERSECT_OR_CONTAIN;
	/*
	 * Resources
	 */
	protected GeometryBuilder geometryBuilder;
	protected Set<Geometry> pool;
	
	@Override
	public void execute() {
		// 
		DataField masterCoords;
		DataField slaveCoords;
		//
		// Get fields - first mandatory ones (coordinates)
		masterCoords = inRecords[0].getField(coords[0]);
		slaveCoords = inRecords[1].getField(coords[1]);
		//
		// Then optional
		masterField = shapeFields[0] > -1 ? inRecords[0].getField(shapeFields[0]) : null;
		slaveField = shapeFields[1] > -1 ? inRecords[1].getField(shapeFields[1]) : null;
		// 
		// Init debug
		getLogger().debug("Using field: "+ masterCoords.getMetadata().getName() +" as master coordinates input.");
		getLogger().debug("Using field: "+ slaveCoords.getMetadata().getName() +" as slave coordinates input.");
		getLogger().debug("Master shape field: "+ (masterField != null ? masterField.getMetadata().getName() : "unspecified") +", default: "+ (defaultMasterShape != null ? "'"+ defaultMasterShape +"'" : "unspecified"));
		getLogger().debug("Slave shape field: "+ (slaveField != null ? slaveField.getMetadata().getName() : "unspecified") +", default: "+ (defaultSlaveShape != null ? "'"+ defaultSlaveShape +"'" : "unspecified"));
		getLogger().debug("Mode: "+ comparison.toString());
		//
		//
		DataRecord master = inRecords[0];
		DataRecord slave = inRecords[1];
		//
		// Reject port is optional
		DataRecord match = outRecords[0];
		DataRecord reject = getComponent().getOutputPortsMaxIndex() == 1 ? outRecords[1] : null;
		//
		int cntr = 0;
		// Run (load slave records to cache)
		while ((slave = readRecordFromPort(1)) != null) {
			cntr++;
			// 
			if (!slaveCoords.isNull()) {
				pool.add(buildGeometry(slaveCoords,getShape(slaveField,defaultSlaveShape)));
			// 
			} else {
				//TODO: report missing coordinates?
				getLogger().debug("Record #"+ cntr +" on input port 1 does not contain any data!");
			}
		}
		//
		while ((master = readRecordFromPort(0)) != null) {
			boolean accept = false;
			// 
			// If master records does not contain coordinates, it is rejected automatically
			if (!masterCoords.isNull()) {
				Geometry self = buildGeometry(masterCoords,getShape(masterField,defaultMasterShape));
				Iterator<Geometry> iter = pool.iterator();
			
				while (!accept && iter.hasNext()) {
					Geometry shape = iter.next();
				
					switch (comparison) {
					case CONTAIN:				accept = shape.contains(self); break;
					case INTERSECT:				accept = shape.intersects(self); break;
					case INTERSECT_OR_CONTAIN:	accept = shape.contains(self) || shape.intersects(self); break;
					//No action or any other value
					default: break;
					}
				}
				
				if (!accept && comparison == CompareMode.NO_REL) {
					accept = true;
				}
			}
			//
			// 
			if (accept) {
				match.reset();
				match.copyFrom(master);
				writeRecordToPort(0,match);
			} else if (reject != null) {
				reject.reset();
				reject.copyFrom(master);
				writeRecordToPort(1,reject);
			}
		}
	}
	
	protected Geometry buildGeometry(DataField f, Shape shp) {
		@SuppressWarnings("unchecked")
		double[] coords = toArray((List<Double>) f.getValue());
		
		// Override for too small arrays
		if (coords.length < 3) {
			return geometryBuilder.point(coords[0],coords[1]);
		}
		
		switch (shp) {
		case MULTIPOINT:	return geometryBuilder.multiPoint(coords[0],coords[1],coords[2],coords[3]);
		case POINT:			return geometryBuilder.point(coords[0],coords[1]);
		case POLYGON:		return geometryBuilder.polygon(coords);
		case POLYLINE:		return geometryBuilder.lineString(coords);
		}
		
		throw new JetelRuntimeException("Invalid shape type: '"+ shp +"'");
	}
	
	protected double[] toArray(Collection<Double> col) {
		double[] arr = new double[col.size()];
		int iter = 0;
		
		for (Double d: col) {
			arr[iter++] = d.doubleValue();
		}
		
		return arr;
	}
	
	protected Shape getShape(DataField rec, Shape def) {
		Shape shp;
		
		try {
			shp = rec != null && !rec.isNull() ?
				Shape.valueOf(rec.getValue().toString().toUpperCase()) : def;
		} catch (IllegalArgumentException | EnumConstantNotPresentException ex) {
			getLogger().debug("Unknown shape constant rejected: '"+ rec.getValue().toString() +"'.");
			shp = def;
		}
			
		return shp;
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);
		initConfig();
		
		if (defaultMasterShape == null && shapeFields[0] < 0) {
			status.addError(getComponent(), PRM_MASTER_FIELD, "Neither Input 0 field nor shape was defined. Please configure either one or both.");
		}
		
		if (defaultSlaveShape == null && shapeFields[1] < 0) {
			status.addError(getComponent(), PRM_SLAVE_FIELD, "Neither Input 1 field nor shape was defined. Please configure either one or both.");
		}
		
		if (comparison == null) {
			status.addError(getComponent(), PRM_MODE, "Invalid comparison mode.");
		}
		
		//
		// Basic key check
		if (getProperties().getStringProperty(PRM_KEY).indexOf(";") > -1) {
			status.addError(getComponent(), PRM_KEY, "Key can contain only one pair of coordinate fields.");
		//
		// Data field checks
		} else {
			//
			// Master coordinates field check
			if (coords[0] < 0) {
				status.addError(getComponent(), PRM_KEY, "Cannot find coordinates field for input record 0.");
			} else if (
				getComponent().getInMetadata().get(0).getField(coords[0]).getContainerType() != DataFieldContainerType.LIST ||
				getComponent().getInMetadata().get(0).getField(coords[0]).getDataType() != DataFieldType.NUMBER
			) {
				status.addError(getComponent(), PRM_KEY, "Invalid data type of input record 0 coordinates field. Expected data type is list(number).");
			}
			//
			// Slave coordinates field check
			if (coords[1] < 0) {
				status.addError(getComponent(), PRM_KEY, "Cannot find coordinates field for input record 1.");
			} else if (
				getComponent().getInMetadata().get(1).getField(coords[1]).getContainerType() != DataFieldContainerType.LIST ||
				getComponent().getInMetadata().get(1).getField(coords[1]).getDataType() != DataFieldType.NUMBER
			) {
				status.addError(getComponent(), PRM_KEY, "Invalid data type of input record 1 coordinates field. Expected data type is list(number).");
			}
		}
		
		return status;
	}

	@Override
	public void init() {
		super.init();
		geometryBuilder = new GeometryBuilder();
		pool = new HashSet<>();
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
		initConfig();
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
	
	private void initConfig() {
		String key = getProperties().getStringProperty(PRM_KEY,"");
		String mf = getProperties().getStringProperty(PRM_MASTER_FIELD);
		String sf = getProperties().getStringProperty(PRM_SLAVE_FIELD);
		// 
		// Shape configuration
		defaultMasterShape = Shape.valueOf(getProperties().getStringProperty(PRM_MASTER_SHAPE,"").toUpperCase());
		defaultSlaveShape = Shape.valueOf(getProperties().getStringProperty(PRM_SLAVE_SHAPE,"").toUpperCase());
		// 
		comparison = getMode(getProperties().getIntProperty(PRM_MODE));
		//
		// Shape fields
		if (mf != null && !mf.trim().isEmpty()) {
			shapeFields[0] = getComponent().getInMetadata().get(0).getFieldPosition(mf);
		} else {
			shapeFields[0] = -1;
		}
		if (sf != null && !sf.trim().isEmpty()) {
			shapeFields[1] = getComponent().getInMetadata().get(1).getFieldPosition(sf);
		} else {
			shapeFields[1] = -1;
		}
		//
		// Coordinates fields
		Pattern p = Pattern.compile("\\$([^#;=]+)");
		Matcher m = p.matcher(key);
		for (int i=0;i<2 && m.find();i++) {
			coords[i] = getComponent().getInputPort(i).getMetadata().getFieldPosition(m.group(1));
		}
	}
	
	private CompareMode getMode(Integer mode) {
		switch (mode) {
		case 0: return CompareMode.INTERSECT;
		case 1: return CompareMode.CONTAIN;
		case 2: return CompareMode.INTERSECT_OR_CONTAIN;
		case 3: return CompareMode.NO_REL;
		}
		
		return null;
	}
}
