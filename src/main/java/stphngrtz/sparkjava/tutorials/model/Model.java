package stphngrtz.sparkjava.tutorials.model;

import java.util.List;
import java.util.UUID;

public interface Model {
    UUID createPost(String title, String content);
    UUID createComment(UUID post, String author, String content);
    List<Post> getAllPosts();
    List<Comment> getAllCommentsOn(UUID post);
    boolean existPost(UUID post);
}
