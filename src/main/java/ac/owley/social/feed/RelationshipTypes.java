package ac.owley.social.feed;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType
{

    FRIEND_OF,
    FOLLOWS,

    POSTED,
    REPOSTED,
    COMMENTED

}
