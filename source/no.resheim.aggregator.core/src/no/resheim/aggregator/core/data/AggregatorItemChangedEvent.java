/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.data;

/**
 * This event type is used to add information to feed changed events.
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class AggregatorItemChangedEvent {

	/**
	 * @author   torkild
	 */
	public enum EventType {
		/**
		 * @uml.property  name="cREATED"
		 * @uml.associationEnd  
		 */
		CREATED,
		/**
		 * @uml.property  name="fAILED"
		 * @uml.associationEnd  
		 */
		FAILED,
		/**
		 * @uml.property  name="mOVED"
		 * @uml.associationEnd  
		 */
		MOVED,
		/**
		 * @uml.property  name="rEAD"
		 * @uml.associationEnd  
		 */
		READ,
		/**
		 * @uml.property  name="rEMOVED"
		 * @uml.associationEnd  
		 */
		REMOVED,
		/**
		 * @uml.property  name="rESTORED"
		 * @uml.associationEnd  
		 */
		RESTORED,
		/**
		 * @uml.property  name="sHIFTED"
		 * @uml.associationEnd  
		 */
		SHIFTED,
		/**
		 * @uml.property  name="cHANGED"
		 * @uml.associationEnd  
		 */
		CHANGED
	}

	/**
	 * Used in conjunction with the MOVED event type indicating that not only
	 * was the item moved, but it also got a new parent.
	 */
	public static final int NEW_PARENT = 0x1;

	/**
	 * @uml.property  name="items"
	 */
	private Object[] items;

	/**
	 * @uml.property  name="type"
	 * @uml.associationEnd  
	 */
	private EventType type;

	/**
	 * 
	 * @param items
	 *            the affected items
	 * @param type
	 *            the type of event
	 */
	public AggregatorItemChangedEvent(Object[] items, EventType type) {
		this.items = items;
		this.type = type;
	}

	/**
	 * @return
	 * @uml.property  name="items"
	 */
	public Object[] getItems() {
		return items;
	}

	/**
	 * @return
	 * @uml.property  name="type"
	 */
	public EventType getType() {
		return type;
	}
}
