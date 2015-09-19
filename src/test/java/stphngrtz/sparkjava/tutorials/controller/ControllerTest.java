package stphngrtz.sparkjava.tutorials.controller;

import org.junit.Before;
import org.junit.Test;
import stphngrtz.sparkjava.tutorials.model.Model;
import stphngrtz.sparkjava.tutorials.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerTest {

    private Controller sut;
    private Model model;

    @Before
    public void setUp() throws Exception {
        model = mock(Model.class);
        sut = new Controller(model);
    }

    @Test
    public void getAllPosts_will_return_all_posts_of_the_model() throws Exception {
        List<Post> posts = new ArrayList<>();
        posts.add(post("Title 1", "Content 1"));
        posts.add(post("Title 2", "Content 2"));
        posts.add(post("Title 3", "Content 3"));
        when(model.getAllPosts()).thenReturn(posts);

        assertThat(sut.getAllPosts(), equalTo(posts));
    }

    @Test(expected = Controller.InvalidDataException.class)
    public void createPost_will_throw_an_exeption_if_the_payload_has_no_title_and_no_content() throws Exception {
        sut.createPost(newPostPayload(null, null));
    }

    @Test(expected = Controller.InvalidDataException.class)
    public void createPost_will_throw_an_exeption_if_the_payload_has_no_title_but_a_content() throws Exception {
        sut.createPost(newPostPayload(null, "Example content"));
    }

    @Test
    public void createPost_will_return_the_uuid_created_by_the_model_if_the_payload_is_valid() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(model.createPost(anyString(), anyString())).thenReturn(uuid);
        assertThat(sut.createPost(newPostPayload("Example title", "Example content")), equalTo(uuid));
    }

    private static Post post(String title, String content) {
        Post post = mock(Post.class, title);
        when(post.getTitle()).thenReturn(title);
        when(post.getContent()).thenReturn(content);
        return post;
    }

    private static Controller.NewPostPayload newPostPayload(String title, String content) {
        Controller.NewPostPayload npp = new Controller.NewPostPayload();
        npp.setTitle(title);
        npp.setContent(content);
        return npp;
    }
}