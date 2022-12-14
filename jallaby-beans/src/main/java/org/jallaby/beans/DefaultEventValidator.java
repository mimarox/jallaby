package org.jallaby.beans;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlProperty;
import org.jallaby.event.Event;
import org.jallaby.event.EventValidator;

/**
 * The default event validator, checking whether a given event is defined in the XML declaration
 * of the state machine and has only declared properties.
 * 
 * @author Matthias Rothe
 */
public class DefaultEventValidator implements EventValidator {
	private final Set<EffectiveXmlEvent> events;
	
	/**
	 * Ctor.
	 * 
	 * @param events the effective events derived from the XML declaration of the state machine
	 */
	public DefaultEventValidator(final Set<EffectiveXmlEvent> events) {
		Objects.requireNonNull(events, "events must not be null");
		this.events = events;
	}
	
	/**
	 * Checks whether the given event is defined in the XML declaration of the state machine
	 * and has only declared properties.
	 * 
	 * @param event the event to validate
	 * @return <code>true</code> if and only if the given event is declared in the XML declaration
	 * of the state machine and has only declared properties, <code>false</code> otherwise.
	 */
	@Override
	public boolean isValidEvent(final Event event) {
		Optional<EffectiveXmlEvent> optionalEvent = events.stream().filter(
				e -> e.getName().equals(event.getEventName())).findFirst();
		
		if (optionalEvent.isPresent()) {
			for (String property : event.getPayload().keySet()) {
				Optional<EffectiveXmlProperty> optionalProperty = optionalEvent.get()
						.getProperties().stream().filter(p -> p.getName().equals(property))
						.findFirst();
				
				if (!optionalProperty.isPresent()) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
}
