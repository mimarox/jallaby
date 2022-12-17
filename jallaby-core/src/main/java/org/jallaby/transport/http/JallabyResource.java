/*
 * Copyright 2022, The Jallaby Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jallaby.transport.http;

import java.util.Map;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jallaby.Jallaby;
import org.jallaby.event.Event;
import org.jallaby.event.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/{stateMachineName}")
public class JallabyResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(JallabyResource.class);
	
	private final Jallaby jallaby = new Jallaby();
	private final ObjectMapper mapper = new ObjectMapper();

	@PUT
	@Path("/{instanceId}/{eventName}")
	public Response receiveEvent(@PathParam("stateMachineName") String stateMachineName,
			@PathParam("instanceId") String instanceId,
			@PathParam("eventName") String eventName,
			String entityString) {
		try {
			Map<String, Object> entity = mapper.readValue(entityString.getBytes(),
					new TypeReference<Map<String, Object>>() {});
			
			Event event = new Event(stateMachineName, instanceId, eventName, entity);
			return Response.ok(toJson(jallaby.receiveEvent(event)), MediaType.APPLICATION_JSON).build();
		} catch (EventProcessingException e) {
			LOGGER.warn(String.format("An exception occurred while processing the event [%s]"
					+ " on state machine [%s/%s].",
					eventName, stateMachineName, instanceId), e);
			return Response.status(900).type(MediaType.APPLICATION_JSON)
					.entity(toJson(e.getError())).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurred.", e);
			return Response.status(901).type(MediaType.APPLICATION_JSON).entity(
					toJson(new GenericError(e.getClass().getCanonicalName(), e.getMessage()))).build();
		}
	}
	
	private String toJson(final Object entity) {
		try {
			return mapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			LOGGER.warn("Couldn't convert entity to JSON. Using toString() method of entity.", e);
			return String.format("{ \"value\": \"%s\" }", entity.toString());
		}
	}
}
