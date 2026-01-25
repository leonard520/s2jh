/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.web.json;

import lab.s2jh.core.entity.BaseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * Jackson ObjectMapper configured for testing with @JsonFilter support
 */
public class TestObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    public TestObjectMapper() {
        super();
        
        // Register Hibernate4 module for lazy loading support
        Hibernate4Module hibernateModule = new Hibernate4Module();
        hibernateModule.configure(Hibernate4Module.Feature.FORCE_LAZY_LOADING, false);
        this.registerModule(hibernateModule);
        
        // Configure FilterProvider for @JsonFilter on BaseEntity
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(BaseEntity.DEFAULT_JSON_FILTER_NAME, 
            SimpleBeanPropertyFilter.serializeAll());
        this.setFilterProvider(filterProvider);
        
        // Configure serialization
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Configure deserialization
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
