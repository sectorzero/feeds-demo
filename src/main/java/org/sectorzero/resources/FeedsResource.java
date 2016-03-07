package org.sectorzero.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import lombok.Value;
import org.apache.commons.lang.Validate;
import org.sectorzero.core.articles.ArticleRef;
import org.sectorzero.core.feeds.FeedsQueuePersistentStore;
import org.sectorzero.core.feeds.FeedsView;
import org.sectorzero.model.Feed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO : Convert exception to externally visible error/response code

@Path("/feeds")
@Api(value = "/feeds", description = "Demos an API which uses a database to put and get a resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Value
public class FeedsResource {

  final FeedsView feedsView;
  final FeedsQueuePersistentStore feedsQueuePersistentStore;
  final Clock clock;

  @Inject
  public FeedsResource(FeedsView feedsView, FeedsQueuePersistentStore feedsQueuePersistentStore, Clock clock) {
    this.feedsView = feedsView;
    this.feedsQueuePersistentStore = feedsQueuePersistentStore;
    this.clock = clock;
  }

  @GET
  @ApiOperation(
      value = "Returns all the feed labels available",
      notes = "Returns all the feed labels available",
      response = String.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Invalid Message ( dummy for demo )"),
      @ApiResponse(code = 404, message = "Not Found ( dummy for demo )") })
  @Timed
  public Collection<String> listFeeds() {
    return feedsView.getConfig().getFeeds();
  }

  @GET
  @Path("/{feedname}")
  @ApiOperation(
      value = "Shows the latest articles in a feed",
      notes = "Shows the latest articles in a feed",
      response = Feed.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Invalid Message ( dummy for demo )"),
      @ApiResponse(code = 404, message = "Not Found ( dummy for demo )") })
  @Timed
  public Feed listFeed(
      @ApiParam(value = "name of the feed that needs to be fetched", required = true) @PathParam("feedname") String feedName) {
    Validate.isTrue(feedsView.getConfig().getFeeds().contains(feedName));
    return new Feed(
        feedName,
        StreamSupport.<ArticleRef>stream(
            Spliterators.spliteratorUnknownSize(
                feedsView.getFeedViewStore().getArticlesForFeed(feedName),
                Spliterator.ORDERED),
            false)
        .map(ArticleRef::getArticleId)
        .collect(
            Collectors.toList()));
  }

  @GET
  @Path("/submit/{feedname}")
  @ApiOperation(
      value = "Add an article to the a feed",
      notes = "Add an article to the a feed")
  @ApiResponses(value = {
      @ApiResponse(code = 405, message = "Invalid Input ( dummy for demo )")})
  @Timed
  public Response postArticleToFeed(
      @ApiParam(value = "name of the feed that needs to be posted to", required = true) @PathParam("feedname") String feedName,
      @ApiParam(value = "id of the article", required = true) @QueryParam("articleRefId") String articleRefId) {
    Validate.isTrue(feedsView.getConfig().getFeeds().contains(feedName));
    feedsQueuePersistentStore.append(
        feedName,
        new ArticleRef(ZonedDateTime.now(clock), articleRefId));
    return Response.ok().entity("SUCCESS").build();
  }
}
