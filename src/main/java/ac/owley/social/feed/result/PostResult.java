package ac.owley.social.feed.result;

import java.util.Map;

import org.neo4j.graphdb.Node;

public class PostResult
{

    public Node post;

    public Node author;

    // POSTED | REPOSTED | COMMENTED


    public PostResult(Node post) {
        this.post = post;
    }

    public PostResult(Node post, Node author) {
        this.post = post;
        this.author = author;
    }

    public Node getPost() {
        return post;
    }
}
