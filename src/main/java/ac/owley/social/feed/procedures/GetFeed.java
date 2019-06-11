package ac.owley.social.feed.procedures;

import ac.owley.social.feed.Labels;
import ac.owley.social.feed.Properties;
import ac.owley.social.feed.RelationshipTypes;
import ac.owley.social.feed.Time;
import ac.owley.social.feed.result.PostResult;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.logging.Log;

import static ac.owley.social.feed.Time.POSTED_ON;
import static java.util.Comparator.reverseOrder;

public class GetFeed
{

    private final static String CURSOR_TYPE_BEFORE = "before";
    private final static String CURSOR_TYPE_AFTER = "after";


    private final GraphDatabaseService db;

    private final Log log;

    public GetFeed( GraphDatabaseService db, Log log ) {
        this.db = db;
        this.log = log;
    }

    public Stream<PostResult> forUser(String username, String cursorType, String sinceId, Double limit) {
        // Get User
        Node user = getUser(username);

        if ( user == null ) {
            log.debug( "Cannot find user "+ username +". Returning empty stream" );
            return Stream.empty();
        }

        // Get Following
        Set following = getFollowing( user );

        if ( following.size() == 0 ) {
            log.debug( "User"+ username +" isn't following anyone. Returning empty stream" );
            return Stream.empty();
        }

        // Get Date of last/next post
        ZonedDateTime dateTime = ZonedDateTime.now();

        if ( sinceId != null && !sinceId.equals("") ) {
            dateTime = getPostTime( sinceId );
        }

        // Correct Cursor Type
        if ( cursorType == null || ( !cursorType.equals(CURSOR_TYPE_BEFORE) && !cursorType.equals(CURSOR_TYPE_AFTER) ) ) {
            cursorType = CURSOR_TYPE_BEFORE;
        }

        if ( cursorType.equals(CURSOR_TYPE_AFTER) ) {
//            return getPostsAfter(following, dateTime);
        }

        // Get the next X posts before this post
        return getPostsBefore(following, dateTime, limit);
    }

    private Node getUser(String username) {
        return db.findNode( Labels.User, Properties.username, username );
    }

    private Set<Node> getFollowing(Node user) {
        Set<Node> users = new HashSet<>(  );

        for ( Relationship rel : user.getRelationships( RelationshipTypes.FOLLOWS, Direction.OUTGOING ) ) {
            users.add( rel.getOtherNode( user ) );
        }

        for ( Relationship rel : user.getRelationships( RelationshipTypes.FRIEND_OF, Direction.OUTGOING ) ) {
            users.add( rel.getOtherNode( user ) );
        }

        return users;
    }

    private ZonedDateTime getPostTime(String postId) {
        Node post = db.findNode( Labels.Post, Properties.postId, postId );

        ResourceIterator<Node> nodes = db.findNodes( Labels.Post );

        if ( post != null ) {
            return (ZonedDateTime) post.getProperty( Properties.postCreatedAt, ZonedDateTime.now() );
        }

        log.debug( "Cannot find post "+ postId +". Using now() as value" );

        return ZonedDateTime.now();
    }

    // TODO: getPostsAfter
    // Same as below, but run in reverse order

    private Stream<PostResult> getPostsBefore(Set users, ZonedDateTime dateTime, Double limit) {
        List<PostResult> output = new ArrayList<>(  );

        ZonedDateTime originalDateTime = dateTime;

        // Set a minimum date to stop the code running forever
        ZonedDateTime floor = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/London]");

        while ( output.size() < limit && dateTime.isAfter( floor ) ) {
            List<Node> posts = getPostsOnDate(users, dateTime, reverseOrder());

            posts.forEach( n -> {
                ZonedDateTime postCreatedAt = (ZonedDateTime) ((Node) n).getProperty( Properties.postCreatedAt );

                // Add to the
                if ( postCreatedAt.isBefore( originalDateTime ) ) {
                    output.add( new PostResult( n ) );
                }
            } );

            // Try again with the day before
            dateTime = dateTime.minusDays(1);
        }

        // Trim to size and return
        return output.subList( 0, Math.min(limit.intValue(), output.size()) ).stream();
    }

    private List<Node> getPostsOnDate(Set<Node> users, ZonedDateTime date, Comparator comparator) {
        List<Node> output = new ArrayList<>(  );

        // Get all posts from each user on the date
        RelationshipType relType = RelationshipType.withName( String.format(POSTED_ON,  date.format( Time.formatter )) );

        for ( Node user : users ) {
            for ( Relationship rel: user.getRelationships( relType, Direction.OUTGOING) ) {
                // Add to output
                output.add( rel.getEndNode() );
            }
        }

        // Sort
        output.sort(
            Comparator.comparing(
                n -> ( (ZonedDateTime) ((Node) n).getProperty( Properties.postCreatedAt ) ).toEpochSecond(),
                comparator
            )
        );

        return output;
    }
}
