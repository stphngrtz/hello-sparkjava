package stphngrtz.sparkjava.tutorials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.NoQuirks;
import stphngrtz.sparkjava.tutorials.model.Model;
import stphngrtz.sparkjava.tutorials.model.Sql2oModel;

import java.io.StringWriter;
import java.util.UUID;

import static spark.Spark.*;

public class Main {

    private static final int BAD_REQUEST = 400;
    private static final int OK = 200;
    private static final String APPLICATION_JSON = "application/json";

    public static void main(String[] args) {
        // port(4568);

        Sql2o sql2o = new Sql2o("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "username", "password", new NoQuirks() {{
            converters.put(UUID.class, new UUIDConverter());
        }});
        Model model = new Sql2oModel(sql2o);
        model.createPost("Example", "This is just an example post.");

        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        get("/hello", (req, res) -> "Hello World!");

        post("/posts", (req, res) -> {
            res.type(APPLICATION_JSON);

            NewPostPayload npp = om.readValue(req.body(), NewPostPayload.class);
            if (!npp.isValid()) {
                res.status(BAD_REQUEST);
                return "";
            }
            UUID uuid = model.createPost(npp.getTitle(), npp.getContent());
            res.status(OK);
            return uuid;
        });

        get("/posts", (request, response) -> {
            response.type(APPLICATION_JSON);
            response.status(OK);

            StringWriter sw = new StringWriter();
            om.writeValue(sw, model.getAllPosts());
            return sw.toString();
        });

        post("/posts/:uuid/comments", (request, response) -> {
            response.type(APPLICATION_JSON);

            UUID post = UUID.fromString(request.params(":uuid"));
            if (!model.existPost(post)) {
                response.status(BAD_REQUEST);
                return "";
            }

            NewCommentPayload ncp = om.readValue(request.body(), NewCommentPayload.class);
            if (!ncp.isValid()) {
                response.status(BAD_REQUEST);
                return "";
            }
            UUID comment = model.createComment(post, ncp.getAuthor(), ncp.getContent());
            response.status(OK);
            return comment;
        });

        get("/posts/:uuid/comments", (request, response) -> {
            response.type(APPLICATION_JSON);

            UUID post = UUID.fromString(request.params(":uuid"));
            if (!model.existPost(post)) {
                response.status(BAD_REQUEST);
                return "";
            }
            response.status(OK);
            StringWriter sw = new StringWriter();
            om.writeValue(sw, model.getAllCommentsOn(post));
            return sw.toString();
        });
    }

    private static class NewPostPayload {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isValid() {
            return title != null && content != null;
        }
    }

    private static class NewCommentPayload {
        private String author;
        private String content;

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isValid() {
            return author != null && content != null;
        }
    }
}
