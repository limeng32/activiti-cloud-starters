/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.starter.audit.mongo.tests.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.audit.mongo.EventsMongoRepository;
import org.activiti.services.audit.mongo.entity.EventLogDocument;
import org.activiti.starters.test.MockProcessEngineEvent;
import org.activiti.starters.test.MyProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class AuditServiceIT {

    @Autowired
    private EventsRestTemplate eventsRestTemplate;

    @Autowired
    private EventsMongoRepository repository;

    @Autowired
    private MyProducer producer;

    @Before
    public void setUp() throws Exception {
        repository.deleteAll();
    }

    @Test
    public void findAllShouldReturnAllAvailableEvents() throws Exception {
        //given
        List<ProcessEngineEvent> coveredEvents = getCoveredEvents();
        producer.send(coveredEvents.toArray(new ProcessEngineEvent[coveredEvents.size()]));
        waitForMessage();

        //when
        ResponseEntity<PagedResources<Map<String, Object>>> eventsPagedResources = eventsRestTemplate.executeFindAll();

        //then
        Collection<Map<String, Object>> messageList = eventsPagedResources.getBody().getContent();
        List<ProcessEngineEvent> retrievedEvents = convert(messageList);
        assertThat(retrievedEvents).hasSameSizeAs(coveredEvents);
        for (ProcessEngineEvent coveredEvent : coveredEvents) {
            assertThat(retrievedEvents)
                                       .extracting(
                                                   ProcessEngineEvent::getEventType,
                                                   ProcessEngineEvent::getExecutionId,
                                                   ProcessEngineEvent::getProcessDefinitionId,
                                                   ProcessEngineEvent::getProcessInstanceId)
                                       .contains(tuple(coveredEvent.getEventType(),
                                                       coveredEvent.getExecutionId(),
                                                       coveredEvent.getProcessDefinitionId(),
                                                       coveredEvent.getProcessInstanceId()));
        }
    }

    private List<ProcessEngineEvent> getCoveredEvents() {
        List<ProcessEngineEvent> coveredEvents = new ArrayList<>();
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityCancelledEvent",
                                                     "100",
                                                     "103",
                                                     "104"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityStartedEvent",
                                                     "2",
                                                     "3",
                                                     "4"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityCompletedEvent",
                                                     "11",
                                                     "23",
                                                     "42"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessCompletedEvent",
                                                     "12",
                                                     "24",
                                                     "43"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessCancelledEvent",
                                                     "112",
                                                     "124",
                                                     "143"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessStartedEvent",
                                                     "13",
                                                     "25",
                                                     "44"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "SequenceFlowTakenEvent",
                                                     "14",
                                                     "26",
                                                     "45"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskAssignedEvent",
                                                     "15",
                                                     "27",
                                                     "46"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskCompletedEvent",
                                                     "16",
                                                     "28",
                                                     "47"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskCreatedEvent",
                                                     "17",
                                                     "29",
                                                     "48"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableCreatedEvent",
                                                     "18",
                                                     "30",
                                                     "49"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableDeletedEvent",
                                                     "19",
                                                     "31",
                                                     "50"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableUpdatedEvent",
                                                     "20",
                                                     "32",
                                                     "51"));
        return coveredEvents;
    }

    @SuppressWarnings("unchecked")
    private List<ProcessEngineEvent> convert(Collection<Map<String, Object>> messageList) throws JsonParseException,
                                                                        JsonMappingException,
                                                                        IOException {
        List<ProcessEngineEvent> list = new ArrayList<>();
        for (Map<String, Object> messaage : messageList) {
            list.add(new MockProcessEngineEvent((Long) ((Map<String, Object>) messaage.get("content")).get("timestamp"),
                                                (String) ((Map<String, Object>) messaage.get("content")).get("eventType"),
                                                (String) ((Map<String, Object>) messaage.get("content")).get("executionId"),
                                                (String) ((Map<String, Object>) messaage.get("content")).get("processDefinitionId"),
                                                (String) ((Map<String, Object>) messaage.get("content")).get("processInstanceId")));
        }
        return list;
    }

    private void waitForMessage() throws InterruptedException {
        //FIXME improve the waiting mechanism
        Thread.sleep(500);
    }

    @Test
    public void findByIdShouldReturnTheEventIdentifiedByTheGivenId() throws Exception {
        //given
        ProcessEngineEvent[] events = new ProcessEngineEvent[1];
        events[0] = new MockProcessEngineEvent(System.currentTimeMillis(),
                                               "ActivityStartedEvent",
                                               "2",
                                               "3",
                                               "4");
        producer.send(events);

        waitForMessage();

        ResponseEntity<PagedResources<Map<String, Object>>> eventsPagedResources = eventsRestTemplate.executeFindAll();
        assertThat(eventsPagedResources.getBody().getContent()).isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> event = (Map<String, Object>) eventsPagedResources.getBody()
                                                                              .getContent()
                                                                              .iterator()
                                                                              .next()
                                                                              .get("content");

        //when
        ResponseEntity<EventLogDocument> responseEntity = eventsRestTemplate.executeFindById((String) event.get("id"));

        //then
        assertEquals(((Map<String, Object>) responseEntity.getBody().get("content")).get("id"), event.get("id"));
        assertEquals(((Map<String, Object>) responseEntity.getBody().get("content")).get("eventType"),
                     "ActivityStartedEvent");
        assertEquals(((Map<String, Object>) responseEntity.getBody().get("content")).get("executionId"), "2");
        assertEquals(((Map<String, Object>) responseEntity.getBody().get("content")).get("processDefinitionId"), "3");
        assertEquals(((Map<String, Object>) responseEntity.getBody().get("content")).get("processInstanceId"), "4");
    }

}