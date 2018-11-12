package de.mpg.aai.security.auth.model;

public abstract class AbstractReadOnly implements ReadOnly {
	/** 
	 * specifies an instances modification status - if true, no more modifications are allowed (via setter)
	 *  - leads to throwing IllegalStateExceptions if setters are called 
	 */
	private	boolean		readOnly;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly() {
		return this.readOnly;
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public void setReadOnly() {
		this.readOnly = true;
	}
	
	
	/**
	 * checks the internal readonly flag and throws an {@link IllegalStateException} if it is true 
	 * @throws IllegalStateException
	 */
	protected void checkReadOnly() throws IllegalStateException {
		if(this.readOnly)
			throw new IllegalStateException("this instance has been set READ-ONLY " +
					"- no modificatons allowed; use #isReadOnly() to check beforehand");
	}
}
