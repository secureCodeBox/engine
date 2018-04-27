/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.scanprocess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Little helper to wrap and unwrap jsonified Objects.
 * <strong>This class is not meant to inherit from it!</strong>
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 27.04.18
 */
public class ProcessVariableHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessVariableHelper.class);

    protected static ObjectMapper objectMapper;

    protected ProcessVariableHelper() {
        // Not instanceable
        // ObjectMapper normaly gets initialised by SecureCodeBox engine!
    }

    private static void initIfNotHappend() {
        if (objectMapper == null) {
            LOG.error(
                    "The object mapper was not init! Falling back to default object mapper by calling new ObjectMapper()!!!!");
            objectMapper = new ObjectMapper();
        }
    }

    /**
     * Tries to read a List of innerClass objects from data.
     * <p>
     *
     * @param data       the String containing an json list
     * @param innerClass the class which should be constructed
     * @param <T>        the return type
     *
     * @return List of innerClass objects, if not successfull it returns an empty {@link LinkedList}.
     */
    public static <T> List<T> readListFromValue(String data, Class<T> innerClass) {
        try {
            initIfNotHappend();
            return objectMapper.readValue(data,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, innerClass));
        } catch (IOException e) {
            LOG.error("Can't extract json data type {} string! Raw Data {}", innerClass.getCanonicalName(), data, e);
            return new LinkedList<>();
        }
    }

    /**
     * Wraps the given Object as camunda spin {@link ObjectValue}.
     * <i>Important:</i> Uses {@link Variables.SerializationDataFormats#JSON} as serialization type!
     *
     * @return value wrapped as camunda spin {@link ObjectValue}
     */
    public static ObjectValue generateObjectValue(Object value) {
        try {
            initIfNotHappend();
            return Variables.objectValue(objectMapper.writeValueAsString(value))
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create();
        } catch (JsonProcessingException e) {
            LOG.error("Can't write generate object value for dield!", DefaultFields.PROCESS_FINDINGS, e);
            throw new IllegalStateException("Can't write field to process!", e);
        }
    }
}
