package org.eclipse.om2m.OM2M;
/**
 * Interface for the mapping operations
 *
 */
public interface MapperInterface {
	/**
	 * Marshal operation object to string (xml)
	 * @param obj
	 * @return xml representation
	 */
	public String marshal(Object obj);

	/**
	 * Unmarshal operation string (xml) to object
	 * @param representation (xml)
	 * @return java object
	 */
	public Object unmarshal(String representation);
}
