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

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jallaby.Jallaby;
import org.jallaby.event.Event;
import org.jallaby.event.EventProcessingException;

@Path("/{stateMachineName}")
public class JallabyResource {
	private Jallaby jallaby = new Jallaby();
	
	@PUT
	@Path("/{instanceId}/{eventName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object receiveEvent(@PathParam("stateMachineName") String stateMachineName,
			@PathParam("instanceId") String instanceId,
			@PathParam("eventName") String eventName,
			Map<String, Object> entity) {

		try {
			Event event = new Event(stateMachineName, instanceId, eventName, entity);
			return jallaby.receiveEvent(event);
		} catch (EventProcessingException e) {
			return Response.status(503).entity(e.getError()).build();
		} catch (Exception e) {
			return Response.status(503).entity(
					new GenericError(e.getClass().getCanonicalName(), e.getMessage())).build();
		}
	}
}
