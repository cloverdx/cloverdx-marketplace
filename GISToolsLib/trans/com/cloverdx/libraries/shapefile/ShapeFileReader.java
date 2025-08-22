package com.cloverdx.libraries.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.JetelRuntimeException;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractMultiPointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPolyShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.NullShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineZShape;

public class ShapeFileReader extends AbstractGenericTransform {
	
	private static final String SHAPEFILE_URL_ATTRIBUTE = "shapefile";

	// Metadata fields
	private static final String Y_VAL = "yVal";
	private static final String X_VAL = "xVal";
	private static final String Z_VAL = "zVal";
	private static final String M_VAL = "measure";
	private static final String SHAPE_TYPE = "shapeType";
	private static final String SHAPE_ID = "shapeNum";
	private static final String PART_ID = "partNum";

	private String shapeFileURL = null;
	private Logger log = null;
	private InputStream is = null;
	private org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader shpReader = null;
	private ShapeFileHeader shpFileHeader;
	private AbstractShape currentShape = null;

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		shapeFileURL = getProperties().getStringProperty(SHAPEFILE_URL_ATTRIBUTE);
		File shapeFile;
		shapeFile = getFile(shapeFileURL);
		if (!shapeFile.exists()) {
			throw new ComponentNotReadyException("Input file does not exist or is not accessible: " + shapeFileURL);
		}

		log = getNode().getLog();

		try {
			is = getInputStream(shapeFileURL);
			
			ValidationPreferences prefs = new ValidationPreferences();
		    prefs.setMaxNumberOfPointsPerShape(20000);
		    //prefs.setAllowBadContentLength(true);
		    
		    shpReader = new org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader(is, prefs);
		} catch (InvalidShapeFileException e) {
			throw new ComponentNotReadyException("Input shape file contains unknown shape types", e);
		} catch (IOException e) {
			throw new ComponentNotReadyException("Unable to open input file", e);
		}

		shpFileHeader = shpReader.getHeader();
		log.info("Reading shapefile: " + shapeFileURL);
		log.info("The input shape file contains shape: " + shpFileHeader.getShapeType());
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			throw new ComponentNotReadyException("Unable to close input stream for file: " + shapeFileURL);
		}
	}

	@Override
	public void execute() {

		try {
			DataRecord output = outRecords[0];
			while (getComponent().runIt() && (currentShape = shpReader.next()) != null) {
				output.reset();

				switch (currentShape.getShapeType()) {
				case POINT:
				case POINT_M:
				case POINT_Z:
					AbstractPointShape shapePoint = (AbstractPointShape) currentShape;
					writePointShape(shapePoint, output);
					break;
				case POLYGON:
				case POLYGON_M:
				case POLYGON_Z:
				case POLYLINE:
				case POLYLINE_M:
				case POLYLINE_Z:
					AbstractPolyShape shapePolygon = (AbstractPolyShape) currentShape;
					writePolyShape(shapePolygon, output);
					break;
				case MULTIPOINT:
				case MULTIPOINT_M:
				case MULTIPOINT_Z:
					AbstractMultiPointShape abstractMultiPointShape = (AbstractMultiPointShape) currentShape;
					writeMultiPointShape(abstractMultiPointShape, output);
					break;
				case NULL:
					NullShape nullShape = (NullShape) currentShape;
					writeNullShape(nullShape, output);
					break;
				case MULTIPATCH:
					//MultiPatchShape multiPatchShape = (MultiPatchShape) currentShape;
					//break;
				default:
					throw new JetelRuntimeException("Unsupported shape: " + currentShape.getShapeType());
				}
			}
		} catch (Exception e) {
			throw new JetelRuntimeException(e);
		}
	}
	
	private void fillRecord(AbstractShape shape, Double x, Double y, Double z, Double measure, DataRecord output) {
		output.getField(SHAPE_ID).setValue(currentShape.getHeader().getRecordNumber());
		output.getField(SHAPE_TYPE).setValue(currentShape.getShapeType().toString());
		output.getField(X_VAL).setValue(x);
		output.getField(Y_VAL).setValue(y);
		output.getField(Z_VAL).setValue(z);
		output.getField(M_VAL).setValue(measure);
	}
	
	private void writeNullShape(NullShape shape, DataRecord output) {
		fillRecord(shape, null, null, null, null, output);
		writeRecordToPort(0, output);
	}
	
	private void writePointShape(AbstractPointShape shape, DataRecord output) throws IOException, InterruptedException {
		if (shape.getShapeType() == ShapeType.POINT_Z) {
			fillRecord(shape, shape.getX(), shape.getY(), ((PointZShape) shape).getZ(), ((PointZShape) shape).getM(), output);
		} else if (shape.getShapeType() == ShapeType.POINT_M) {
			fillRecord(shape, shape.getX(), shape.getY(), null, ((PointMShape) shape).getM(), output);
		} else {
			fillRecord(shape, shape.getX(), shape.getY(), null, null, output);
		}
		writeRecordToPort(0, output);
	}
	
	private void writePolyShape(AbstractPolyShape shape, DataRecord output) throws IOException, InterruptedException {
		PointData[] pointData = shape.getPoints();
		log.info("Poly shape found, " + pointData.length + " points, " + shape.getNumberOfParts() + " parts");
		for (int part = 0; part < shape.getNumberOfParts(); part++) {
			pointData = shape.getPointsOfPart(part);
			log.info("Part found, " + pointData.length + " points.");
			for (int i = 0; i < pointData.length; i++) {
				if (shape.getShapeType() == ShapeType.POLYGON_Z) {
					fillRecord(currentShape, pointData[i].getX(), pointData[i].getY(), ((PolygonZShape) shape).getZOfPart(part)[i], ((PolygonZShape) shape).getMOfPart(part)[i], output);
				} else if (shape.getShapeType() == ShapeType.POLYGON_M) {
					fillRecord(currentShape, pointData[i].getX(), pointData[i].getY(), null, ((PolygonMShape) shape).getMOfPart(part)[i], output);
				} else if (shape.getShapeType() == ShapeType.POLYLINE_Z) {
					fillRecord(currentShape, pointData[i].getX(), pointData[i].getY(), ((PolylineZShape) shape).getZOfPart(part)[i], ((PolylineZShape) shape).getMOfPart(part)[i], output);
				} else if (shape.getShapeType() == ShapeType.POLYLINE_M) {
					fillRecord(currentShape, pointData[i].getX(), pointData[i].getY(), null, ((PolylineMShape) shape).getMOfPart(part)[i], output);
				} else {
					// polyline or polygon
					fillRecord(currentShape, pointData[i].getX(), pointData[i].getY(), null, null, output);
				}
				output.getField(PART_ID).setValue(part + 1); // indexing part from 1
				writeRecordToPort(0, output);
			}
		}
	}
	
	private void writeMultiPointShape(AbstractMultiPointShape shape, DataRecord output) throws IOException, InterruptedException {
		PointData[] points = shape.getPoints();
		
		if (shape.getShapeType() == ShapeType.MULTIPOINT_Z) {
			MultiPointZShape exactShape = (MultiPointZShape) shape;
			double[] z = exactShape.getZ();
			double[] m = exactShape.getM();
			for (int i = 0; i < points.length; i++) {
				fillRecord(shape, points[i].getX(), points[i].getY(), z[i], m[i], output);
				writeRecordToPort(0, output);
			}
		} else if (shape.getShapeType() == ShapeType.MULTIPOINT_M) {
			MultiPointMShape exactShape = (MultiPointMShape) shape;
			double[] m = exactShape.getM();
			for (int i = 0; i < points.length; i++) {
				fillRecord(shape, points[i].getX(), points[i].getY(), null, m[i], output);
				writeRecordToPort(0, output);
			}
		} else {
			for (int i = 0; i < points.length; i++) {
				fillRecord(shape, points[i].getX(), points[i].getY(), null, null, output);
				writeRecordToPort(0, output);
			}
		}
	}

}
