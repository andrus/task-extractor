package org.objectstyle.taskextractor.jaxrs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import org.objectstyle.taskextractor.Branch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class BranchesMessageBodyReader implements MessageBodyReader<Collection<Branch>> {

    private final ObjectMapper mapper;

    public BranchesMessageBodyReader(ObjectMapper mapper) {
        this.mapper = mapper;
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
        return params != null && params.length == 1 && Branch.class.equals(params[0]);
    }

    @Override
    public Collection<Branch> readFrom(
            Class<Collection<Branch>> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {

        TypeReference<Collection<ProtocolBranch>> ref = new TypeReference<Collection<ProtocolBranch>>() {
        };

        Collection<ProtocolBranch> values = mapper.readValue(entityStream, ref);

        Collection<Branch> branches = new ArrayList<>(values.size());
        values.forEach(v -> branches.add(toBranch(v)));
        return branches;
    }

    private Branch toBranch(ProtocolBranch value) {

        String name = value.name;
        String sha = value.commit != null ? value.commit.sha : null;

        return new Branch(name, sha);
    }

    static class ProtocolBranch {

        private String name;
        private ProtocolBranchCommit commit;

        public void setCommit(ProtocolBranchCommit commit) {
            this.commit = commit;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class ProtocolBranchCommit {
        private String sha;

        public void setSha(String sha) {
            this.sha = sha;
        }
    }
}
