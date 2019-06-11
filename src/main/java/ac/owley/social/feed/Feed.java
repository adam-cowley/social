package ac.owley.social.feed;

import ac.owley.social.feed.procedures.GetFeed;
import ac.owley.social.feed.result.PostResult;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class Feed
{

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(name="social.feed")
    @Description("social.feed(username, [limit, [cursorType, since]]) :: post, author | Get the feed for a user")
    public Stream<PostResult> socialFeed(
            @Name("username") String username,
            @Name(value = "limit", defaultValue = "10") Double limit,
            @Name(value = "cursor", defaultValue = "before") String cursorType,
            @Name(value = "sinceId", defaultValue = "") String sinceId
    ) {
        GetFeed feed = new GetFeed(db, log);

        if ( limit == null ) {
            limit = 10d;
        }

        return feed.forUser(username, cursorType, sinceId, limit);
    }

}
