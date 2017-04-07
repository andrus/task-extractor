package org.objectstyle.taskextractor.jaxrs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.bootique.jackson.JacksonService;
import org.objectstyle.taskextractor.Commit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Provider
public class CommitsMessageBodyReader implements MessageBodyReader<Collection<Commit>> {

    private JacksonService jacksonService;

    public CommitsMessageBodyReader(JacksonService jacksonService) {
        this.jacksonService = jacksonService;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!Collection.class.isAssignableFrom(type)) {
            return false;
        }

        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }

        Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();
        return params != null && params.length == 1 && Commit.class.equals(params[0]);
    }

    @Override
    public Collection<Commit> readFrom(Class<Collection<Commit>> type,
                                       Type genericType,
                                       Annotation[] annotations,
                                       MediaType mediaType,
                                       MultivaluedMap<String, String> httpHeaders,
                                       InputStream entityStream) throws IOException, WebApplicationException {

        TypeReference<Collection<ProtocolCommit>> ref = new TypeReference<Collection<ProtocolCommit>>() {
        };

        Collection<ProtocolCommit> values = jacksonService
                .newObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(entityStream, ref);

        Collection<Commit> commits = new ArrayList<>(values.size());
        values.forEach(v -> commits.add(toCommit(v)));
        return commits;
    }

    private Commit toCommit(ProtocolCommit value) {
        
        Commit commit = new Commit();

        commit.setHash(value.sha);

        if (value.author != null) {
            commit.setUser(value.author.login);
        }

        if (value.commit != null) {
            commit.setMessage(value.commit.message);

            if (value.commit.author != null) {
                commit.setTime(value.commit.author.date);
            }
        }

        return commit;
    }

    static class ProtocolCommit {

        private String sha;
        private ProtocolAuthor author;
        private ProtocolCommitCommit commit;

        public void setSha(String sha) {
            this.sha = sha;
        }

        public void setAuthor(ProtocolAuthor author) {
            this.author = author;
        }

        public void setCommit(ProtocolCommitCommit commit) {
            this.commit = commit;
        }
    }

    static class ProtocolAuthor {
        private String login;

        public void setLogin(String login) {
            this.login = login;
        }
    }

    static class ProtocolCommitCommit {
        private String message;
        private ProtocolCommitAuthor author;

        public void setMessage(String message) {
            this.message = message;
        }

        public void setAuthor(ProtocolCommitAuthor author) {
            this.author = author;
        }
    }

    static class ProtocolCommitAuthor {

        private ZonedDateTime date;

        public void setDate(ZonedDateTime date) {
            this.date = date;
        }
    }
}
