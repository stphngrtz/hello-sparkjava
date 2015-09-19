package stphngrtz.sparkjava.tutorials.model;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Sql2oModel implements Model {

    private final Sql2o sql2o;

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        initialize();
    }

    private void initialize() {
        try (Connection c = sql2o.open()) {
            c.createQuery(
                    "CREATE TABLE posts (\n" +
                    "    uuid uuid primary key,\n" +
                    "    title text not null,\n" +
                    "    content text,\n" +
                    "    publishing_date date\n" +
                    ");")
                    .executeUpdate();
            c.createQuery(
                    "CREATE TABLE comments (\n" +
                    "    uuid uuid primary key,\n" +
                    "    post_uuid uuid references posts(uuid),\n" +
                    "    author text,\n" +
                    "    content text,\n" +
                    "    approved bool,\n" +
                    "    submission_date date\n" +
                    ");")
                    .executeUpdate();

            c.commit();
        }
    }

    @Override
    public UUID createPost(String title, String content) {
        try (Connection c = sql2o.open()) { // if we want to execute several operations atomically (i.e., so if one fails no changes are persisted) we use a .beginTransaction(), otherwise we just open a connection.
            UUID uuid = UUID.randomUUID();
            c.createQuery("insert into posts(uuid, title, content, publishing_date) values (:uuid, :title, :content, :publishing_date)")
                    .addParameter("uuid", uuid)
                    .addParameter("title", title)
                    .addParameter("content", content)
                    .addParameter("publishing_date", new Date())
                    .executeUpdate();
            c.commit();
            return uuid;
        }
    }

    @Override
    public UUID createComment(UUID post, String author, String content) {
        try (Connection c = sql2o.open()) {
            UUID uuid = UUID.randomUUID();
            c.createQuery("insert into comments(uuid, post_uuid, author, content, approved, submission_date) values (:uuid, :post_uuid, :author, :content, :approved, :submission_date)")
                    .addParameter("uuid", uuid)
                    .addParameter("post_uuid", post)
                    .addParameter("author", author)
                    .addParameter("content", content)
                    .addParameter("approved", false)
                    .addParameter("submission_date", new Date())
                    .executeUpdate();
            c.commit();
            return uuid;
        }
    }

    @Override
    public List<Post> getAllPosts() {
        try (Connection c = sql2o.open()) {
            return c.createQuery("select uuid, title, content, publishing_date as publishingDate from posts").executeAndFetch(Post.class);
        }
    }

    @Override
    public List<Comment> getAllCommentsOn(UUID post) {
        try (Connection c = sql2o.open()) {
            return c.createQuery("select uuid, post_uuid as post, author, content, approved, submission_date as submissionDate from comments where post_uuid=:post_uuid")
                    .addParameter("post_uuid", post)
                    .executeAndFetch(Comment.class);
        }
    }

    @Override
    public boolean existPost(UUID post) {
        try (Connection c = sql2o.open()) {
            return c.createQuery("select count(*) from posts where uuid=:uuid")
                    .addParameter("uuid", post)
                    .executeScalar(Integer.class) > 0;
        }
    }
}
