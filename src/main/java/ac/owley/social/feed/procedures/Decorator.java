package ac.owley.social.feed.procedures;

import ac.owley.social.feed.Properties;
import ac.owley.social.feed.RelationshipTypes;
import ac.owley.social.feed.result.PostResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;

public class Decorator
{


    private final static int INTERACTION_USERS = 2;

    public static Map<String, Object> decoratePost(Node post) {
        Map<String, Object> output = new HashMap<>();

        output.put( Properties.postId, post.getProperty(Properties.postId) );
        output.put( Properties.postCreatedAt, post.getProperty(Properties.postCreatedAt) );
        output.put( Properties.postBody, post.getProperty(Properties.postBody) );

        // POSTED
//        Node author = post.getSingleRelationship( RelationshipTypes.POSTED, Direction.INCOMING ).getStartNode();

//        output.put( "author", decorateAuthor( author ));

        // REPOSTED,
        output.putAll( decorateInteraction(post, "reposted", RelationshipTypes.REPOSTED, Direction.INCOMING ) );

        // COMMENTED
        output.putAll( decorateInteraction(post, "reposted", RelationshipTypes.COMMENTED, Direction.INCOMING ) );

        return output;
    }

    public static Map<String, Object> decorateAuthor(Node author) {
        Map<String, Object> output = new HashMap<>();

        output.put( Properties.username, author.getProperty(Properties.username) );

        return output;
    }

    public static Map<String, Object> decorateInteraction(Node post, String key, RelationshipType relationshipType, Direction direction) {
        Map<String, Object> output = new HashMap<>();

        // Count
        int degree = post.getDegree(relationshipType, direction);
        output.put(key +"_count", degree);

        // People who have performed the interaction
        if ( degree > 0 ) {
            Iterator<Relationship> rels = post.getRelationships(relationshipType, direction).iterator();

            List<String> interactors = new ArrayList<>();

            int names = 0;

            while ( rels.hasNext() && names < INTERACTION_USERS ) {
                Relationship rel = rels.next();

                Node other = rel.getOtherNode(post);

                interactors.add( (String) other.getProperty( Properties.username ) );
            }

            output.put(key +"_users", interactors);
        }

        //

    }

}
