package org.objectstyle.taskextractor.jaxrs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.dflib.DataFrame;
import com.nhl.dflib.Extractor;
import org.objectstyle.taskextractor.Commit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;

@Provider
public class CommitsMessageBodyReader implements MessageBodyReader<DataFrame> {

    private final ObjectMapper mapper;

    public CommitsMessageBodyReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataFrame.class.isAssignableFrom(type);
    }

    @Override
    public DataFrame readFrom(
            Class<DataFrame> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {

        TypeReference<Collection<ProtocolCommit>> ref = new TypeReference<>() {
        };

        return DataFrame.byRow(
                        Extractor.$col(this::zTime),
                        Extractor.$col(pc -> null),
                        Extractor.$col(pc -> pc.commit != null ? pc.commit.message : null),
                        Extractor.$col(pc -> pc.author != null ? pc.author.login : null),
                        Extractor.$col(pc -> pc.sha)
                )
                .columnIndex(Commit.index())
                .ofIterable(mapper.readValue(entityStream, ref));
    }

    private ZonedDateTime zTime(ProtocolCommit pc) {
        ZonedDateTime time = pc.commit != null && pc.commit.author != null ? pc.commit.author.date : null;
        return time != null ? ZonedDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC) : null;
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
