package ac.owley.social.feed;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.junit.Neo4jRule;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class FeedTest
{

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withProcedure( Feed.class )
            .withFixture(
                    " CREATE (u1:User { username: 'adam' }) " +
                    " CREATE (u2:User { name: 'luke' }) " +
                    " CREATE (u3:User { name: 'lauren' }) " +
                    " CREATE (u4:User { name: 'jon' }) " +
                    " CREATE (u5:User { name: 'mike' }) " +
                    " CREATE (u1)-[:FOLLOWS]->(u2) " +
                    " CREATE (u1)-[:FOLLOWS]->(u2) " +
                    " CREATE (u1)-[:FOLLOWS]->(u3) " +
                    " CREATE (u1)-[:FOLLOWS]->(u4) " +
                    " CREATE (u1)-[:FOLLOWS]->(u5) " +

                    " CREATE (u2)-[:POSTED_ON_2019_06_10]->(  :Post {id: '7', createdAt: datetime('2019-06-10T13:40:00.0000Z')})" +
                    " CREATE (u2)-[:POSTED_ON_2019_06_10]->(  :Post {id: '6', createdAt: datetime('2019-06-10T13:30:00.0000Z')})" +
                    " CREATE (u2)-[:POSTED_ON_2019_06_10]->(  :Post {id: '5', createdAt: datetime('2019-06-10T12:00:00.0000Z')})" +
                    " CREATE (u3)-[:POSTED_ON_2019_06_10]->(  :Post {id: '4', createdAt: datetime('2019-06-10T11:00:00.0000Z')})" +
                    " CREATE (u4)-[:POSTED_ON_2019_06_10]->(  :Post {id: '3', createdAt: datetime('2019-06-10T10:00:00.0000Z')})" +
                    " CREATE (u5)-[:POSTED_ON_2019_06_10]->(  :Post {id: '2', createdAt: datetime('2019-06-10T09:00:00.0000Z')})" +
                    " CREATE (u5)-[:POSTED_ON_2019_06_09]->(p1:Post {id: '1', createdAt: datetime('2019-06-09T13:00:00.0000Z')})" +
                    " CREATE (u3)-[:COMMENTED]->(p1)" +
                    " CREATE (u2)-[:COMMENTED]->(p1)" +
                    " CREATE (u4)-[:COMMENTED]->(p1)"
            );

    @Test
    public void shouldGetLatestPostsAndLimitNumberOfResults() {
        List<String> ids = runAndGetIds("CALL social.feed('adam', 1) YIELD post, author RETURN *");

        assertEquals(1, ids.size());

        assertTrue( ids.contains( "7" ) );
    }

    @Test
    public void shouldReturnResultsOverMultipleDays() {
        List<String> ids = runAndGetIds("CALL social.feed('adam', 10) YIELD post, author RETURN *");

        assertEquals(7, ids.size());
    }

    @Test
    public void shouldCutOffResultsInDayForBeforeCursor() {
        List<String> ids = runAndGetIds("CALL social.feed('adam', 3, 'before', '7') YIELD post, author RETURN *");

        assertEquals(3, ids.size());

        assertTrue( ids.contains( "6" ) );
        assertTrue( ids.contains( "5" ) );
        assertTrue( ids.contains( "4" ) );
    }

    @Test
    public void shouldPaginateBeforeCursor() {
        GraphDatabaseService db = neo4j.getGraphDatabaseService();

        try ( Transaction tx = db.beginTx() )  {
            Result all = db.execute("CALL social.feed('adam', 4) YIELD post RETURN collect(post.id) AS ids");
            List<String> allIds = (List) all.next().get("ids");

            Result batches = db.execute(
                    "CALL social.feed('adam', 2) YIELD post WITH collect(post.id) AS first " +
                    "CALL social.feed('adam', 2, 'before', first[-1]) YIELD post WITH first, collect(post.id) AS second " +
                    "RETURN first + second AS ids"
            );
            List<String> batchIds = (List) batches.next().get("ids");

            assertEquals(allIds, batchIds);
        }
    }

    private List<String> runAndGetIds(String query) {
        GraphDatabaseService db = neo4j.getGraphDatabaseService();

        List<String> ids = new ArrayList<>(  );

        try ( Transaction tx = db.beginTx() )  {
            Result res = db.execute(query);

            while ( res.hasNext() ) {
                Node post = (Node) res.next().get("post");

                ids.add( (String) post.getProperty("id") );

                System.out.println(
                        post.getProperty("id")
                                + " -- "
                                + post.getProperty("createdAt")
                );
            }

            return ids;
        }
    }
}
