package com.cloverdx.libraries.gcloud;

public class RejectedRecordException extends RuntimeException {

	/**
	 * 
	 */
	
	private String rowNum = "";
	
	
	private static final long serialVersionUID = 5585967372690578397L;

	public RejectedRecordException() {
		// TODO Auto-generated constructor stub
	}

	public RejectedRecordException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public RejectedRecordException(String message, String rowNum) {
		super(message);
		this.rowNum = rowNum;
		// TODO Auto-generated constructor stub
	}

	public RejectedRecordException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public RejectedRecordException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RejectedRecordException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public String getRowNum() {
		return rowNum;
	}

	public void setRowNum(String rowNum) {
		this.rowNum = rowNum;
	}

}
