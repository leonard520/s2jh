package lab.s2jh.core.web.mvc;

import java.util.List;

import lab.s2jh.core.entity.BaseEntity;
import lab.s2jh.core.web.mvc.interceptor.TokenHandlerInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * Spring MVC Configuration - replacement for struts.xml configuration
 * 
 * Configures:
 * - View resolvers (JSP)
 * - Content negotiation (extensionless URLs)
 * - Message converters (Jackson JSON)
 * - Interceptors (Token validation)
 * - Multipart file upload
 * 
 * Note: Excluded from test profile to avoid ServletContext requirement in service tests
 */
@Configuration
@EnableWebMvc
@Profile("!test")
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired(required = false)
    private TokenHandlerInterceptor tokenHandlerInterceptor;

    /**
     * Configure JSP view resolver (Spring 3.2 style - use @Bean instead of ViewResolverRegistry)
     */
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(1);
        return resolver;
    }

    /**
     * Configure content negotiation for extensionless URLs
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            // Disable path extension content negotiation
            .favorPathExtension(false)
            // Enable parameter-based content negotiation
            .favorParameter(true)
            .parameterName("format")
            // Use Accept header
            .ignoreAcceptHeader(false)
            // Default to JSON for API responses
            .defaultContentType(MediaType.APPLICATION_JSON)
            // Map format parameter values
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("html", MediaType.TEXT_HTML);
    }

    /**
     * Configure Jackson message converter with Hibernate support
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(converter);
    }

    /**
     * Configure ObjectMapper with Hibernate4 module
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Hibernate4 module for lazy loading support
        Hibernate4Module hibernateModule = new Hibernate4Module();
        hibernateModule.configure(Hibernate4Module.Feature.FORCE_LAZY_LOADING, false);
        mapper.registerModule(hibernateModule);
        
        // Configure FilterProvider for @JsonFilter on BaseEntity
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(BaseEntity.DEFAULT_JSON_FILTER_NAME, 
            SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filterProvider);
        
        // Configure serialization
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Configure deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }

    /**
     * Register interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (tokenHandlerInterceptor != null) {
            registry.addInterceptor(tokenHandlerInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/assets/**", "/resources/**", "/pub/**");
        }
    }

    /**
     * Configure static resource handling
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/assets/");
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
    }

    /**
     * Enable default servlet for static resources
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * Configure multipart file upload resolver
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(10485760); // 10MB
        resolver.setMaxInMemorySize(4096);
        resolver.setDefaultEncoding("UTF-8");
        return resolver;
    }
}
