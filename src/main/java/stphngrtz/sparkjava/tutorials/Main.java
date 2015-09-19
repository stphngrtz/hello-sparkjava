package stphngrtz.sparkjava.tutorials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class Main {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_OK = 200;

    public static void main(String[] args) {
        Model model = new Model();
        model.createPost("Example", "This is just an example post.");

        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        get("/hello", (req, res) -> "Hello World!");

        post("/posts", (req, res) -> {
            NewPostPayload npp = om.readValue(req.body(), NewPostPayload.class);
            if (!npp.isValid()) {
                res.status(HTTP_BAD_REQUEST);
                return "";
            }
            int id = model.createPost(npp.getTitle(), npp.getContent());
            res.status(HTTP_OK);
            res.type("application/json");
            return id;
        });

        get("/posts", (request, response) -> {
            response.status(200);
            response.type("application/json");

            StringWriter sw = new StringWriter();
            om.writeValue(sw, model.getAllPosts());
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

    private static class Model {
        private Integer nextId = 1;
        private Map<Integer, Post> posts = new HashMap<>();

        public Integer createPost(String title, String content) {
            Integer id = nextId++;
            posts.put(id, new Post(id, title, content));
            return id;
        }

        public Collection<Post> getAllPosts() {
            return posts.values();
        }

        private static class Post {
            private Integer id;
            private String title;
            private String content;

            public Post() {
            }

            public Post(Integer id, String title, String content) {
                this.id = id;
                this.title = title;
                this.content = content;
            }

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

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
        }
    }
}
