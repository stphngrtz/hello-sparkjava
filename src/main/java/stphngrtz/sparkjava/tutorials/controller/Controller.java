package stphngrtz.sparkjava.tutorials.controller;

import stphngrtz.sparkjava.tutorials.model.Comment;
import stphngrtz.sparkjava.tutorials.model.Model;
import stphngrtz.sparkjava.tutorials.model.Post;

import java.util.List;
import java.util.UUID;

public class Controller {

    private final Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public List<Post> getAllPosts() {
        return model.getAllPosts();
    }

    public UUID createPost(NewPostPayload npp) throws InvalidDataException {
        if (!npp.isValid())
            throw new InvalidDataException();

        return model.createPost(npp.getTitle(), npp.getContent());
    }

    public List<Comment> getAllComments(UUID post) throws InvalidDataException {
        if (!model.existPost(post))
            throw new InvalidDataException();

        return model.getAllCommentsOn(post);
    }

    public UUID createComment(UUID post, NewCommentPayload ncp) throws InvalidDataException {
        if (!model.existPost(post))
            throw new InvalidDataException();

        if (!ncp.isValid())
            throw new InvalidDataException();

        return model.createComment(post, ncp.getAuthor(), ncp.getContent());
    }

    public static class InvalidDataException extends Exception {
    }

    public static class NewPostPayload {
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

    public static class NewCommentPayload {
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
