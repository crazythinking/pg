package net.engining.pg.support.db.querydsl;

import java.io.Serializable;

/**
 * The range of interest for a single handler.
 * 
 * @author luxue
 *
 */
public class Range implements Serializable {

	private static final long serialVersionUID = 1L;
	private int length;
	private int start;

	/**
	 * Construct a new {@link Range}.
	 *
	 * @param start
	 *            the start index
	 * @param length
	 *            the length
	 */
	public Range(int start, int length) {
		this.start = start;
		this.length = length;
	}

	/**
	 * Used by RPC.
	 */
	Range() {
	}

	/**
	 * Return true if this ranges's start end length are equal to those of the
	 * given object.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Range)) {
			return false;
		}
		Range r = (Range) o;
		return start == r.getStart() && length == r.getLength();
	}

	/**
	 * Get the length of the range.
	 *
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Get the start index of the range.
	 *
	 * @return the start index
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Return a hash code based on this range's start and length.
	 */
	@Override
	public int hashCode() {
		return (length * 31) ^ start;
	}

	/**
	 * Returns a String representation for debugging.
	 */
	@Override
	public String toString() {
		return "Range(" + start + "," + length + ")";
	}
}
