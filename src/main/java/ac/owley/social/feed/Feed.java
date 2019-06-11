package ac.owley.social.feed;

import ac.owley.social.feed.procedures.GetFeed;
import ac.owley.social.feed.result.PostResult;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class Feed
{

    @Context
    public GraphDatabaseService db;


    @Procedure(name="social.feed")
    @Description("social.feed(username, [since])")
    public Stream<PostResult> socialFeed(
            @Name("username") String username,
            @Name(value = "limit", defaultValue = "10") Double limit,
            @Name(value = "cursor", defaultValue = "before") String cursorType,
            @Name(value = "since", defaultValue = "before") String cursor

    ) {
        GetFeed feed = new GetFeed(db);

        if ( limit == null ) {
            limit = 1d;
        }

        return feed.forUser(username, cursorType, cursor, limit);
    }

}
