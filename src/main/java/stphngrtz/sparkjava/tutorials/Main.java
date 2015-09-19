package stphngrtz.sparkjava.tutorials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.NoQuirks;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;
import stphngrtz.sparkjava.tutorials.controller.Controller;
import stphngrtz.sparkjava.tutorials.model.Comment;
import stphngrtz.sparkjava.tutorials.model.Model;
import stphngrtz.sparkjava.tutorials.model.Post;
import stphngrtz.sparkjava.tutorials.model.Sql2oModel;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

public class Main {

    private static final int BAD_REQUEST = 400;
    private static final int OK = 200;
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_HTML = "text/html";

    public static void main(String[] args) {
        // port(4568);

        Sql2o sql2o = new Sql2o("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "username", "password", new NoQuirks() {{
            converters.put(UUID.class, new UUIDConverter());
        }});
        Model model = new Sql2oModel(sql2o);
        model.createPost("Example", "This is just an example post.");

        Controller controller = new Controller(model);

        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        Configuration configuration = new Configuration();
        configuration.setTemplateLoader(new ClassTemplateLoader(Main.class, "/"));
        freeMarkerEngine.setConfiguration(configuration);

        get("/hello", (req, res) -> "Hello World!");

        post("/posts", (request, response) -> {
            response.type(APPLICATION_JSON);

            try {
                UUID post = controller.createPost(om.readValue(request.body(), Controller.NewPostPayload.class));
                response.status(OK);
                return post;
            } catch (Controller.InvalidDataException e) {
                response.status(BAD_REQUEST);
                return "";
            }
        });

        get("/posts", (request, response) -> {
            List<Post> allPosts = controller.getAllPosts();
            if (shouldReturnHtml(request)) {
                response.type(TEXT_HTML);
                response.status(OK);

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("posts", allPosts);
                return freeMarkerEngine.render(new ModelAndView(attributes, "stphngrtz/sparkjava/tutorials/posts.html"));
            } else {
                response.type(APPLICATION_JSON);
                response.status(OK);

                StringWriter sw = new StringWriter();
                om.writeValue(sw, allPosts);
                return sw.toString();
            }
        });

        post("/posts/:uuid/comments", (request, response) -> {
            response.type(APPLICATION_JSON);

            try {
                UUID comment = controller.createComment(UUID.fromString(request.params(":uuid")), om.readValue(request.body(), Controller.NewCommentPayload.class));
                response.status(OK);
                return comment;
            } catch (Controller.InvalidDataException e) {
                response.status(BAD_REQUEST);
                return "";
            }
        });

        get("/posts/:uuid/comments", (request, response) -> {
            response.type(APPLICATION_JSON);

            try {
                List<Comment> allComments = controller.getAllComments(UUID.fromString(request.params(":uuid")));
                response.status(OK);

                StringWriter sw = new StringWriter();
                om.writeValue(sw, allComments);
                return sw.toString();
            } catch (Controller.InvalidDataException e) {
                response.status(BAD_REQUEST);
                return "";
            }
        });
    }

    private static boolean shouldReturnHtml(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains(TEXT_HTML);
    }
}
